package com.stratos.promotion.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.stratos.common.constant.CacheConstants;
import com.stratos.common.exception.BusinessException;
import com.stratos.common.result.ResultCode;
import com.stratos.common.util.SnowflakeIdGenerator;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.stratos.promotion.dto.BindSeckillOrderDTO;
import com.stratos.promotion.dto.SeckillDTO;
import com.stratos.promotion.entity.PromotionSeckill;
import com.stratos.promotion.entity.PromotionSeckillRecord;
import com.stratos.promotion.vo.SeckillResultVO;
import com.stratos.promotion.mapper.PromotionSeckillMapper;
import com.stratos.promotion.mapper.PromotionSeckillRecordMapper;
import com.stratos.promotion.service.SeckillService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 秒杀：Redis Lua 原子扣库存 + 限购。
 *
 * @author StratosCommerce
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SeckillServiceImpl extends ServiceImpl<PromotionSeckillMapper, PromotionSeckill> implements SeckillService {

    private static final DefaultRedisScript<Long> DEDUCT_SCRIPT = new DefaultRedisScript<>();

    static {
        DEDUCT_SCRIPT.setResultType(Long.class);
        DEDUCT_SCRIPT.setScriptText("""
                local bought = tonumber(redis.call('GET', KEYS[2]) or '0')
                local qty = tonumber(ARGV[1])
                local limit = tonumber(ARGV[2])
                if bought + qty > limit then
                  return -3
                end
                local stock = tonumber(redis.call('GET', KEYS[1]) or '-1')
                if stock < 0 then
                  return -2
                end
                if stock < qty then
                  return -1
                end
                redis.call('DECRBY', KEYS[1], qty)
                redis.call('INCRBY', KEYS[2], qty)
                redis.call('EXPIRE', KEYS[2], 86400)
                return stock - qty
                """);
    }

    private final PromotionSeckillRecordMapper seckillRecordMapper;
    private final StringRedisTemplate stringRedisTemplate;
    private final SnowflakeIdGenerator snowflakeIdGenerator;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SeckillResultVO doSeckill(SeckillDTO dto) {
        PromotionSeckill seckill = getById(dto.getSeckillId());
        if (seckill == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "秒杀活动不存在");
        }
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(seckill.getStartTime())) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "秒杀活动未开始");
        }
        if (now.isAfter(seckill.getEndTime())) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "秒杀活动已结束");
        }
        if (seckill.getStatus() != null && seckill.getStatus() == 3) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "秒杀活动已下架");
        }

        warmupStockIfAbsent(seckill);
        String stockKey = CacheConstants.Seckill.STOCK + dto.getSeckillId();
        String userKey = CacheConstants.Seckill.USER + dto.getSeckillId() + ":" + dto.getUserId();
        int limit = seckill.getLimitPerUser() == null ? 1 : seckill.getLimitPerUser();
        Long remain = stringRedisTemplate.execute(
                DEDUCT_SCRIPT,
                List.of(stockKey, userKey),
                String.valueOf(dto.getQuantity()),
                String.valueOf(limit));
        if (remain == null || remain == -2) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "秒杀库存未预热");
        }
        if (remain == -3) {
            PromotionSeckillRecord existing = findUserRecord(dto.getSeckillId(), dto.getUserId());
            if (existing != null) {
                return toResult(existing, true);
            }
            stringRedisTemplate.delete(userKey);
            remain = stringRedisTemplate.execute(
                    DEDUCT_SCRIPT,
                    List.of(stockKey, userKey),
                    String.valueOf(dto.getQuantity()),
                    String.valueOf(limit));
            if (remain != null && remain == -3) {
                existing = findUserRecord(dto.getSeckillId(), dto.getUserId());
                if (existing != null) {
                    return toResult(existing, true);
                }
                throw new BusinessException(ResultCode.BUSINESS_ERROR, "超过限购数量");
            }
        }
        if (remain == -1) {
            throw new BusinessException(ResultCode.PRODUCT_STOCK_INSUFFICIENT, "秒杀库存不足");
        }

        PromotionSeckillRecord record = new PromotionSeckillRecord();
        record.setId(snowflakeIdGenerator.nextId());
        record.setSeckillId(dto.getSeckillId());
        record.setUserId(dto.getUserId());
        record.setSkuId(seckill.getSkuId());
        record.setQuantity(dto.getQuantity());
        record.setSeckillPrice(seckill.getSeckillPrice());
        record.setStatus(0);
        LocalDateTime nowFill = LocalDateTime.now();
        record.setCreateTime(nowFill);
        record.setUpdateTime(nowFill);
        try {
            seckillRecordMapper.insert(record);
            int sold = seckill.getSoldCount() == null ? 0 : seckill.getSoldCount();
            boolean updated = lambdaUpdate()
                    .set(PromotionSeckill::getSoldCount, sold + dto.getQuantity())
                    .set(PromotionSeckill::getSeckillStock, Math.max(
                            (seckill.getSeckillStock() == null ? 0 : seckill.getSeckillStock()) - dto.getQuantity(), 0))
                    .set(seckill.getStatus() != null && seckill.getStatus() == 0, PromotionSeckill::getStatus, 1)
                    .eq(PromotionSeckill::getId, seckill.getId())
                    .update();
            if (!updated) {
                throw new BusinessException(ResultCode.BUSINESS_ERROR, "秒杀活动更新失败");
            }
        } catch (RuntimeException ex) {
            stringRedisTemplate.opsForValue().increment(stockKey, dto.getQuantity());
            stringRedisTemplate.opsForValue().increment(userKey, -dto.getQuantity().longValue());
            throw ex;
        }
        log.info("秒杀成功 seckillId={} userId={} remain={}", dto.getSeckillId(), dto.getUserId(), remain);
        return toResult(record, false);
    }

    @Override
    public List<PromotionSeckill> listActive() {
        return lambdaQuery()
                .in(PromotionSeckill::getStatus, 0, 1)
                .orderByAsc(PromotionSeckill::getStartTime)
                .list();
    }

    @Override
    public PromotionSeckill querySeckillDetail(Long seckillId) {
        PromotionSeckill seckill = getById(seckillId);
        if (seckill != null) {
            warmupStockIfAbsent(seckill);
        }
        return seckill;
    }

    @Override
    public void warmup(Long seckillId) {
        PromotionSeckill seckill = getById(seckillId);
        if (seckill == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "秒杀活动不存在");
        }
        String stockKey = CacheConstants.Seckill.STOCK + seckill.getId();
        int remain = seckill.getSeckillStock() == null ? 0 : seckill.getSeckillStock();
        stringRedisTemplate.opsForValue().set(stockKey, String.valueOf(remain));
    }

    @Override
    public void bindOrder(BindSeckillOrderDTO dto) {
        PromotionSeckillRecord record = seckillRecordMapper.selectById(dto.getRecordId());
        if (record == null || !dto.getUserId().equals(record.getUserId())) {
            throw new BusinessException(ResultCode.NOT_FOUND, "秒杀记录不存在");
        }
        record.setOrderId(dto.getOrderId());
        record.setOrderNo(dto.getOrderNo());
        record.setUpdateTime(LocalDateTime.now());
        seckillRecordMapper.updateById(record);
    }

    @Override
    public List<PromotionSeckillRecord> listUserRecords(Long userId) {
        return seckillRecordMapper.selectList(new LambdaQueryWrapper<PromotionSeckillRecord>()
                .eq(PromotionSeckillRecord::getUserId, userId)
                .orderByDesc(PromotionSeckillRecord::getCreateTime));
    }

    @Override
    public PromotionSeckillRecord findUserRecord(Long seckillId, Long userId) {
        return seckillRecordMapper.selectOne(new LambdaQueryWrapper<PromotionSeckillRecord>()
                .eq(PromotionSeckillRecord::getSeckillId, seckillId)
                .eq(PromotionSeckillRecord::getUserId, userId)
                .orderByDesc(PromotionSeckillRecord::getCreateTime)
                .last("LIMIT 1"));
    }

    @Override
    public PromotionSeckillRecord findByOrderId(Long orderId) {
        return seckillRecordMapper.selectOne(new LambdaQueryWrapper<PromotionSeckillRecord>()
                .eq(PromotionSeckillRecord::getOrderId, orderId)
                .last("LIMIT 1"));
    }

    private SeckillResultVO toResult(PromotionSeckillRecord record, boolean reused) {
        SeckillResultVO vo = new SeckillResultVO();
        vo.setRecordId(record.getId());
        vo.setSeckillId(record.getSeckillId());
        vo.setSkuId(record.getSkuId());
        vo.setUserId(record.getUserId());
        vo.setOrderId(record.getOrderId());
        vo.setOrderNo(record.getOrderNo());
        vo.setQuantity(record.getQuantity());
        vo.setSeckillPrice(record.getSeckillPrice());
        vo.setStatus(record.getStatus());
        vo.setReused(reused);
        return vo;
    }

    private void warmupStockIfAbsent(PromotionSeckill seckill) {
        String stockKey = CacheConstants.Seckill.STOCK + seckill.getId();
        Boolean exists = stringRedisTemplate.hasKey(stockKey);
        if (exists == null || !exists) {
            int remain = seckill.getSeckillStock() == null ? 0 : seckill.getSeckillStock();
            stringRedisTemplate.opsForValue().set(stockKey, String.valueOf(remain));
        }
    }

}

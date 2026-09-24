package com.stratos.promotion.service;

import com.stratos.common.util.SnowflakeIdGenerator;
import com.stratos.promotion.entity.PromotionCoupon;
import com.stratos.promotion.entity.PromotionGroupBuying;
import com.stratos.promotion.entity.PromotionSeckill;
import com.stratos.promotion.mapper.PromotionCouponMapper;
import com.stratos.promotion.mapper.PromotionGroupBuyingMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class PromotionWriteSupport {

    private final SnowflakeIdGenerator snowflakeIdGenerator;
    private final SeckillService seckillService;
    private final PromotionCouponMapper couponMapper;
    private final PromotionGroupBuyingMapper groupBuyingMapper;

    public boolean saveSeckill(PromotionSeckill entity) {
        LocalDateTime now = LocalDateTime.now();
        if (entity.getId() == null) {
            entity.setId(snowflakeIdGenerator.nextId());
            if (entity.getSoldCount() == null) {
                entity.setSoldCount(0);
            }
            if (!StringUtils.hasText(entity.getActivityCode())) {
                entity.setActivityCode("SK" + System.currentTimeMillis());
            }
            if (entity.getVersion() == null) {
                entity.setVersion(0);
            }
        }
        if (entity.getStatus() == null) {
            entity.setStatus(1);
        }
        if (entity.getLimitPerUser() == null) {
            entity.setLimitPerUser(1);
        }
        if (entity.getStartTime() == null) {
            entity.setStartTime(now);
        }
        if (entity.getEndTime() == null) {
            entity.setEndTime(now.plusDays(7));
        }
        boolean ok = seckillService.saveOrUpdate(entity);
        if (ok && entity.getSeckillStock() != null) {
            seckillService.warmup(entity.getId());
        }
        return ok;
    }

    public int saveCoupon(PromotionCoupon entity) {
        LocalDateTime now = LocalDateTime.now();
        if (entity.getId() == null) {
            entity.setId(snowflakeIdGenerator.nextId());
            if (entity.getReceivedCount() == null) {
                entity.setReceivedCount(0);
            }
            if (entity.getUsedCount() == null) {
                entity.setUsedCount(0);
            }
            if (!StringUtils.hasText(entity.getCouponCode())) {
                entity.setCouponCode("CP" + System.currentTimeMillis());
            }
            return couponMapper.insert(fillCouponDefaults(entity, now));
        }
        return couponMapper.updateById(fillCouponDefaults(entity, now));
    }

    public int saveGroup(PromotionGroupBuying entity) {
        LocalDateTime now = LocalDateTime.now();
        if (entity.getId() == null) {
            entity.setId(snowflakeIdGenerator.nextId());
            if (!StringUtils.hasText(entity.getActivityCode())) {
                entity.setActivityCode("GB" + System.currentTimeMillis());
            }
            fillGroupDefaults(entity, now);
            return groupBuyingMapper.insert(entity);
        }
        fillGroupDefaults(entity, now);
        entity.setUpdateTime(now);
        return groupBuyingMapper.updateById(entity);
    }

    private PromotionCoupon fillCouponDefaults(PromotionCoupon entity, LocalDateTime now) {
        if (entity.getStatus() == null) {
            entity.setStatus(1);
        }
        if (entity.getLimitPerUser() == null) {
            entity.setLimitPerUser(1);
        }
        if (entity.getPublishCount() == null) {
            entity.setPublishCount(1000);
        }
        if (entity.getReceiveType() == null) {
            entity.setReceiveType(1);
        }
        if (entity.getUseType() == null) {
            entity.setUseType(1);
        }
        if (entity.getCouponType() == null) {
            entity.setCouponType(1);
        }
        if (entity.getDiscountType() == null) {
            entity.setDiscountType(1);
        }
        if (entity.getValidDays() == null) {
            entity.setValidDays(7);
        }
        if (entity.getVersion() == null) {
            entity.setVersion(0);
        }
        if (entity.getStartTime() == null) {
            entity.setStartTime(now);
        }
        if (entity.getEndTime() == null) {
            entity.setEndTime(now.plusDays(30));
        }
        return entity;
    }

    private void fillGroupDefaults(PromotionGroupBuying entity, LocalDateTime now) {
        if (entity.getStatus() == null) {
            entity.setStatus(1);
        }
        if (entity.getRequireNum() == null) {
            entity.setRequireNum(2);
        }
        if (entity.getLimitHours() == null) {
            entity.setLimitHours(24);
        }
        if (entity.getLimitPerUser() == null) {
            entity.setLimitPerUser(1);
        }
        if (entity.getStartTime() == null) {
            entity.setStartTime(now);
        }
        if (entity.getEndTime() == null) {
            entity.setEndTime(now.plusDays(7));
        }
    }
}

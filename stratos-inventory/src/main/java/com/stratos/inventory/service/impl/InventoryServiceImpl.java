package com.stratos.inventory.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.stratos.common.constant.CacheConstants;
import com.stratos.common.result.ResultCode;
import com.stratos.common.exception.BusinessException;
import com.stratos.inventory.dto.LockInventoryDTO;
import com.stratos.inventory.entity.InventoryLock;
import com.stratos.inventory.entity.InventoryLog;
import com.stratos.inventory.entity.InventoryStock;
import com.stratos.inventory.mapper.InventoryLockMapper;
import com.stratos.inventory.mapper.InventoryLogMapper;
import com.stratos.inventory.mapper.InventoryStockMapper;
import com.stratos.inventory.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 库存服务实现
 *
 * @author StratosCommerce
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryServiceImpl extends ServiceImpl<InventoryStockMapper, InventoryStock> implements InventoryService {

    private final InventoryLockMapper lockMapper;
    private final InventoryLogMapper logMapper;
    private final StringRedisTemplate stringRedisTemplate;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${stratos.product.base-url:http://localhost:8083}")
    private String productBaseUrl;

    @Override
    public InventoryStock queryStock(Long skuId, Long warehouseId) {
        return lambdaQuery()
                .eq(InventoryStock::getSkuId, skuId)
                .eq(InventoryStock::getWarehouseId, warehouseId)
                .one();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void lockStock(LockInventoryDTO dto) {
        Long warehouseId = dto.getWarehouseId() == null ? 1L : dto.getWarehouseId();
        dto.setWarehouseId(warehouseId);
        InventoryStock stock = queryStock(dto.getSkuId(), warehouseId);
        if (stock == null) {
            int seed = resolveProductStock(dto.getSkuId(), dto.getQuantity());
            stock = upsertStock(dto.getSkuId(), warehouseId, seed);
        }

        if (stock.getAvailableStock() < dto.getQuantity()) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "库存不足");
        }

        stock.setAvailableStock(stock.getAvailableStock() - dto.getQuantity());
        stock.setLockStock(stock.getLockStock() + dto.getQuantity());
        saveStock(stock);

        InventoryLock lock = new InventoryLock();
        lock.setCreateTime(LocalDateTime.now());
        lock.setUpdateTime(LocalDateTime.now());
        lock.setSkuId(dto.getSkuId());
        lock.setWarehouseId(dto.getWarehouseId());
        lock.setLockQuantity(dto.getQuantity());
        lock.setLockType(2);
        lock.setSourceId(dto.getOrderId());
        lock.setSourceNo(dto.getOrderNo());
        lock.setUserId(dto.getUserId());
        lock.setStatus(0);
        lock.setExpireTime(LocalDateTime.now().plusMinutes(30));
        lockMapper.insert(lock);

        saveLog(dto.getSkuId(), dto.getWarehouseId(), 3, 2, -dto.getQuantity(),
                stock.getAvailableStock() + dto.getQuantity(), stock.getAvailableStock(),
                dto.getOrderId(), dto.getOrderNo(), "订单锁定库存");

        cacheAvailableStock(dto.getSkuId(), stock.getAvailableStock());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unlockStock(Long orderId) {
        List<InventoryLock> locks = lockMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<InventoryLock>()
                        .eq("source_id", orderId)
                        .eq("status", 0)
        );

        for (InventoryLock lock : locks) {
            InventoryStock stock = queryStock(lock.getSkuId(), lock.getWarehouseId());
            if (stock != null) {
                stock.setAvailableStock(stock.getAvailableStock() + lock.getLockQuantity());
                stock.setLockStock(stock.getLockStock() - lock.getLockQuantity());
                saveStock(stock);

                lock.setStatus(2);
                lock.setReleaseTime(LocalDateTime.now());
                lockMapper.updateById(lock);

                saveLog(lock.getSkuId(), stock.getWarehouseId(), 4, 2, lock.getLockQuantity(),
                        stock.getAvailableStock() - lock.getLockQuantity(), stock.getAvailableStock(),
                        orderId, lock.getSourceNo(), "订单取消释放库存");
                cacheAvailableStock(lock.getSkuId(), stock.getAvailableStock());
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deductStock(Long orderId) {
        List<InventoryLock> locks = lockMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<InventoryLock>()
                        .eq("source_id", orderId)
                        .eq("status", 0)
        );

        for (InventoryLock lock : locks) {
            InventoryStock stock = queryStock(lock.getSkuId(), lock.getWarehouseId());
            if (stock != null) {
                stock.setLockStock(stock.getLockStock() - lock.getLockQuantity());
                stock.setTotalStock(stock.getTotalStock() - lock.getLockQuantity());
                stock.setSoldStock((stock.getSoldStock() == null ? 0 : stock.getSoldStock()) + lock.getLockQuantity());
                saveStock(stock);

                lock.setStatus(1);
                lock.setDeductTime(LocalDateTime.now());
                lockMapper.updateById(lock);

                saveLog(lock.getSkuId(), stock.getWarehouseId(), 2, 2, -lock.getLockQuantity(),
                        stock.getTotalStock() + lock.getLockQuantity(), stock.getTotalStock(),
                        orderId, lock.getSourceNo(), "订单支付扣减库存");
                cacheAvailableStock(lock.getSkuId(), stock.getAvailableStock());
            }
        }
    }

    @Override
    public List<InventoryStock> batchQueryStock(List<Long> skuIds) {
        if (skuIds == null || skuIds.isEmpty()) {
            return List.of();
        }
        return lambdaQuery().in(InventoryStock::getSkuId, skuIds).list();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public InventoryStock upsertStock(Long skuId, Long warehouseId, Integer stock) {
        Long warehouse = warehouseId == null ? 1L : warehouseId;
        int target = stock == null || stock < 0 ? 0 : stock;
        InventoryStock current = queryStock(skuId, warehouse);
        if (current == null) {
            current = new InventoryStock();
            current.setSkuId(skuId);
            current.setWarehouseId(warehouse);
            current.setTotalStock(target);
            current.setAvailableStock(target);
            current.setLockStock(0);
            current.setSoldStock(0);
            current.setInTransitStock(0);
            current.setLowStockThreshold(10);
            current.setStatus(1);
            current.setVersion(0);
            current.setCreateTime(LocalDateTime.now());
            current.setUpdateTime(LocalDateTime.now());
            save(current);
            cacheAvailableStock(skuId, target);
            return current;
        }
        int locked = current.getLockStock() == null ? 0 : current.getLockStock();
        current.setAvailableStock(target);
        current.setTotalStock(target + locked);
        saveStock(current);
        cacheAvailableStock(skuId, target);
        return current;
    }

    private int resolveProductStock(Long skuId, Integer need) {
        int fallback = Math.max(need == null ? 1 : need, 100);
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> body = restTemplate.getForObject(productBaseUrl + "/product/sku/" + skuId, Map.class);
            Object data = body == null ? null : body.get("data");
            if (data instanceof Map<?, ?> sku) {
                Object stock = sku.get("stock");
                if (stock != null) {
                    int value = Integer.parseInt(stock.toString());
                    return Math.max(value, need == null ? 0 : need);
                }
            }
        } catch (Exception ex) {
            log.warn("同步商品库存失败 skuId={}: {}", skuId, ex.getMessage());
        }
        return fallback;
    }

    @Override
    public boolean deductStockDirect(Long skuId, Integer quantity) {
        String key = CacheConstants.Inventory.STOCK + skuId;
        Long stock = stringRedisTemplate.opsForValue().decrement(key, quantity);
        if (stock != null && stock < 0) {
            stringRedisTemplate.opsForValue().increment(key, quantity);
            return false;
        }

        // 同步扣减数据�?
        InventoryStock inventoryStock = queryStock(skuId, null);
        if (inventoryStock != null && inventoryStock.getAvailableStock() >= quantity) {
            inventoryStock.setAvailableStock(inventoryStock.getAvailableStock() - quantity);
            inventoryStock.setTotalStock(inventoryStock.getTotalStock() - quantity);
            saveStock(inventoryStock);
            return true;
        }
        return false;
    }

    private void saveStock(InventoryStock stock) {
        boolean ok = lambdaUpdate()
                .set(InventoryStock::getAvailableStock, stock.getAvailableStock())
                .set(InventoryStock::getLockStock, stock.getLockStock())
                .set(InventoryStock::getTotalStock, stock.getTotalStock())
                .set(InventoryStock::getSoldStock, stock.getSoldStock())
                .set(InventoryStock::getUpdateTime, LocalDateTime.now())
                .eq(InventoryStock::getId, stock.getId())
                .update();
        if (!ok) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "库存更新失败");
        }
    }

    private void saveLog(Long skuId, Long warehouseId, Integer changeType, Integer sourceType, Integer changeQuantity,
                         Integer beforeStock, Integer afterStock, Long sourceId, String sourceNo, String remark) {
        InventoryLog log = new InventoryLog();
        log.setSkuId(skuId);
        log.setWarehouseId(warehouseId);
        log.setChangeType(changeType);
        log.setChangeQuantity(changeQuantity);
        log.setBeforeStock(beforeStock);
        log.setAfterStock(afterStock);
        log.setSourceType(sourceType);
        log.setSourceId(sourceId);
        log.setSourceNo(sourceNo);
        log.setRemark(remark);
        log.setCreateTime(LocalDateTime.now());
        logMapper.insert(log);
    }

    private void cacheAvailableStock(Long skuId, Integer available) {
        stringRedisTemplate.opsForValue().set(
                CacheConstants.Inventory.STOCK + skuId,
                String.valueOf(available == null ? 0 : available)
        );
    }

}

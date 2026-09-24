package com.stratos.inventory.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.stratos.inventory.dto.LockInventoryDTO;
import com.stratos.inventory.entity.InventoryStock;

import java.util.List;

/**
 * 库存服务接口
 *
 * @author StratosCommerce
 * @since 1.0.0
 */
public interface InventoryService extends IService<InventoryStock> {

    /**
     * 查询库存
     */
    InventoryStock queryStock(Long skuId, Long warehouseId);

    /**
     * 锁定库存（订单创建时�?
     */
    void lockStock(LockInventoryDTO dto);

    /**
     * 释放锁定库存（订单取消时�?
     */
    void unlockStock(Long orderId);

    /**
     * 扣减库存（订单支付成功时�?
     */
    void deductStock(Long orderId);

    /**
     * 批量查询库存
     */
    List<InventoryStock> batchQueryStock(List<Long> skuIds);

    InventoryStock upsertStock(Long skuId, Long warehouseId, Integer stock);

    /**
     * 库存扣减（同步扣减，不走订单流程�?
     */
    boolean deductStockDirect(Long skuId, Integer quantity);

}

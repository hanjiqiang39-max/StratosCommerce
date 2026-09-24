package com.stratos.order.dto;

import lombok.Data;

/**
 * 锁定库存（字段与库存服务 LockInventoryDTO 对齐）
 *
 * @author StratosCommerce
 * @since 1.0.0
 */
@Data
public class LockStockCommand {

    private Long userId;
    private Long orderId;
    private String orderNo;
    private Long warehouseId;
    private Long productId;
    private Long skuId;
    private Integer quantity;

}

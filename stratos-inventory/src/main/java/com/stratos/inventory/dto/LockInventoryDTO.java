package com.stratos.inventory.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 库存锁定DTO
 *
 * @author StratosCommerce
 * @since 1.0.0
 */
@Data
public class LockInventoryDTO {

    @NotNull(message = "用户ID不能为空")
    private Long userId;

    @NotNull(message = "订单ID不能为空")
    private Long orderId;

    @NotNull(message = "订单号不能为空")
    private String orderNo;

    @NotNull(message = "仓库ID不能为空")
    private Long warehouseId;

    @NotNull(message = "商品ID不能为空")
    private Long productId;

    @NotNull(message = "SKU ID不能为空")
    private Long skuId;

    @NotNull(message = "数量不能为空")
    private Integer quantity;

}

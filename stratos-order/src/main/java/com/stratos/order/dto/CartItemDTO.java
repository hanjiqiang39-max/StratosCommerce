package com.stratos.order.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CartItemDTO {

    @NotNull(message = "用户ID不能为空")
    private Long userId;

    @NotNull(message = "SKU不能为空")
    private Long skuId;

    @NotNull(message = "SPU不能为空")
    private Long spuId;

    @NotNull
    @Min(1)
    private Integer quantity;

}

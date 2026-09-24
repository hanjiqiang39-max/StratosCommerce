package com.stratos.inventory.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpsertStockDTO {

    @NotNull(message = "SKU ID不能为空")
    private Long skuId;

    private Long warehouseId;

    @NotNull(message = "库存不能为空")
    private Integer stock;
}

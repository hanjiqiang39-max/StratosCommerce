package com.stratos.promotion.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 满减试算
 *
 * @author StratosCommerce
 * @since 1.0.0
 */
@Data
public class FullDiscountCalcDTO {

    @NotNull(message = "订单金额不能为空")
    private BigDecimal amount;

    private Long activityId;

    private List<Long> spuIds;
}

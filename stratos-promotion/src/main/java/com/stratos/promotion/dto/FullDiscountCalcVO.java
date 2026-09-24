package com.stratos.promotion.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 满减试算结果
 *
 * @author StratosCommerce
 * @since 1.0.0
 */
@Data
public class FullDiscountCalcVO {

    private Long activityId;

    private String activityName;

    private BigDecimal fullAmount;

    private BigDecimal discountAmount;

    private BigDecimal payAmount;
}

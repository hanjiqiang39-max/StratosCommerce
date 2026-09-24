package com.stratos.order.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 下单结果，支付创单需要这些字段。
 */
@Data
public class OrderCreateVO {

    private Long orderId;

    private String orderNo;

    private Long userId;

    private BigDecimal totalAmount;

    private BigDecimal freightAmount;

    private BigDecimal payAmount;

    private Integer status;
}

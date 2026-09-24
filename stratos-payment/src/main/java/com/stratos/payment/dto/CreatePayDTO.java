package com.stratos.payment.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 创建支付单请求DTO
 *
 * @author StratosCommerce
 * @since 1.0.0
 */
@Data
public class CreatePayDTO {

    @NotNull(message = "订单ID不能为空")
    private Long orderId;

    @NotNull(message = "订单号不能为空")
    private String orderNo;

    @NotNull(message = "用户ID不能为空")
    private Long userId;

    @NotNull(message = "支付渠道不能为空")
    private Integer payChannel;

    @NotNull(message = "支付金额不能为空")
    private BigDecimal payAmount;

    private String subject;

    private String body;

}

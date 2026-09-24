package com.stratos.payment.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 申请退款请求DTO
 *
 * @author StratosCommerce
 * @since 1.0.0
 */
@Data
public class RefundDTO {

    @NotNull(message = "支付单号不能为空")
    private String payNo;

    @NotNull(message = "退款金额不能为空")
    private BigDecimal refundAmount;

    private String refundReason;

}

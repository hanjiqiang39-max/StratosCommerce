package com.stratos.order.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PaymentRefundCommand {

    private String payNo;

    private BigDecimal refundAmount;

    private String refundReason;
}

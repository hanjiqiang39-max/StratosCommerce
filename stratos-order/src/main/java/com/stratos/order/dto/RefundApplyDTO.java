package com.stratos.order.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class RefundApplyDTO {

    @NotNull
    private Long orderId;

    @NotNull
    private Long userId;

    @NotNull
    private Integer refundType;

    @NotNull
    private Integer refundReason;

    private String refundDesc;

    @NotNull
    private BigDecimal refundAmount;

}

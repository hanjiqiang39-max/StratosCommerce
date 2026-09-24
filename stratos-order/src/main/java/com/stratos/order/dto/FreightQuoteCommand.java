package com.stratos.order.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class FreightQuoteCommand {

    private Long templateId;

    private String province;

    private Integer quantity;

    private BigDecimal weight;

    private BigDecimal orderAmount;
}

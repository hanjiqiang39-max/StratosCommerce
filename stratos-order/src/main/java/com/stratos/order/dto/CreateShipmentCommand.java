package com.stratos.order.dto;

import lombok.Data;

@Data
public class CreateShipmentCommand {

    private Long orderId;

    private String orderNo;

    private String companyCode;
}

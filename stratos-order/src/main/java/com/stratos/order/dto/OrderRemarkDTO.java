package com.stratos.order.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OrderRemarkDTO {

    @NotNull
    private Long orderId;

    private Long shopId;

    private String sellerRemark;
}

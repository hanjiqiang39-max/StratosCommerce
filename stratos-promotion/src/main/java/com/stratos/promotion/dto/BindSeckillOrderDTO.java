package com.stratos.promotion.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BindSeckillOrderDTO {

    @NotNull
    private Long recordId;

    @NotNull
    private Long userId;

    @NotNull
    private Long orderId;

    @NotNull
    private String orderNo;
}

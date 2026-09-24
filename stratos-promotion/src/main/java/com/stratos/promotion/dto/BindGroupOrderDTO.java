package com.stratos.promotion.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 把已创建订单绑定到拼团成员
 *
 * @author StratosCommerce
 * @since 1.0.0
 */
@Data
public class BindGroupOrderDTO {

    @NotNull
    private Long groupRecordId;

    @NotNull
    private Long userId;

    @NotNull
    private Long orderId;

    @NotNull
    private String orderNo;
}

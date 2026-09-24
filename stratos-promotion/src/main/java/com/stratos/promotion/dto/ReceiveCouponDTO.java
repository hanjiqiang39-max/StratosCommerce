package com.stratos.promotion.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 领取优惠券DTO
 *
 * @author StratosCommerce
 * @since 1.0.0
 */
@Data
public class ReceiveCouponDTO {

    @NotNull(message = "用户ID不能为空")
    private Long userId;

    @NotNull(message = "优惠券ID不能为空")
    private Long couponId;

}

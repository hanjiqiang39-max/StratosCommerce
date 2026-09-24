package com.stratos.promotion.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 开团 / 参团
 *
 * @author StratosCommerce
 * @since 1.0.0
 */
@Data
public class GroupJoinDTO {

    @NotNull(message = "活动ID不能为空")
    private Long activityId;

    @NotNull(message = "用户ID不能为空")
    private Long userId;

    /**
     * 空则开团，有值则参团
     */
    private String groupNo;

    private Long orderId;

    private String orderNo;
}

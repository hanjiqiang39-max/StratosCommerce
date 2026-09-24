package com.stratos.promotion.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.stratos.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 用户优惠券。表无 create_by / update_by / is_deleted。
 *
 * @author StratosCommerce
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "user_coupon", excludeProperty = {"createBy", "updateBy", "isDeleted"})
public class UserCoupon extends BaseEntity {

    private Long id;

    private Long couponId;

    private Long userId;

    private String couponCode;

    /**
     * 获得方式：1=主动领取，2=系统发放，3=券码兑换
     */
    private Integer receiveType;

    /**
     * 状态：0=未使用，1=已使用，2=已过期，3=已冻结
     */
    private Integer status;

    private LocalDateTime receiveTime;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private LocalDateTime useTime;

    private Long orderId;

    private String orderNo;

}

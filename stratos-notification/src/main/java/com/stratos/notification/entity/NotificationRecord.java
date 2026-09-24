package com.stratos.notification.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.stratos.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 通知记录实体
 *
 * @author StratosCommerce
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "notification_record", excludeProperty = {"createBy", "updateBy"})
public class NotificationRecord extends BaseEntity {

    private Long id;

    private Long userId;

    /**
     * 通知类型：1=订单通知，2=支付通知，3=物流通知，4=营销通知，5=系统通知
     */
    @TableField("notification_type")
    private Integer notificationType;

    private String title;

    private String content;

    /**
     * 业务类型：1=订单，2=支付，3=物流，4=秒杀，5=优惠券
     */
    private Integer bizType;

    /**
     * 业务ID
     */
    private Long bizId;

    /**
     * 业务单号
     */
    private String bizNo;

    /**
     * 跳转链接
     */
    private String jumpUrl;

    /**
     * 是否已读：0=未读，1=已读
     */
    private Integer isRead;

    /**
     * 阅读时间
     */
    private LocalDateTime readTime;

    /**
     * 优先级：1=普通，2=重要，3=紧急
     */
    private Integer priority;

}

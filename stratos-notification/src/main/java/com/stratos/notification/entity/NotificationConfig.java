package com.stratos.notification.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.stratos.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 通知配置实体（用户偏好设置）
 *
 * @author StratosCommerce
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "notification_config", excludeProperty = {"createBy", "updateBy", "isDeleted"})
public class NotificationConfig extends BaseEntity {

    private Long id;

    private Long userId;

    /**
     * 通知类型：1=订单通知，2=支付通知，3=物流通知，4=营销通知，5=系统通知
     */
    @TableField("notification_type")
    private Integer notificationType;

    /**
     * 是否启用：0=禁用，1=启用
     */
    private Integer isEnabled;

}

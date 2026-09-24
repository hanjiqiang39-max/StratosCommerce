package com.stratos.notification.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.stratos.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 批量通知任务实体
 *
 * @author StratosCommerce
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "notification_batch_task", excludeProperty = {"updateBy"})
public class NotificationBatchTask extends BaseEntity {

    private Long id;

    /**
     * 任务名称
     */
    private String taskName;

    /**
     * 通知类型
     */
    @TableField("notification_type")
    private Integer notificationType;

    private String title;

    private String content;

    /**
     * 目标范围：1=全体用户，2=指定会员等级，3=指定用户列表
     */
    private Integer targetScope;

    /**
     * 目标值（等级ID数组或用户ID数组，JSON格式）
     */
    private String targetValue;

    /**
     * 总用户数
     */
    private Integer totalCount;

    /**
     * 已发送数
     */
    private Integer sentCount;

    /**
     * 状态：0=待发送，1=发送中，2=已完成，3=已取消
     */
    private Integer status;

    /**
     * 计划发送时间
     */
    private LocalDateTime scheduledTime;

    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    private LocalDateTime endTime;

}

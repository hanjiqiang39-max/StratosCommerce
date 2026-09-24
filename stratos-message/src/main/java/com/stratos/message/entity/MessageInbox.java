package com.stratos.message.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.stratos.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 站内信。表无 create_by / update_by。
 *
 * @author StratosCommerce
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "message_inbox", excludeProperty = {"createBy", "updateBy"})
public class MessageInbox extends BaseEntity {

    private Long id;

    private Long userId;

    /**
     * 业务类型：1=账号，2=订单，3=支付，4=物流，5=营销，6=售后
     */
    private Integer bizType;

    private String bizId;

    private String title;

    private String content;

    private String jumpUrl;

    /**
     * 是否已读：0=未读，1=已读
     */
    private Integer isRead;

    private LocalDateTime readTime;

}

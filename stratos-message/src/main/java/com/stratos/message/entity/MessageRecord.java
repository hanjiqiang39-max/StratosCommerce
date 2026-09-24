package com.stratos.message.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import com.stratos.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 消息发送记录。表无 create_by / update_by。
 *
 * @author StratosCommerce
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "message_record", excludeProperty = {"createBy", "updateBy"})
public class MessageRecord extends BaseEntity {

    private Long id;

    private String msgNo;

    private String templateCode;

    private Integer channel;

    /**
     * 业务类型：1=账号，2=订单，3=支付，4=物流，5=营销，6=售后
     */
    private Integer bizType;

    private String bizId;

    private Long receiverId;

    private String receiverTargetEncrypted;

    private String receiverTargetHash;

    private String title;

    private String content;

    /**
     * 发送状态：0=待发送，1=发送中，2=成功，3=失败，4=已取消
     */
    private Integer sendStatus;

    private Integer retryCount;

    private Integer maxRetry;

    private LocalDateTime nextRetryTime;

    private LocalDateTime sendTime;

    private String thirdPartyMsgId;

    private String failCode;

    private String failReason;

    @Version
    private Integer version;

}

package com.stratos.message.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import com.stratos.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 消息模板
 *
 * @author StratosCommerce
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("message_template")
public class MessageTemplate extends BaseEntity {

    private Long id;

    private String templateCode;

    private String templateName;

    /**
     * 发送渠道：1=站内信，2=短信，3=邮件，4=APP推送，5=微信
     */
    private Integer channel;

    /**
     * 业务类型：1=账号，2=订单，3=支付，4=物流，5=营销，6=售后
     */
    private Integer bizType;

    private String titleTemplate;

    private String contentTemplate;

    private String thirdPartyTemplateId;

    private String paramDesc;

    private Integer status;

    private String remark;

    @Version
    private Integer version;

}

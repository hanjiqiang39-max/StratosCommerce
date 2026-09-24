package com.stratos.payment.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 支付单实体
 *
 * @author StratosCommerce
 * @since 1.0.0
 */
@Data
@TableName("pay_order")
public class PayOrder {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String payNo;

    private String outTradeNo;

    private Long orderId;

    private String orderNo;

    private Long userId;

    /**
     * 支付渠道：1=支付宝，2=微信，3=银联，4=余额
     */
    private Integer payChannel;

    /**
     * 支付类型：1=订单支付，2=充值
     */
    private Integer payType;

    private BigDecimal payAmount;

    private String currency;

    /**
     * 支付状态：0=待支付，1=支付中，2=支付成功，3=支付失败，4=已关闭
     */
    private Integer status;

    private LocalDateTime payTime;

    private LocalDateTime expireTime;

    private String notifyUrl;

    private String returnUrl;

    private String subject;

    private String body;

    private String thirdPartyTradeNo;

    private String thirdPartyUserId;

    private String errorCode;

    private String errorMsg;

    private Integer notifyCount;

    private LocalDateTime lastNotifyTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer isDeleted;

    @Version
    private Integer version;

}

package com.stratos.payment.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.stratos.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 退款单实体
 *
 * @author StratosCommerce
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "pay_refund", excludeProperty = {"createBy", "updateBy"})
public class PayRefund extends BaseEntity {

    private Long id;

    private String refundNo;

    private String outRefundNo;

    private Long payId;

    private String payNo;

    private Long orderId;

    private String orderNo;

    private Long userId;

    private Integer refundChannel;

    private BigDecimal refundAmount;

    private String refundReason;

    /**
     * 退款状态：0=待退款，1=退款中，2=退款成功，3=退款失败
     */
    private Integer status;

    private LocalDateTime refundTime;

    private String thirdPartyRefundNo;

    private String errorCode;

    private String errorMsg;

    private Integer notifyCount;

    private LocalDateTime lastNotifyTime;

    private Integer version;

}

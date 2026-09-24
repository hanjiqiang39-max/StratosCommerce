package com.stratos.order.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.stratos.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 售后/退款单。表无 create_by / update_by。
 *
 * @author StratosCommerce
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "order_refund", excludeProperty = {"createBy", "updateBy"})
public class OrderRefund extends BaseEntity {

    private Long id;

    private String refundNo;

    private Long orderId;

    private String orderNo;

    private Long userId;

    private Integer refundType;

    private Integer refundReason;

    private String refundDesc;

    private BigDecimal refundAmount;

    private String refundImages;

    private Integer status;

    private LocalDateTime auditTime;

    private String auditRemark;

    private LocalDateTime refundTime;

}

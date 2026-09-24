package com.stratos.order.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import com.stratos.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单主表实体。
 * 逻辑表 order_info，由 ShardingSphere 按 user_id 路由到 order_info_0..7。表无 create_by/update_by。
 *
 * @author StratosCommerce
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "order_info", excludeProperty = {"createBy", "updateBy"})
public class OrderInfo extends BaseEntity {

    @TableId
    private Long id;

    private String orderNo;

    private Long userId;

    private Long shopId;

    /**
     * 订单状态：0=待支付，10=已支付，20=待发货，30=已发货，40=已收货，50=已完成
     * -10=已取消，-20=退款中，-30=已退款
     */
    private Integer status;

    private BigDecimal totalAmount;

    private BigDecimal payAmount;

    private BigDecimal freightAmount;

    private BigDecimal discountAmount;

    private BigDecimal couponAmount;

    private BigDecimal pointsAmount;

    private Integer usePoints;

    private Integer gainPoints;

    private Long couponId;

    private Integer paymentType;

    private LocalDateTime paymentTime;

    private Integer deliveryType;

    private String receiverName;

    private String receiverPhone;

    private String receiverProvince;

    private String receiverCity;

    private String receiverDistrict;

    private String receiverDetailAddress;

    private String receiverPostalCode;

    private Integer autoConfirmDay;

    private String buyerRemark;

    private String sellerRemark;

    private LocalDateTime confirmTime;

    private LocalDateTime closeTime;

    @Version
    private Integer version;

}

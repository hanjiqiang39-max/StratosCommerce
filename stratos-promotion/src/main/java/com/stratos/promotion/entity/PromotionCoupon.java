package com.stratos.promotion.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.stratos.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 优惠券模板
 *
 * @author StratosCommerce
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("promotion_coupon")
public class PromotionCoupon extends BaseEntity {

    private Long id;

    private String couponName;

    private String couponCode;

    /**
     * 优惠券类型：1=满减券，2=折扣券，3=代金券
     */
    private Integer couponType;

    /**
     * 优惠方式：1=固定金额，2=折扣百分比
     */
    private Integer discountType;

    private BigDecimal discountValue;

    private BigDecimal minAmount;

    private BigDecimal maxDiscount;

    /**
     * 发行数量（-1=无限）
     */
    private Integer publishCount;

    private Integer receivedCount;

    private Integer usedCount;

    private Integer limitPerUser;

    /**
     * 领取方式：1=主动领取，2=系统发放，3=券码兑换
     */
    private Integer receiveType;

    /**
     * 使用范围：1=全平台，2=指定分类，3=指定商品
     */
    private Integer useType;

    private String categoryIds;

    private String spuIds;

    private Integer validDays;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    private Integer status;

    private String remark;

    @Version
    private Integer version;

}

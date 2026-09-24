package com.stratos.promotion.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class UserCouponVO {

    private Long id;

    private Long couponId;

    private Long userId;

    private String couponCode;

    private String couponName;

    private Integer couponType;

    private Integer discountType;

    private BigDecimal discountValue;

    private BigDecimal minAmount;

    private BigDecimal maxDiscount;

    /**
     * 0=未使用，1=已使用，2=已过期
     */
    private Integer status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime receiveTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    private Long orderId;

    private String orderNo;
}

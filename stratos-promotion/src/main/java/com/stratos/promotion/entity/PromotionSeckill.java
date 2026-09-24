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
 * 秒杀活动
 *
 * @author StratosCommerce
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("promotion_seckill")
public class PromotionSeckill extends BaseEntity {

    private Long id;

    private String activityName;

    private String activityCode;

    private Long spuId;

    private Long skuId;

    private BigDecimal originalPrice;

    private BigDecimal seckillPrice;

    private Integer seckillStock;

    private Integer limitPerUser;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    /**
     * 状态：0=未开始，1=进行中，2=已结束，3=已下架
     */
    private Integer status;

    private Integer soldCount;

    private Integer sortOrder;

    @Version
    private Integer version;

}

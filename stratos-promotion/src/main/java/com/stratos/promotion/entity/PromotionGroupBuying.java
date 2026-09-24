package com.stratos.promotion.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.stratos.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 拼团活动
 *
 * @author StratosCommerce
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "promotion_group_buying", excludeProperty = {"createBy", "updateBy"})
public class PromotionGroupBuying extends BaseEntity {

    private Long id;

    private String activityName;

    private String activityCode;

    private Long spuId;

    private Long skuId;

    private BigDecimal originalPrice;

    private BigDecimal groupPrice;

    private Integer requireNum;

    private Integer limitHours;

    private Integer limitPerUser;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    private Integer status;
}

package com.stratos.promotion.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.stratos.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 满减活动
 *
 * @author StratosCommerce
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("promotion_full_discount")
public class PromotionFullDiscount extends BaseEntity {

    private Long id;

    private String activityName;

    private String activityCode;

    private BigDecimal fullAmount;

    private BigDecimal discountAmount;

    /**
     * 使用范围：1=全平台，2=指定分类，3=指定商品
     */
    private Integer useType;

    private String categoryIds;

    private String spuIds;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private Integer status;

    private Integer sortOrder;

    private Integer usedCount;
}

package com.stratos.promotion.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.stratos.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 秒杀记录。表无 create_by / update_by / is_deleted。
 *
 * @author StratosCommerce
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "promotion_seckill_record", excludeProperty = {"createBy", "updateBy", "isDeleted"})
public class PromotionSeckillRecord extends BaseEntity {

    private Long id;

    private Long seckillId;

    private Long userId;

    private Long skuId;

    private Integer quantity;

    private BigDecimal seckillPrice;

    private Long orderId;

    private String orderNo;

    /**
     * 状态：0=待支付，1=已支付，2=已取消，3=超时取消
     */
    private Integer status;

}

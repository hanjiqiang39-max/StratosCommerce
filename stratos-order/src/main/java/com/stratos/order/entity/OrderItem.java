package com.stratos.order.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.stratos.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 订单明细表实体。
 * 逻辑表 order_item，由 ShardingSphere 按 user_id 路由到 order_item_0..7。表无 create_by/update_by。
 *
 * @author StratosCommerce
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "order_item", excludeProperty = {"createBy", "updateBy"})
public class OrderItem extends BaseEntity {

    @TableId
    private Long id;

    private Long orderId;

    private String orderNo;

    private Long userId;

    private Long spuId;

    private Long skuId;

    private String skuName;

    private String skuImage;

    private String skuCode;

    private String specDesc;

    private BigDecimal price;

    private Integer quantity;

    private BigDecimal totalAmount;

    private BigDecimal discountAmount;

    private BigDecimal realAmount;

    private Long promotionId;

}

package com.stratos.order.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 购物车
 *
 * @author StratosCommerce
 * @since 1.0.0
 */
@Data
@TableName("shopping_cart")
public class ShoppingCart {

    private Long id;

    private Long userId;

    private Long skuId;

    private Long spuId;

    private Integer quantity;

    private Integer selected;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

}

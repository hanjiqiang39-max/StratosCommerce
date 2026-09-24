package com.stratos.product.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import com.stratos.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 商品SKU实体
 *
 * @author StratosCommerce
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "product_sku", excludeProperty = {"createBy", "updateBy"})
public class ProductSku extends BaseEntity {

    @TableId
    private Long id;

    private Long spuId;

    private String skuName;

    private String skuCode;

    private String barCode;

    private String skuImage;

    private String specJson;

    private BigDecimal price;

    private BigDecimal originalPrice;

    private BigDecimal costPrice;

    private Integer stock;

    private Integer lockStock;

    private Integer lowStockThreshold;

    private BigDecimal weight;

    private Integer status;

    private Integer sortOrder;

    @Version
    private Integer version;

    @TableField(exist = false)
    private Long shopId;

    @TableField(exist = false)
    private String productTitle;

    @TableField(exist = false)
    private String displayName;

}

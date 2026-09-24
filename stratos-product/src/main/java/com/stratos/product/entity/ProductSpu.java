package com.stratos.product.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.stratos.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 商品SPU实体
 *
 * @author StratosCommerce
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("product_spu")
public class ProductSpu extends BaseEntity {

    @TableId
    private Long id;

    private Long shopId;

    private String spuName;

    private String spuCode;

    private Long categoryId;

    private Long brandId;

    private String title;

    private String subTitle;

    private String mainImage;

    private String imageList;

    private String detailHtml;

    private java.math.BigDecimal weight;

    private java.math.BigDecimal volume;

    private String sellingPoint;

    private String tags;

    private Integer sortOrder;

    private Integer status;

    private Integer publishStatus;

    private Integer auditStatus;

    private Integer saleCount;

    private Integer viewCount;

    private Integer favoriteCount;

    private Integer commentCount;

    private Integer version;

    @TableField(exist = false)
    private java.math.BigDecimal minPrice;

}

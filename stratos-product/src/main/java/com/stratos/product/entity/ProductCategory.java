package com.stratos.product.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.stratos.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

/**
 * 商品分类实体
 *
 * @author StratosCommerce
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "product_category", excludeProperty = {"createBy", "updateBy"})
public class ProductCategory extends BaseEntity {

    @TableId
    private Long id;

    private Long parentId;

    private String categoryName;

    private String categoryCode;

    private Integer categoryLevel;

    private String icon;

    private String image;

    private String keywords;

    private String description;

    private Integer sortOrder;

    private Integer showStatus;

    private Integer navStatus;

    @TableField(exist = false)
    private List<ProductCategory> children = new ArrayList<>();

}

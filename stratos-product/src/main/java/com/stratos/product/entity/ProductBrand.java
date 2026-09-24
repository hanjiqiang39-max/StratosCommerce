package com.stratos.product.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.stratos.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 商品品牌。表无 create_by / update_by。
 *
 * @author StratosCommerce
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "product_brand", excludeProperty = {"createBy", "updateBy"})
public class ProductBrand extends BaseEntity {

    @TableId
    private Long id;

    private String brandName;

    private String brandCode;

    private String brandLogo;

    private String brandDesc;

    private String brandStory;

    private String officialWebsite;

    private String firstLetter;

    private Integer sortOrder;

    private Integer showStatus;

    private Integer productCount;

}

package com.stratos.product.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 供搜索服务导入的商品文档。
 *
 * @author StratosCommerce
 * @since 1.0.0
 */
@Data
public class ProductIndexVO {

    private Long id;

    private String name;

    private String sn;

    private Long categoryId;

    private String categoryName;

    private Long brandId;

    private String brandName;

    private BigDecimal price;

    private Integer sales;

    private String description;

    private String image;

    private Integer status;

    private Long createTime;

}

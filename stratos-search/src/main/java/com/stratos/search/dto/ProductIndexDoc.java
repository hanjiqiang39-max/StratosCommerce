package com.stratos.search.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 与商品服务 ProductIndexVO 字段对齐。
 *
 * @author StratosCommerce
 * @since 1.0.0
 */
@Data
public class ProductIndexDoc {

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

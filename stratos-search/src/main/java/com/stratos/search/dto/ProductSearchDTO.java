package com.stratos.search.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 商品搜索请求DTO
 *
 * @author StratosCommerce
 * @since 1.0.0
 */
@Data
public class ProductSearchDTO {

    private String keyword;

    private Long categoryId;

    private Long brandId;

    private BigDecimal minPrice;

    private BigDecimal maxPrice;

    private Integer sortType;

    private Integer page = 1;

    private Integer size = 20;

}

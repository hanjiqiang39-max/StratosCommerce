package com.stratos.product.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 商品详情VO（SPU + SKU列表）
 *
 * @author StratosCommerce
 * @since 1.0.0
 */
@Data
public class ProductDetailVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private Long shopId;

    private String shopName;

    private String title;

    private String subTitle;

    private String mainImage;

    private String imageList;

    private String detailHtml;

    private String sellingPoint;

    private Integer status;

    private Integer saleCount;

    private BigDecimal minPrice;

    private BigDecimal maxPrice;

    private List<SkuItemVO> skuList;

    @Data
    public static class SkuItemVO implements Serializable {
        private Long id;
        private String skuName;
        private String skuImage;
        private String specJson;
        private BigDecimal price;
        private BigDecimal originalPrice;
        private Integer stock;
    }

}

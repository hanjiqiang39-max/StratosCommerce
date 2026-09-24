package com.stratos.product.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 发布/更新商品
 *
 * @author StratosCommerce
 * @since 1.0.0
 */
@Data
public class SaveProductDTO {

    private Long id;

    private Long shopId;

    @NotBlank(message = "商品名称不能为空")
    private String spuName;

    @NotBlank(message = "商品标题不能为空")
    private String title;

    private String subTitle;

    @NotNull(message = "分类不能为空")
    private Long categoryId;

    private Long brandId;

    private String mainImage;

    private String imageList;

    private String detailHtml;

    private String sellingPoint;

    private String tags;

    private Integer status;

    private List<SkuDTO> skuList;

    @Data
    public static class SkuDTO {
        private Long id;
        private String skuName;
        private String skuCode;
        private String skuImage;
        private String specJson;
        private BigDecimal price;
        private BigDecimal originalPrice;
        private Integer stock;
    }

}

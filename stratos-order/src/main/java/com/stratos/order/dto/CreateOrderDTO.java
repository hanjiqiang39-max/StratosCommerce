package com.stratos.order.dto;

import lombok.Data;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

/**
 * 创建订单请求DTO
 *
 * @author StratosCommerce
 * @since 1.0.0
 */
@Data
public class CreateOrderDTO {

    @NotNull(message = "用户ID不能为空")
    private Long userId;

    private Long shopId;

    @NotEmpty(message = "订单明细不能为空")
    @Valid
    private List<OrderItemDTO> items;

    private Long addressId;

    @NotBlank(message = "收货人不能为空")
    private String receiverName;

    @NotBlank(message = "收货手机不能为空")
    private String receiverPhone;

    @NotBlank(message = "省份不能为空")
    private String receiverProvince;

    @NotBlank(message = "城市不能为空")
    private String receiverCity;

    @NotBlank(message = "区县不能为空")
    private String receiverDistrict;

    @NotBlank(message = "详细地址不能为空")
    private String receiverDetailAddress;

    /**
     * 发货仓，不传默认 1
     */
    private Long warehouseId;

    /**
     * 运费模板，默认 1=全国包邮
     */
    private Long freightTemplateId;

    private Long couponId;

    private Long fullDiscountId;

    private Long groupBuyingId;

    private Long groupRecordId;

    private Long seckillId;

    private Long seckillRecordId;

    private Integer usePoints;

    private String buyerRemark;

    @Data
    public static class OrderItemDTO {
        @NotNull(message = "SKU ID不能为空")
        private Long skuId;

        private Long spuId;

        private Long shopId;

        @NotNull(message = "数量不能为空")
        private Integer quantity;

        private BigDecimal price;

        private String skuName;

        private String skuImage;

        private String skuCode;
    }

}

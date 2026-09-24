package com.stratos.order.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 购物车结算。传 addressId 或完整收货地址。
 */
@Data
public class CheckoutDTO {

    @NotNull(message = "用户ID不能为空")
    private Long userId;

    /**
     * 指定购物车行；空则结算已勾选商品
     */
    private List<Long> cartIds;

    private Long addressId;

    private String receiverName;

    private String receiverPhone;

    private String receiverProvince;

    private String receiverCity;

    private String receiverDistrict;

    private String receiverDetailAddress;

    private Long warehouseId;

    private Long couponId;

    private Integer usePoints;

    private Long fullDiscountId;

    private Long groupBuyingId;

    private Long groupRecordId;

    private Long seckillId;

    private Long seckillRecordId;

    private Long freightTemplateId;

    private String buyerRemark;
}

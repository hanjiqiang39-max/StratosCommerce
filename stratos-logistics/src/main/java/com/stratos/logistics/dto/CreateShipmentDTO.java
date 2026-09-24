package com.stratos.logistics.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 订单发货
 *
 * @author StratosCommerce
 * @since 1.0.0
 */
@Data
public class CreateShipmentDTO {

    @NotNull(message = "订单ID不能为空")
    private Long orderId;

    @NotBlank(message = "订单号不能为空")
    private String orderNo;

    /**
     * 物流公司编码，默认 SF
     */
    private String companyCode;

}

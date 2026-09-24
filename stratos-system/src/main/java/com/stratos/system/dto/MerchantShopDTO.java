package com.stratos.system.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MerchantShopDTO {

    @NotNull(message = "商家ID不能为空")
    private Long merchantId;

    @NotBlank(message = "店铺名称不能为空")
    private String shopName;

    private String shopLogo;

    private String shopDesc;

    private String contactName;

    private String contactPhone;

    private String province;

    private String city;

    private String district;

    private String detailAddress;

    private Integer status;
}

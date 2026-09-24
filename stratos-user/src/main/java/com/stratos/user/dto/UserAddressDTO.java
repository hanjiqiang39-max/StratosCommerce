package com.stratos.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 收货地址
 *
 * @author StratosCommerce
 * @since 1.0.0
 */
@Data
public class UserAddressDTO {

    @NotBlank(message = "收货人不能为空")
    private String receiverName;

    @NotBlank(message = "手机号不能为空")
    private String receiverPhone;

    @NotBlank(message = "省份不能为空")
    private String province;

    @NotBlank(message = "城市不能为空")
    private String city;

    @NotBlank(message = "区县不能为空")
    private String district;

    @NotBlank(message = "详细地址不能为空")
    private String detailAddress;

    private String postalCode;

    private Integer isDefault;

    private Integer addressType;

}

package com.stratos.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.stratos.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 商家店铺
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("merchant_shop")
public class MerchantShop extends BaseEntity {

    private Long id;

    private Long merchantId;

    private String shopName;

    private String shopLogo;

    private String shopDesc;

    private String contactName;

    private String contactPhone;

    private String province;

    private String city;

    private String district;

    private String detailAddress;

    /**
     * 0=打烊，1=营业
     */
    private Integer status;
}

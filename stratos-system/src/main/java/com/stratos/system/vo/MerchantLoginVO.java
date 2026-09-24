package com.stratos.system.vo;

import lombok.Data;

@Data
public class MerchantLoginVO {

    private Long merchantId;

    private Long shopId;

    private String username;

    private String nickname;

    private String realName;

    private String shopName;

    private Integer status;

    private Integer auditStatus;

    private String token;
}

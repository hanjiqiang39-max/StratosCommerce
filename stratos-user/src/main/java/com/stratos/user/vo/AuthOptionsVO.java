package com.stratos.user.vo;

import lombok.Data;

@Data
public class AuthOptionsVO {

    private boolean smsReady;

    private String smsVendor;

    private boolean wechatReady;

    private boolean alipayReady;
}

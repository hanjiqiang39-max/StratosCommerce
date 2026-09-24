package com.stratos.payment.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 创建支付结果：支付宝渠道会带上可提交的表单 HTML
 *
 * @author StratosCommerce
 * @since 1.0.0
 */
@Data
public class PayOrderVO {

    private String payNo;

    private String outTradeNo;

    private Integer status;

    private BigDecimal payAmount;

    /**
     * 支付宝电脑网站支付表单，前端直接写入页面自动提交
     */
    private String payForm;

}

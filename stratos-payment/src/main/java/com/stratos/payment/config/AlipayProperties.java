package com.stratos.payment.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 支付宝沙箱/开放平台配置。私钥与支付宝公钥放 application-local.yml 或环境变量，不要提交仓库。
 *
 * @author StratosCommerce
 * @since 1.0.0
 */
@Data
@ConfigurationProperties(prefix = "alipay")
public class AlipayProperties {

    private boolean enabled = true;

    private boolean sandbox = true;

    private String appId;

    private String merchantId;

    private String gatewayUrl;

    private String signType = "RSA2";

    private String charset = "UTF-8";

    private String format = "json";

    private String notifyUrl;

    private String returnUrl;

    /**
     * 同步回跳后跳转的商城地址
     */
    private String shopReturnUrl = "http://localhost:5173/orders";

    /**
     * 应用私钥（RSA2，PKCS8，不要带 BEGIN/END 头）
     */
    private String privateKey;

    /**
     * 支付宝公钥（沙箱「公钥模式」里查看）
     */
    private String alipayPublicKey;

}

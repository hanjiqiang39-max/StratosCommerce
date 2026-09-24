package com.stratos.notification.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 短信配置。未填 access-key 时走控制台日志（开发联调）。
 *
 * @author StratosCommerce
 * @since 1.0.0
 */
@Data
@ConfigurationProperties(prefix = "sms")
public class SmsProperties {

    /**
     * mock=只打日志；aliyun=阿里云短信
     */
    private String vendor = "mock";

    private String accessKeyId = "";

    private String accessKeySecret = "";

    private String signName = "StratosCommerce";

    private String templateCode = "";

    public boolean aliyunReady() {
        return "aliyun".equalsIgnoreCase(vendor)
                && accessKeyId != null && !accessKeyId.isBlank()
                && accessKeySecret != null && !accessKeySecret.isBlank()
                && templateCode != null && !templateCode.isBlank();
    }

}

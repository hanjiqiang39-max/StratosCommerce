package com.stratos.payment.config;

import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

/**
 * 支付宝客户端
 *
 * @author StratosCommerce
 * @since 1.0.0
 */
@Configuration
@EnableConfigurationProperties(AlipayProperties.class)
public class AlipayClientConfig {

    @Bean
    @ConditionalOnProperty(prefix = "alipay", name = "enabled", havingValue = "true", matchIfMissing = true)
    public AlipayClient alipayClient(AlipayProperties properties) {
        if (!StringUtils.hasText(properties.getPrivateKey()) || !StringUtils.hasText(properties.getAlipayPublicKey())) {
            return null;
        }
        return new DefaultAlipayClient(
                properties.getGatewayUrl(),
                properties.getAppId(),
                properties.getPrivateKey(),
                properties.getFormat(),
                properties.getCharset(),
                properties.getAlipayPublicKey(),
                properties.getSignType()
        );
    }

}

package com.stratos.logistics.config;

import com.stratos.logistics.express.ExpressClient;
import com.stratos.logistics.express.HttpExpressClient;
import com.stratos.logistics.express.MockExpressClient;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 快递渠道：有快递100密钥走真查询，否则 mock。
 *
 * @author StratosCommerce
 * @since 1.0.0
 */
@Configuration
@EnableConfigurationProperties(ExpressProperties.class)
public class ExpressConfig {

    @Bean
    public ExpressClient expressClient(ExpressProperties properties) {
        if (properties.kuaidi100Ready()) {
            return new HttpExpressClient(properties);
        }
        return new MockExpressClient();
    }
}

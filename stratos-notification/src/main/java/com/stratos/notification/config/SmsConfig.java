package com.stratos.notification.config;

import com.stratos.notification.sms.AliyunSmsSender;
import com.stratos.notification.sms.MockSmsSender;
import com.stratos.notification.sms.SmsSender;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 短信通道装配
 *
 * @author StratosCommerce
 * @since 1.0.0
 */
@Configuration
@EnableConfigurationProperties(SmsProperties.class)
public class SmsConfig {

    @Bean
    public SmsSender smsSender(SmsProperties properties) {
        if (properties.aliyunReady()) {
            return new AliyunSmsSender(properties);
        }
        return new MockSmsSender();
    }

}

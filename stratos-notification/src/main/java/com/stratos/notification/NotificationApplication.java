package com.stratos.notification;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 通知服务启动类
 *
 * @author StratosCommerce
 * @since 1.0.0
 */
@SpringBootApplication(scanBasePackages = {"com.stratos.notification", "com.stratos.common"})
@EnableDiscoveryClient
@EnableFeignClients
@MapperScan("com.stratos.notification.mapper")
public class NotificationApplication {

    public static void main(String[] args) {
        SpringApplication.run(NotificationApplication.class, args);
    }

}

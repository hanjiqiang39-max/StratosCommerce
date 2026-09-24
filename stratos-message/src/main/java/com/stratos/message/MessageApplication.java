package com.stratos.message;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 消息服务启动�?
 *
 * @author StratosCommerce
 * @since 1.0.0
 */
@SpringBootApplication(scanBasePackages = {"com.stratos.message", "com.stratos.common"})
@EnableDiscoveryClient
@MapperScan("com.stratos.message.mapper")
public class MessageApplication {

    public static void main(String[] args) {
        SpringApplication.run(MessageApplication.class, args);
        System.out.println("=======================================");
        System.out.println("    Message Service Started Successfully");
        System.out.println("    Port: 8089");
        System.out.println("=======================================");
    }

}

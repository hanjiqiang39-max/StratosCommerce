package com.stratos.order;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 订单服务启动类
 *
 * @author StratosCommerce
 * @since 1.0.0
 */
@SpringBootApplication(scanBasePackages = {"com.stratos.order", "com.stratos.common"})
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.stratos.order.feign")
@EnableScheduling
@MapperScan("com.stratos.order.mapper")
public class OrderApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrderApplication.class, args);
        System.out.println("=======================================");
        System.out.println("    Order Service Started Successfully");
        System.out.println("    Port: 8084");
        System.out.println("=======================================");
    }

}

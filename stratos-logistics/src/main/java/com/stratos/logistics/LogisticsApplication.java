package com.stratos.logistics;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 物流服务启动�?
 *
 * @author StratosCommerce
 * @since 1.0.0
 */
@SpringBootApplication(scanBasePackages = {"com.stratos.logistics", "com.stratos.common"})
@EnableDiscoveryClient
@MapperScan("com.stratos.logistics.mapper")
public class LogisticsApplication {

    public static void main(String[] args) {
        SpringApplication.run(LogisticsApplication.class, args);
        System.out.println("=======================================");
        System.out.println("    Logistics Service Started Successfully");
        System.out.println("    Port: 8088");
        System.out.println("=======================================");
    }

}

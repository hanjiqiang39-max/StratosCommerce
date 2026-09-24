package com.stratos.user;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 用户服务启动类
 *
 * @author StratosCommerce
 * @since 1.0.0
 */
@SpringBootApplication(scanBasePackages = {"com.stratos.user", "com.stratos.common"})
@EnableDiscoveryClient
@MapperScan("com.stratos.user.mapper")
public class UserApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserApplication.class, args);
        System.out.println("=======================================");
        System.out.println("    User Service Started Successfully");
        System.out.println("    Port: 8082");
        System.out.println("=======================================");
    }

}

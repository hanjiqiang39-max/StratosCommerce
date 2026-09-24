package com.stratos.system;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 系统管理服务启动�?
 *
 * @author StratosCommerce
 * @since 1.0.0
 */
@SpringBootApplication(scanBasePackages = {"com.stratos.system", "com.stratos.common"})
@EnableDiscoveryClient
@MapperScan("com.stratos.system.mapper")
public class SystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(SystemApplication.class, args);
        System.out.println("=======================================");
        System.out.println("    System Service Started Successfully");
        System.out.println("    Port: 8090");
        System.out.println("=======================================");
    }

}

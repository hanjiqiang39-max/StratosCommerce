package com.stratos.inventory;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 库存服务启动�?
 *
 * @author StratosCommerce
 * @since 1.0.0
 */
@SpringBootApplication(scanBasePackages = {"com.stratos.inventory", "com.stratos.common"})
@EnableDiscoveryClient
@MapperScan("com.stratos.inventory.mapper")
public class InventoryApplication {

    public static void main(String[] args) {
        SpringApplication.run(InventoryApplication.class, args);
        System.out.println("=======================================");
        System.out.println("    Inventory Service Started Successfully");
        System.out.println("    Port: 8087");
        System.out.println("=======================================");
    }

}

package com.stratos.product;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 商品服务启动类
 *
 * @author StratosCommerce
 * @since 1.0.0
 */
@SpringBootApplication(scanBasePackages = {"com.stratos.product", "com.stratos.common"})
@EnableDiscoveryClient
@MapperScan("com.stratos.product.mapper")
public class ProductApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProductApplication.class, args);
        System.out.println("=======================================");
        System.out.println("    Product Service Started Successfully");
        System.out.println("    Port: 8083");
        System.out.println("=======================================");
    }

}

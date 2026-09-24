package com.stratos.search;

import com.baomidou.mybatisplus.autoconfigure.MybatisPlusAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.elasticsearch.ElasticsearchRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 搜索服务启动类
 *
 * @author StratosCommerce
 * @since 1.0.0
 */
@SpringBootApplication(
        scanBasePackages = {"com.stratos.search", "com.stratos.common"},
        exclude = {
                DataSourceAutoConfiguration.class,
                MybatisPlusAutoConfiguration.class,
                ElasticsearchRepositoriesAutoConfiguration.class
        }
)
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.stratos.search.feign")
public class SearchApplication {

    public static void main(String[] args) {
        SpringApplication.run(SearchApplication.class, args);
    }

}

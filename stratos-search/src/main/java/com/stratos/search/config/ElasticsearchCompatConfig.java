package com.stratos.search.config;

import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import org.springframework.boot.autoconfigure.elasticsearch.RestClientBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * ES 9 对 8.x 客户端要求兼容头。
 *
 * @author StratosCommerce
 * @since 1.0.0
 */
@Configuration
public class ElasticsearchCompatConfig {

    @Bean
    public RestClientBuilderCustomizer elasticsearchCompatibleWith8() {
        return builder -> builder.setDefaultHeaders(new Header[]{
                new BasicHeader("Accept", "application/vnd.elasticsearch+json; compatible-with=8"),
                new BasicHeader("Content-Type", "application/vnd.elasticsearch+json; compatible-with=8")
        });
    }

}

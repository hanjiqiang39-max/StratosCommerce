package com.stratos.product.service;

import com.stratos.product.vo.ProductIndexVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * 商品变更后同步搜索索引，搜索服务未启动时只打日志。
 *
 * @author StratosCommerce
 * @since 1.0.0
 */
@Slf4j
@Component
public class SearchIndexClient {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${stratos.search.base-url:http://localhost:8093}")
    private String searchBaseUrl;

    public void upsert(ProductIndexVO doc) {
        try {
            restTemplate.postForEntity(searchBaseUrl + "/search/index", doc, String.class);
        } catch (Exception ex) {
            log.warn("同步搜索索引失败（可稍后 POST /search/import）: {}", ex.getMessage());
        }
    }

    public void delete(Long spuId) {
        try {
            restTemplate.delete(searchBaseUrl + "/search/index/" + spuId);
        } catch (Exception ex) {
            log.warn("删除搜索索引失败: {}", ex.getMessage());
        }
    }

}

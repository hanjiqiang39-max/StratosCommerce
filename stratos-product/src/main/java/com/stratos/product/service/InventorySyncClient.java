package com.stratos.product.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class InventorySyncClient {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${stratos.inventory.base-url:http://localhost:8087}")
    private String inventoryBaseUrl;

    public void upsert(Long skuId, Integer stock) {
        if (skuId == null) {
            return;
        }
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("skuId", skuId);
            payload.put("warehouseId", 1L);
            payload.put("stock", stock == null ? 0 : stock);
            restTemplate.postForEntity(inventoryBaseUrl + "/inventory/stock/upsert", payload, String.class);
        } catch (Exception ex) {
            log.warn("同步库存服务失败 skuId={}: {}", skuId, ex.getMessage());
        }
    }
}

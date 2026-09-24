package com.stratos.order.feign;

import com.stratos.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

@FeignClient(name = "stratos-product", path = "/product")
public interface ProductFeignClient {

    @GetMapping("/sku/{skuId}")
    Result<Map<String, Object>> getSku(@PathVariable("skuId") Long skuId);
}

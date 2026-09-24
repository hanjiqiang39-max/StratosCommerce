package com.stratos.search.feign;

import com.stratos.common.base.PageResult;
import com.stratos.common.result.Result;
import com.stratos.search.dto.ProductIndexDoc;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

/**
 * 从商品服务拉取待索引数据。
 *
 * @author StratosCommerce
 * @since 1.0.0
 */
@FeignClient(name = "stratos-product", path = "/product")
public interface ProductFeignClient {

    @GetMapping("/index-docs")
    Result<List<ProductIndexDoc>> listIndexDocs();

    @GetMapping("/list")
    Result<PageResult<Map<String, Object>>> listProducts(
            @RequestParam("pageNum") Integer pageNum,
            @RequestParam("pageSize") Integer pageSize);

    @GetMapping("/{spuId}")
    Result<Map<String, Object>> getProductDetail(@PathVariable("spuId") Long spuId);

}

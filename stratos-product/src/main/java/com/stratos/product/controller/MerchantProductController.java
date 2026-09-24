package com.stratos.product.controller;

import com.stratos.common.base.PageQuery;
import com.stratos.common.base.PageResult;
import com.stratos.common.result.Result;
import com.stratos.product.dto.SaveProductDTO;
import com.stratos.product.entity.ProductSku;
import com.stratos.product.entity.ProductSpu;
import com.stratos.product.service.ProductService;
import com.stratos.product.vo.ProductDetailVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "商家商品")
@RestController
@RequestMapping("/merchant/product")
@RequiredArgsConstructor
public class MerchantProductController {

    private final ProductService productService;

    @Operation(summary = "本店商品列表")
    @GetMapping("/list")
    public Result<PageResult<ProductSpu>> list(PageQuery pageQuery,
                                               @RequestParam("shopId") Long shopId,
                                               @RequestParam(value = "keyword", required = false) String keyword,
                                               @RequestParam(value = "status", required = false) Integer status) {
        return Result.success(productService.getProductList(pageQuery, keyword, null, null, status, shopId));
    }

    @Operation(summary = "本店商品详情")
    @GetMapping("/{spuId}")
    public Result<ProductDetailVO> detail(@PathVariable Long spuId, @RequestParam("shopId") Long shopId) {
        productService.requireShopProduct(spuId, shopId);
        return Result.success(productService.getProductDetail(spuId));
    }

    @Operation(summary = "发布/更新本店商品")
    @PostMapping
    public Result<Long> save(@Validated @RequestBody SaveProductDTO dto) {
        return Result.success("保存成功", productService.saveProduct(dto));
    }

    @Operation(summary = "上下架")
    @PutMapping("/{spuId}/status/{status}")
    public Result<Void> updateStatus(@PathVariable Long spuId,
                                     @PathVariable Integer status,
                                     @RequestParam("shopId") Long shopId) {
        productService.requireShopProduct(spuId, shopId);
        productService.updateStatus(spuId, status);
        return Result.success();
    }

    @Operation(summary = "删除本店商品")
    @DeleteMapping("/{spuId}")
    public Result<Void> delete(@PathVariable Long spuId, @RequestParam("shopId") Long shopId) {
        productService.deleteProduct(spuId, shopId);
        return Result.success();
    }

    @Operation(summary = "本店 SKU 库存")
    @GetMapping("/skus")
    public Result<List<ProductSku>> skus(@RequestParam("shopId") Long shopId) {
        return Result.success(productService.listShopSkus(shopId));
    }

    @Operation(summary = "调整本店 SKU 库存")
    @PutMapping("/sku/{skuId}/stock/{stock}")
    public Result<Void> updateStock(@PathVariable Long skuId,
                                    @PathVariable Integer stock,
                                    @RequestParam("shopId") Long shopId) {
        productService.updateSkuStock(skuId, stock, shopId);
        return Result.success();
    }
}

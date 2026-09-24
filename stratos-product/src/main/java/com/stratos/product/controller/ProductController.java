package com.stratos.product.controller;

import com.stratos.common.base.PageQuery;
import com.stratos.common.base.PageResult;
import com.stratos.common.result.Result;
import com.stratos.product.dto.SaveProductDTO;
import com.stratos.product.entity.ProductBrand;
import com.stratos.product.entity.ProductCategory;
import com.stratos.product.entity.ProductSku;
import com.stratos.product.entity.ProductSpu;
import com.stratos.product.service.ProductService;
import com.stratos.product.vo.ProductDetailVO;
import com.stratos.product.vo.ProductIndexVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商品控制器
 *
 * @author StratosCommerce
 * @since 1.0.0
 */
@Tag(name = "商品管理", description = "商品查询、搜索、详情、发布")
@RestController
@RequestMapping("/product")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @Operation(summary = "商品列表")
    @GetMapping("/list")
    public Result<PageResult<ProductSpu>> getProductList(PageQuery pageQuery,
                                                         @RequestParam(value = "keyword", required = false) String keyword,
                                                         @RequestParam(value = "categoryId", required = false) Long categoryId,
                                                         @RequestParam(value = "brandId", required = false) Long brandId,
                                                         @RequestParam(value = "status", required = false) Integer status,
                                                         @RequestParam(value = "shopId", required = false) Long shopId) {
        return Result.success(productService.getProductList(pageQuery, keyword, categoryId, brandId, status, shopId));
    }

    @Operation(summary = "根据分类查询商品")
    @GetMapping("/category/{categoryId}")
    public Result<PageResult<ProductSpu>> getProductsByCategory(@PathVariable Long categoryId,
                                                                PageQuery pageQuery) {
        return Result.success(productService.getProductsByCategory(categoryId, pageQuery));
    }

    @Operation(summary = "分类树")
    @GetMapping("/categories")
    public Result<List<ProductCategory>> categoryTree() {
        return Result.success(productService.getCategoryTree());
    }

    @Operation(summary = "品牌列表")
    @GetMapping("/brand/list")
    public Result<List<ProductBrand>> brandList() {
        return Result.success(productService.listBrands());
    }

    @Operation(summary = "保存分类")
    @PostMapping("/category")
    public Result<Long> saveCategory(@RequestBody ProductCategory category) {
        return Result.success("保存成功", productService.saveCategory(category));
    }

    @Operation(summary = "删除分类")
    @DeleteMapping("/category/{id}")
    public Result<Void> deleteCategory(@PathVariable Long id) {
        productService.deleteCategory(id);
        return Result.success();
    }

    @Operation(summary = "保存品牌")
    @PostMapping("/brand")
    public Result<Long> saveBrand(@RequestBody ProductBrand brand) {
        return Result.success("保存成功", productService.saveBrand(brand));
    }

    @Operation(summary = "删除品牌")
    @DeleteMapping("/brand/{id}")
    public Result<Void> deleteBrand(@PathVariable Long id) {
        productService.deleteBrand(id);
        return Result.success();
    }

    @Operation(summary = "搜索索引文档")
    @GetMapping("/index-docs")
    public Result<List<ProductIndexVO>> listIndexDocs() {
        return Result.success(productService.listIndexDocs());
    }

    @Operation(summary = "后台 SKU 列表")
    @GetMapping("/admin/skus")
    public Result<List<ProductSku>> adminSkus() {
        return Result.success(productService.listAllSkus());
    }

    @Operation(summary = "调整 SKU 库存")
    @PutMapping("/sku/{skuId}/stock/{stock}")
    public Result<Void> updateSkuStock(@PathVariable Long skuId, @PathVariable Integer stock) {
        productService.updateSkuStock(skuId, stock);
        return Result.success();
    }

    @Operation(summary = "查询SKU")
    @GetMapping("/sku/{skuId}")
    public Result<ProductSku> getSku(@PathVariable Long skuId) {
        return Result.success(productService.getSkuById(skuId));
    }

    @Operation(summary = "删除商品")
    @DeleteMapping("/{spuId}")
    public Result<Void> deleteProduct(@PathVariable Long spuId) {
        productService.deleteProduct(spuId, null);
        return Result.success();
    }

    @Operation(summary = "发布/更新商品")
    @PostMapping
    public Result<Long> saveProduct(@Validated @RequestBody SaveProductDTO dto) {
        return Result.success("保存成功", productService.saveProduct(dto));
    }

    @Operation(summary = "上下架")
    @PutMapping("/{spuId}/status/{status}")
    public Result<Void> updateStatus(@PathVariable Long spuId, @PathVariable Integer status) {
        productService.updateStatus(spuId, status);
        return Result.<Void>success("更新成功", null);
    }

    @Operation(summary = "商品详情")
    @GetMapping({"/{spuId:\\d+}", "/detail/{spuId}"})
    public Result<ProductDetailVO> getProductDetail(@PathVariable Long spuId) {
        return Result.success(productService.getProductDetail(spuId));
    }

    @Operation(summary = "健康检查")
    @GetMapping("/health")
    public Result<String> health() {
        return Result.success("Product Service is running");
    }

}

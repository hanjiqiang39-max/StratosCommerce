package com.stratos.search.controller;

import com.stratos.common.result.Result;
import com.stratos.search.dto.ProductSearchDTO;
import com.stratos.search.dto.SearchPageVO;
import com.stratos.search.entity.ProductDocument;
import com.stratos.search.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商品搜索
 *
 * @author StratosCommerce
 * @since 1.0.0
 */
@RestController
@RequestMapping("/search")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

    @GetMapping("/health")
    public Result<String> health() {
        return Result.success("Search Service is running");
    }

    @PostMapping("/products")
    public Result<SearchPageVO<ProductDocument>> searchProducts(@RequestBody ProductSearchDTO dto) {
        return Result.success(searchService.searchProducts(dto));
    }

    @GetMapping("/products")
    public Result<SearchPageVO<ProductDocument>> searchProductsGet(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "categoryId", required = false) Long categoryId,
            @RequestParam(value = "brandId", required = false) Long brandId,
            @RequestParam(value = "sortType", required = false) Integer sortType,
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "size", defaultValue = "20") Integer size) {
        ProductSearchDTO dto = new ProductSearchDTO();
        dto.setKeyword(keyword);
        dto.setCategoryId(categoryId);
        dto.setBrandId(brandId);
        dto.setSortType(sortType);
        dto.setPage(page);
        dto.setSize(size);
        return Result.success(searchService.searchProducts(dto));
    }

    @GetMapping("/suggest")
    public Result<List<String>> suggest(@RequestParam(value = "keyword", required = false) String keyword) {
        return Result.success(searchService.suggest(keyword));
    }

    @GetMapping("/hot")
    public Result<List<String>> hotWords() {
        return Result.success(searchService.hotWords());
    }

    @PostMapping("/index")
    public Result<Void> index(@RequestBody ProductDocument product) {
        searchService.saveOrUpdateProduct(product);
        return Result.success();
    }

    @DeleteMapping("/index/{productId}")
    public Result<Void> delete(@PathVariable("productId") Long productId) {
        searchService.deleteProduct(productId);
        return Result.success();
    }

    @PostMapping("/import")
    public Result<Integer> importProducts() {
        return Result.success("导入完成", searchService.importProducts());
    }

}

package com.stratos.search.service;

import com.stratos.search.dto.ProductSearchDTO;
import com.stratos.search.dto.SearchPageVO;
import com.stratos.search.entity.ProductDocument;

import java.util.List;

/**
 * 搜索服务
 *
 * @author StratosCommerce
 * @since 1.0.0
 */
public interface SearchService {

    SearchPageVO<ProductDocument> searchProducts(ProductSearchDTO dto);

    List<String> suggest(String keyword);

    List<String> hotWords();

    void saveOrUpdateProduct(ProductDocument product);

    void deleteProduct(Long productId);

    int importProducts();

}

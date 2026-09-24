package com.stratos.search.repository;

import com.stratos.search.entity.ProductDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

/**
 * 商品搜索Repository
 *
 * @author StratosCommerce
 * @since 1.0.0
 */
@Repository
public interface ProductRepository extends ElasticsearchRepository<ProductDocument, Long> {
}

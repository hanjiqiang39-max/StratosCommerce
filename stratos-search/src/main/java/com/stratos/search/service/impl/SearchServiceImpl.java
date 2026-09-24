package com.stratos.search.service.impl;

import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.RangeQuery;
import co.elastic.clients.json.JsonData;
import com.stratos.common.base.PageResult;
import com.stratos.common.constant.CacheConstants;
import com.stratos.common.exception.BusinessException;
import com.stratos.common.result.Result;
import com.stratos.common.result.ResultCode;
import com.stratos.search.dto.ProductIndexDoc;
import com.stratos.search.dto.ProductSearchDTO;
import com.stratos.search.dto.SearchPageVO;
import com.stratos.search.entity.ProductDocument;
import com.stratos.search.feign.ProductFeignClient;
import com.stratos.search.service.SearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.client.elc.NativeQueryBuilder;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 商品全文检索、联想、热词。
 *
 * @author StratosCommerce
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {

    private final ElasticsearchOperations elasticsearchOperations;
    private final ProductFeignClient productFeignClient;
    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public SearchPageVO<ProductDocument> searchProducts(ProductSearchDTO dto) {
        int page = dto.getPage() == null || dto.getPage() < 1 ? 1 : dto.getPage();
        int size = dto.getSize() == null || dto.getSize() < 1 ? 20 : dto.getSize();

        BoolQuery.Builder boolQuery = new BoolQuery.Builder();
        if (StringUtils.hasText(dto.getKeyword())) {
            String keyword = dto.getKeyword().trim();
            boolQuery.must(Query.of(q -> q.multiMatch(m -> m
                    .fields("name", "description", "brandName", "categoryName")
                    .query(keyword)
            )));
            stringRedisTemplate.opsForZSet().incrementScore(CacheConstants.Captcha.SEARCH_HOT, keyword, 1);
        } else {
            boolQuery.must(Query.of(q -> q.matchAll(m -> m)));
        }

        if (dto.getCategoryId() != null) {
            boolQuery.filter(Query.of(q -> q.term(t -> t.field("categoryId").value(dto.getCategoryId()))));
        }
        if (dto.getBrandId() != null) {
            boolQuery.filter(Query.of(q -> q.term(t -> t.field("brandId").value(dto.getBrandId()))));
        }
        if (dto.getMinPrice() != null || dto.getMaxPrice() != null) {
            RangeQuery.Builder rangeQuery = new RangeQuery.Builder().field("price");
            if (dto.getMinPrice() != null) {
                rangeQuery.gte(JsonData.of(dto.getMinPrice()));
            }
            if (dto.getMaxPrice() != null) {
                rangeQuery.lte(JsonData.of(dto.getMaxPrice()));
            }
            boolQuery.filter(Query.of(q -> q.range(rangeQuery.build())));
        }
        boolQuery.filter(Query.of(q -> q.term(t -> t.field("status").value(1))));

        Pageable pageable = PageRequest.of(page - 1, size);
        NativeQueryBuilder queryBuilder = NativeQuery.builder()
                .withQuery(Query.of(q -> q.bool(boolQuery.build())))
                .withPageable(pageable);

        int sortType = dto.getSortType() == null ? 0 : dto.getSortType();
        switch (sortType) {
            case 1 -> queryBuilder.withSort(s -> s.field(f -> f.field("price").order(SortOrder.Asc)));
            case 2 -> queryBuilder.withSort(s -> s.field(f -> f.field("price").order(SortOrder.Desc)));
            case 3 -> queryBuilder.withSort(s -> s.field(f -> f.field("sales").order(SortOrder.Desc)));
            default -> queryBuilder.withSort(s -> s.field(f -> f.field("createTime").order(SortOrder.Desc)));
        }

        SearchHits<ProductDocument> searchHits;
        try {
            searchHits = elasticsearchOperations.search(queryBuilder.build(), ProductDocument.class);
        } catch (Exception ex) {
            throw new BusinessException(ResultCode.SERVICE_UNAVAILABLE, "搜索暂不可用，请确认 Elasticsearch 已启动");
        }
        List<ProductDocument> products = new ArrayList<>();
        for (SearchHit<ProductDocument> hit : searchHits) {
            products.add(hit.getContent());
        }
        return new SearchPageVO<>(products, searchHits.getTotalHits(), page, size);
    }

    @Override
    public List<String> suggest(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return hotWords();
        }
        ProductSearchDTO dto = new ProductSearchDTO();
        dto.setKeyword(keyword.trim());
        dto.setPage(1);
        dto.setSize(8);
        Set<String> names = new LinkedHashSet<>();
        for (ProductDocument doc : searchProducts(dto).getRecords()) {
            if (StringUtils.hasText(doc.getName())) {
                names.add(doc.getName());
            }
        }
        return new ArrayList<>(names);
    }

    @Override
    public List<String> hotWords() {
        Set<String> words = stringRedisTemplate.opsForZSet()
                .reverseRange(CacheConstants.Captcha.SEARCH_HOT, 0, 9);
        return words == null ? List.of() : new ArrayList<>(words);
    }

    @Override
    public void saveOrUpdateProduct(ProductDocument product) {
        try {
            elasticsearchOperations.save(product);
            log.info("商品索引已更新: {}", product.getId());
        } catch (Exception ex) {
            throw new BusinessException(ResultCode.SERVICE_UNAVAILABLE, "搜索索引不可用，请确认 Elasticsearch 已启动");
        }
    }

    @Override
    public void deleteProduct(Long productId) {
        try {
            elasticsearchOperations.delete(String.valueOf(productId), ProductDocument.class);
            log.info("商品索引已删除: {}", productId);
        } catch (Exception ex) {
            throw new BusinessException(ResultCode.SERVICE_UNAVAILABLE, "搜索索引不可用，请确认 Elasticsearch 已启动");
        }
    }

    @Override
    public int importProducts() {
        try {
            Result<List<ProductIndexDoc>> remote = productFeignClient.listIndexDocs();
            if (remote != null && remote.isSuccess() && remote.getData() != null) {
                return saveIndexDocs(remote.getData());
            }
            log.warn("index-docs 返回空，回退商品列表导入");
        } catch (Exception ex) {
            log.warn("index-docs 不可用，回退商品列表导入: {}", ex.getMessage());
        }
        return importFromProductList();
    }

    private int importFromProductList() {
        Result<PageResult<Map<String, Object>>> page = productFeignClient.listProducts(1, 100);
        if (page == null || !page.isSuccess() || page.getData() == null || page.getData().getRecords() == null) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR,
                    page != null ? page.getMessage() : "商品服务不可用，无法导入索引");
        }
        int count = 0;
        for (Map<String, Object> item : page.getData().getRecords()) {
            ProductDocument doc = fromListItem(item);
            if (doc.getId() == null) {
                continue;
            }
            enrichPrice(doc);
            elasticsearchOperations.save(doc);
            count++;
        }
        log.info("从商品列表导入索引完成, count={}", count);
        return count;
    }

    private void enrichPrice(ProductDocument doc) {
        try {
            Result<Map<String, Object>> detail = productFeignClient.getProductDetail(doc.getId());
            if (detail == null || !detail.isSuccess() || detail.getData() == null) {
                return;
            }
            Object minPrice = detail.getData().get("minPrice");
            if (minPrice != null) {
                doc.setPrice(new BigDecimal(minPrice.toString()));
            }
            Object title = detail.getData().get("title");
            if (title != null && !StringUtils.hasText(doc.getName())) {
                doc.setName(title.toString());
            }
        } catch (Exception ex) {
            log.debug("补全商品价格失败 id={}: {}", doc.getId(), ex.getMessage());
        }
    }

    private ProductDocument fromListItem(Map<String, Object> item) {
        ProductDocument doc = new ProductDocument();
        doc.setId(toLong(item.get("id")));
        Object title = item.get("title");
        Object spuName = item.get("spuName");
        doc.setName(title != null ? title.toString() : (spuName != null ? spuName.toString() : null));
        Object sn = item.get("spuCode");
        doc.setSn(sn != null ? sn.toString() : null);
        doc.setCategoryId(toLong(item.get("categoryId")));
        doc.setBrandId(toLong(item.get("brandId")));
        doc.setSales(toInt(item.get("saleCount")));
        Object image = item.get("mainImage");
        doc.setImage(image != null ? image.toString() : null);
        Object desc = item.get("subTitle");
        doc.setDescription(desc != null ? desc.toString() : null);
        doc.setStatus(toInt(item.get("status")));
        if (doc.getStatus() == null) {
            doc.setStatus(1);
        }
        doc.setCreateTime(System.currentTimeMillis());
        return doc;
    }

    private Long toLong(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        return Long.parseLong(value.toString());
    }

    private Integer toInt(Object value) {
        if (value == null) {
            return 0;
        }
        if (value instanceof Number number) {
            return number.intValue();
        }
        return Integer.parseInt(value.toString());
    }

    private int saveIndexDocs(List<ProductIndexDoc> items) {
        int count = 0;
        for (ProductIndexDoc item : items) {
            elasticsearchOperations.save(toDocument(item));
            count++;
        }
        log.info("批量导入商品索引完成, count={}", count);
        return count;
    }

    private ProductDocument toDocument(ProductIndexDoc item) {
        ProductDocument doc = new ProductDocument();
        doc.setId(item.getId());
        doc.setName(item.getName());
        doc.setSn(item.getSn());
        doc.setCategoryId(item.getCategoryId());
        doc.setCategoryName(item.getCategoryName());
        doc.setBrandId(item.getBrandId());
        doc.setBrandName(item.getBrandName());
        doc.setPrice(item.getPrice());
        doc.setSales(item.getSales() == null ? 0 : item.getSales());
        doc.setDescription(item.getDescription());
        doc.setImage(item.getImage());
        doc.setStatus(item.getStatus());
        doc.setCreateTime(item.getCreateTime());
        return doc;
    }

}

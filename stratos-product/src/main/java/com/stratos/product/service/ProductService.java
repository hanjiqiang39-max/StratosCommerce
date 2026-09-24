package com.stratos.product.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.stratos.common.base.PageQuery;
import com.stratos.common.base.PageResult;
import com.stratos.common.exception.BusinessException;
import com.stratos.common.result.ResultCode;
import com.stratos.common.util.SnowflakeIdGenerator;
import com.stratos.product.dto.SaveProductDTO;
import com.stratos.product.entity.ProductBrand;
import com.stratos.product.entity.ProductCategory;
import com.stratos.product.entity.ProductSku;
import com.stratos.product.entity.ProductSpu;
import com.stratos.product.mapper.ProductBrandMapper;
import com.stratos.product.mapper.ProductCategoryMapper;
import com.stratos.product.mapper.ProductSkuMapper;
import com.stratos.product.mapper.ProductSpuMapper;
import com.stratos.product.vo.ProductDetailVO;
import com.stratos.product.vo.ProductIndexVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 商品服务实现类
 *
 * @author StratosCommerce
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductSpuMapper productSpuMapper;
    private final ProductSkuMapper productSkuMapper;
    private final ProductCategoryMapper productCategoryMapper;
    private final ProductBrandMapper productBrandMapper;
    private final SnowflakeIdGenerator snowflakeIdGenerator;
    private final SearchIndexClient searchIndexClient;
    private final InventorySyncClient inventorySyncClient;

    /**
     * 分页查询商品列表
     */
    public PageResult<ProductSpu> getProductList(PageQuery pageQuery, String keyword, Long categoryId, Long brandId) {
        return getProductList(pageQuery, keyword, categoryId, brandId, 1, null);
    }

    public PageResult<ProductSpu> getProductList(PageQuery pageQuery, String keyword, Long categoryId, Long brandId,
                                                 Integer status) {
        return getProductList(pageQuery, keyword, categoryId, brandId, status, null);
    }

    public PageResult<ProductSpu> getProductList(PageQuery pageQuery, String keyword, Long categoryId, Long brandId,
                                                 Integer status, Long shopId) {
        Page<ProductSpu> page = new Page<>(pageQuery.getPageNum(), pageQuery.getPageSize());
        
        LambdaQueryWrapper<ProductSpu> queryWrapper = new LambdaQueryWrapper<>();
        if (status == null) {
            queryWrapper.eq(ProductSpu::getStatus, 1);
        } else if (status >= 0) {
            queryWrapper.eq(ProductSpu::getStatus, status);
        }
        queryWrapper.eq(shopId != null, ProductSpu::getShopId, shopId);
        queryWrapper.eq(categoryId != null, ProductSpu::getCategoryId, categoryId);
        queryWrapper.eq(brandId != null, ProductSpu::getBrandId, brandId);
        
        if (keyword != null && !keyword.trim().isEmpty()) {
            queryWrapper.and(w -> w.like(ProductSpu::getTitle, keyword)
                                   .or()
                                   .like(ProductSpu::getSpuName, keyword));
        }
        
        queryWrapper.orderByDesc(ProductSpu::getSaleCount);
        
        Page<ProductSpu> result = productSpuMapper.selectPage(page, queryWrapper);
        fillMinPrice(result.getRecords());

        return new PageResult<>(result.getRecords(), result.getTotal(),
                pageQuery.getPageNum(), pageQuery.getPageSize());
    }

    /**
     * 根据分类查询商品
     */
    public PageResult<ProductSpu> getProductsByCategory(Long categoryId, PageQuery pageQuery) {
        Page<ProductSpu> page = new Page<>(pageQuery.getPageNum(), pageQuery.getPageSize());
        
        LambdaQueryWrapper<ProductSpu> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ProductSpu::getCategoryId, categoryId)
                    .eq(ProductSpu::getStatus, 1)
                    .orderByDesc(ProductSpu::getSaleCount);
        
        Page<ProductSpu> result = productSpuMapper.selectPage(page, queryWrapper);
        fillMinPrice(result.getRecords());

        return new PageResult<>(result.getRecords(), result.getTotal(),
                pageQuery.getPageNum(), pageQuery.getPageSize());
    }

    private void fillMinPrice(List<ProductSpu> records) {
        if (records == null || records.isEmpty()) {
            return;
        }
        List<Long> ids = records.stream().map(ProductSpu::getId).collect(Collectors.toList());
        List<ProductSku> skus = productSkuMapper.selectList(new LambdaQueryWrapper<ProductSku>()
                .in(ProductSku::getSpuId, ids)
                .eq(ProductSku::getStatus, 1));
        Map<Long, BigDecimal> minMap = new HashMap<>();
        for (ProductSku sku : skus) {
            if (sku.getPrice() == null) {
                continue;
            }
            minMap.merge(sku.getSpuId(), sku.getPrice(), BigDecimal::min);
        }
        for (ProductSpu spu : records) {
            spu.setMinPrice(minMap.get(spu.getId()));
        }
    }

    /**
     * 查询商品详情（包含SKU列表）
     */
    public ProductDetailVO getProductDetail(Long spuId) {
        // 1. 查询SPU
        ProductSpu spu = productSpuMapper.selectById(spuId);
        if (spu == null) {
            throw new BusinessException(ResultCode.PRODUCT_NOT_FOUND);
        }

        // 2. 查询SKU列表
        LambdaQueryWrapper<ProductSku> skuQuery = new LambdaQueryWrapper<>();
        skuQuery.eq(ProductSku::getSpuId, spuId)
                .eq(ProductSku::getStatus, 1)
                .orderByAsc(ProductSku::getSortOrder);
        List<ProductSku> skuList = productSkuMapper.selectList(skuQuery);

        // 3. 组装VO
        ProductDetailVO vo = new ProductDetailVO();
        BeanUtils.copyProperties(spu, vo);

        if (!skuList.isEmpty()) {
            // 计算价格区间
            BigDecimal minPrice = skuList.stream()
                    .map(ProductSku::getPrice)
                    .min(BigDecimal::compareTo)
                    .orElse(BigDecimal.ZERO);
            BigDecimal maxPrice = skuList.stream()
                    .map(ProductSku::getPrice)
                    .max(BigDecimal::compareTo)
                    .orElse(BigDecimal.ZERO);

            vo.setMinPrice(minPrice);
            vo.setMaxPrice(maxPrice);

            // 转换SKU列表
            String title = StringUtils.hasText(spu.getTitle()) ? spu.getTitle() : spu.getSpuName();
            List<ProductDetailVO.SkuItemVO> skuVOList = skuList.stream().map(sku -> {
                ProductDetailVO.SkuItemVO skuVO = new ProductDetailVO.SkuItemVO();
                BeanUtils.copyProperties(sku, skuVO);
                skuVO.setSkuName(displaySkuName(sku.getSkuName(), title));
                return skuVO;
            }).collect(Collectors.toList());

            vo.setSkuList(skuVOList);
        }

        spu.setViewCount((spu.getViewCount() == null ? 0 : spu.getViewCount()) + 1);
        productSpuMapper.updateById(spu);

        return vo;
    }

    /**
     * 根据ID查询SKU
     */
    public ProductSku getSkuById(Long skuId) {
        ProductSku sku = productSkuMapper.selectById(skuId);
        if (sku == null || sku.getStatus() == 0) {
            throw new BusinessException(ResultCode.PRODUCT_NOT_FOUND);
        }
        ProductSpu spu = productSpuMapper.selectById(sku.getSpuId());
        if (spu != null) {
            sku.setShopId(spu.getShopId() != null ? spu.getShopId() : 1L);
            sku.setProductTitle(StringUtils.hasText(spu.getTitle()) ? spu.getTitle() : spu.getSpuName());
        }
        sku.setDisplayName(displaySkuName(sku.getSkuName(), sku.getProductTitle()));
        return sku;
    }

    public ProductSpu requireShopProduct(Long spuId, Long shopId) {
        ProductSpu spu = productSpuMapper.selectById(spuId);
        if (spu == null) {
            throw new BusinessException(ResultCode.PRODUCT_NOT_FOUND);
        }
        Long owner = spu.getShopId() == null ? 1L : spu.getShopId();
        if (shopId != null && !owner.equals(shopId)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "不能操作其他店铺的商品");
        }
        return spu;
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteProduct(Long spuId, Long shopId) {
        requireShopProduct(spuId, shopId);
        productSpuMapper.deleteById(spuId);
        searchIndexClient.delete(spuId);
    }

    public List<ProductSku> listShopSkus(Long shopId) {
        List<ProductSpu> spus = productSpuMapper.selectList(new LambdaQueryWrapper<ProductSpu>()
                .eq(ProductSpu::getShopId, shopId));
        if (spus.isEmpty()) {
            return List.of();
        }
        List<Long> spuIds = spus.stream().map(ProductSpu::getId).toList();
        List<ProductSku> skus = productSkuMapper.selectList(new LambdaQueryWrapper<ProductSku>()
                .in(ProductSku::getSpuId, spuIds)
                .orderByDesc(ProductSku::getUpdateTime));
        decorateSkus(skus);
        return skus;
    }

    public List<ProductSku> listAllSkus() {
        List<ProductSku> skus = productSkuMapper.selectList(new LambdaQueryWrapper<ProductSku>()
                .orderByDesc(ProductSku::getUpdateTime)
                .last("LIMIT 200"));
        decorateSkus(skus);
        return skus;
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateSkuStock(Long skuId, Integer stock) {
        updateSkuStock(skuId, stock, null);
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateSkuStock(Long skuId, Integer stock, Long shopId) {
        if (stock == null || stock < 0) {
            throw new BusinessException(ResultCode.PARAM_VALIDATE_ERROR, "库存不能小于 0");
        }
        ProductSku sku = productSkuMapper.selectById(skuId);
        if (sku == null) {
            throw new BusinessException(ResultCode.PRODUCT_NOT_FOUND, "SKU 不存在");
        }
        if (shopId != null) {
            requireShopProduct(sku.getSpuId(), shopId);
        }
        sku.setStock(stock);
        productSkuMapper.updateById(sku);
        inventorySyncClient.upsert(skuId, stock);
    }

    public List<ProductCategory> getCategoryTree() {
        List<ProductCategory> all = productCategoryMapper.selectList(new LambdaQueryWrapper<ProductCategory>()
                .eq(ProductCategory::getShowStatus, 1)
                .orderByAsc(ProductCategory::getSortOrder));
        return buildTree(all, 0L);
    }

    public List<ProductBrand> listBrands() {
        return productBrandMapper.selectList(new LambdaQueryWrapper<ProductBrand>()
                .eq(ProductBrand::getShowStatus, 1)
                .orderByAsc(ProductBrand::getSortOrder));
    }

    @Transactional(rollbackFor = Exception.class)
    public Long saveProduct(SaveProductDTO dto) {
        ProductSpu spu = dto.getId() == null ? new ProductSpu() : productSpuMapper.selectById(dto.getId());
        if (spu == null) {
            throw new BusinessException(ResultCode.PRODUCT_NOT_FOUND);
        }
        if (spu.getId() == null) {
            spu.setId(snowflakeIdGenerator.nextId());
            spu.setSpuCode("SPU" + spu.getId());
            spu.setSaleCount(0);
            spu.setViewCount(0);
            spu.setFavoriteCount(0);
            spu.setCommentCount(0);
            spu.setPublishStatus(1);
            spu.setAuditStatus(1);
            spu.setVersion(0);
            spu.setShopId(dto.getShopId() != null ? dto.getShopId() : 1L);
        } else if (dto.getShopId() != null && spu.getShopId() != null && !dto.getShopId().equals(spu.getShopId())) {
            throw new BusinessException(ResultCode.FORBIDDEN, "不能修改其他店铺的商品");
        }
        spu.setSpuName(dto.getSpuName());
        spu.setTitle(dto.getTitle());
        spu.setSubTitle(dto.getSubTitle());
        spu.setCategoryId(dto.getCategoryId());
        spu.setBrandId(dto.getBrandId());
        spu.setMainImage(StringUtils.hasText(dto.getMainImage()) ? dto.getMainImage() : "");
        spu.setImageList(dto.getImageList());
        spu.setDetailHtml(dto.getDetailHtml());
        spu.setSellingPoint(dto.getSellingPoint());
        spu.setTags(dto.getTags());
        spu.setStatus(dto.getStatus() == null ? 1 : dto.getStatus());

        if (dto.getId() == null) {
            productSpuMapper.insert(spu);
        } else {
            productSpuMapper.updateById(spu);
        }

        if (dto.getSkuList() != null) {
            for (SaveProductDTO.SkuDTO skuDTO : dto.getSkuList()) {
                ProductSku sku = skuDTO.getId() == null ? new ProductSku() : productSkuMapper.selectById(skuDTO.getId());
                if (sku == null) {
                    sku = new ProductSku();
                }
                boolean insert = sku.getId() == null;
                if (insert) {
                    sku.setId(snowflakeIdGenerator.nextId());
                }
                sku.setSpuId(spu.getId());
                sku.setSkuName(normalizeSkuName(skuDTO.getSkuName(), spu));
                sku.setSkuCode(StringUtils.hasText(skuDTO.getSkuCode()) ? skuDTO.getSkuCode() : "SKU" + sku.getId());
                sku.setSkuImage(skuDTO.getSkuImage());
                sku.setSpecJson(skuDTO.getSpecJson());
                sku.setPrice(skuDTO.getPrice() == null ? BigDecimal.ZERO : skuDTO.getPrice());
                sku.setOriginalPrice(skuDTO.getOriginalPrice());
                sku.setStock(skuDTO.getStock() == null ? 0 : skuDTO.getStock());
                sku.setLockStock(0);
                sku.setStatus(1);
                sku.setVersion(sku.getVersion() == null ? 0 : sku.getVersion());
                if (insert) {
                    productSkuMapper.insert(sku);
                } else {
                    productSkuMapper.updateById(sku);
                }
                inventorySyncClient.upsert(sku.getId(), sku.getStock());
            }
        }
        searchIndexClient.upsert(toIndexVo(spu));
        return spu.getId();
    }

    public List<ProductIndexVO> listIndexDocs() {
        List<ProductSpu> spus = productSpuMapper.selectList(new LambdaQueryWrapper<ProductSpu>()
                .eq(ProductSpu::getStatus, 1));
        List<ProductIndexVO> result = new ArrayList<>();
        for (ProductSpu spu : spus) {
            result.add(toIndexVo(spu));
        }
        return result;
    }

    private void decorateSkus(List<ProductSku> skus) {
        if (skus == null || skus.isEmpty()) {
            return;
        }
        List<Long> spuIds = skus.stream().map(ProductSku::getSpuId).filter(id -> id != null).distinct().toList();
        if (spuIds.isEmpty()) {
            return;
        }
        Map<Long, ProductSpu> spus = productSpuMapper.selectBatchIds(spuIds).stream()
                .collect(Collectors.toMap(ProductSpu::getId, item -> item, (a, b) -> a));
        for (ProductSku sku : skus) {
            ProductSpu spu = spus.get(sku.getSpuId());
            if (spu != null) {
                sku.setShopId(spu.getShopId());
                sku.setProductTitle(StringUtils.hasText(spu.getTitle()) ? spu.getTitle() : spu.getSpuName());
            }
            sku.setDisplayName(displaySkuName(sku.getSkuName(), sku.getProductTitle()));
            if ("默认规格".equals(StringUtils.hasText(sku.getSkuName()) ? sku.getSkuName().trim() : "")
                    && StringUtils.hasText(sku.getProductTitle())) {
                sku.setSkuName(sku.getProductTitle());
                productSkuMapper.updateById(sku);
                sku.setDisplayName(sku.getProductTitle());
            }
        }
    }

    private String normalizeSkuName(String skuName, ProductSpu spu) {
        String title = spu == null ? "" : (StringUtils.hasText(spu.getTitle()) ? spu.getTitle() : spu.getSpuName());
        if (!StringUtils.hasText(skuName) || "默认规格".equals(skuName.trim())) {
            return StringUtils.hasText(title) ? title : "标准款";
        }
        return skuName.trim();
    }

    private String displaySkuName(String skuName, String productTitle) {
        if (!StringUtils.hasText(skuName) || "默认规格".equals(skuName.trim())) {
            return StringUtils.hasText(productTitle) ? productTitle : "标准款";
        }
        if (StringUtils.hasText(productTitle) && !skuName.contains(productTitle) && !productTitle.equals(skuName)) {
            return productTitle + " · " + skuName;
        }
        return skuName;
    }

    private ProductIndexVO toIndexVo(ProductSpu spu) {
        ProductIndexVO vo = new ProductIndexVO();
        vo.setId(spu.getId());
        vo.setName(StringUtils.hasText(spu.getTitle()) ? spu.getTitle() : spu.getSpuName());
        vo.setSn(spu.getSpuCode());
        vo.setCategoryId(spu.getCategoryId());
        vo.setBrandId(spu.getBrandId());
        vo.setSales(spu.getSaleCount() == null ? 0 : spu.getSaleCount());
        vo.setDescription(spu.getSellingPoint());
        vo.setImage(spu.getMainImage());
        vo.setStatus(spu.getStatus());
        if (spu.getCreateTime() != null) {
            vo.setCreateTime(spu.getCreateTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
        } else {
            vo.setCreateTime(System.currentTimeMillis());
        }
        ProductCategory category = productCategoryMapper.selectById(spu.getCategoryId());
        if (category != null) {
            vo.setCategoryName(category.getCategoryName());
        }
        if (spu.getBrandId() != null) {
            ProductBrand brand = productBrandMapper.selectById(spu.getBrandId());
            if (brand != null) {
                vo.setBrandName(brand.getBrandName());
            }
        }
        List<ProductSku> skus = productSkuMapper.selectList(new LambdaQueryWrapper<ProductSku>()
                .eq(ProductSku::getSpuId, spu.getId())
                .eq(ProductSku::getStatus, 1));
        BigDecimal minPrice = skus.stream()
                .map(ProductSku::getPrice)
                .filter(price -> price != null)
                .min(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);
        vo.setPrice(minPrice);
        return vo;
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long spuId, Integer status) {
        ProductSpu spu = productSpuMapper.selectById(spuId);
        if (spu == null) {
            throw new BusinessException(ResultCode.PRODUCT_NOT_FOUND);
        }
        spu.setStatus(status);
        productSpuMapper.updateById(spu);
        if (status != null && status == 1) {
            searchIndexClient.upsert(toIndexVo(spu));
        } else {
            searchIndexClient.delete(spuId);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public Long saveCategory(ProductCategory category) {
        if (category.getId() == null) {
            category.setId(snowflakeIdGenerator.nextId());
            if (category.getParentId() == null) {
                category.setParentId(0L);
            }
            if (category.getCategoryLevel() == null) {
                category.setCategoryLevel(1);
            }
            if (category.getShowStatus() == null) {
                category.setShowStatus(1);
            }
            productCategoryMapper.insert(category);
        } else {
            productCategoryMapper.updateById(category);
        }
        return category.getId();
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteCategory(Long id) {
        Long children = productCategoryMapper.selectCount(new LambdaQueryWrapper<ProductCategory>()
                .eq(ProductCategory::getParentId, id));
        if (children != null && children > 0) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "请先删除子分类");
        }
        productCategoryMapper.deleteById(id);
    }

    @Transactional(rollbackFor = Exception.class)
    public Long saveBrand(ProductBrand brand) {
        if (brand.getId() == null) {
            brand.setId(snowflakeIdGenerator.nextId());
            if (brand.getShowStatus() == null) {
                brand.setShowStatus(1);
            }
            productBrandMapper.insert(brand);
        } else {
            productBrandMapper.updateById(brand);
        }
        return brand.getId();
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteBrand(Long id) {
        productBrandMapper.deleteById(id);
    }

    private List<ProductCategory> buildTree(List<ProductCategory> all, Long parentId) {
        List<ProductCategory> roots = all.stream()
                .filter(item -> parentId.equals(item.getParentId() == null ? 0L : item.getParentId()))
                .collect(Collectors.toList());
        for (ProductCategory node : roots) {
            node.setChildren(buildTree(all, node.getId()));
        }
        return roots;
    }

}

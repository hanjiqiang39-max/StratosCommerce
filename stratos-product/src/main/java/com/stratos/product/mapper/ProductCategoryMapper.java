package com.stratos.product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.stratos.product.entity.ProductCategory;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品分类 Mapper
 *
 * @author StratosCommerce
 * @since 1.0.0
 */
@Mapper
public interface ProductCategoryMapper extends BaseMapper<ProductCategory> {
}

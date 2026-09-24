package com.stratos.product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.stratos.product.entity.ProductSku;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品SKU Mapper
 *
 * @author StratosCommerce
 * @since 1.0.0
 */
@Mapper
public interface ProductSkuMapper extends BaseMapper<ProductSku> {
}

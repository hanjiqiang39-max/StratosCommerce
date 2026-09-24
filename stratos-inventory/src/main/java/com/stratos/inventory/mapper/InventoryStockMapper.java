package com.stratos.inventory.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.stratos.inventory.entity.InventoryStock;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 库存Mapper
 *
 * @author StratosCommerce
 * @since 1.0.0
 */
@Mapper
public interface InventoryStockMapper extends BaseMapper<InventoryStock> {

    /**
     * 扣减库存（乐观锁�?
     */
    int deductStock(@Param("skuId") Long skuId, @Param("quantity") Integer quantity, @Param("version") Integer version);

}

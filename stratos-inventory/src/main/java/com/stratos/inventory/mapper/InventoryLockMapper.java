package com.stratos.inventory.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.stratos.inventory.entity.InventoryLock;
import org.apache.ibatis.annotations.Mapper;

/**
 * 库存锁定Mapper
 *
 * @author StratosCommerce
 * @since 1.0.0
 */
@Mapper
public interface InventoryLockMapper extends BaseMapper<InventoryLock> {
}

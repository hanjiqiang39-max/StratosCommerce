package com.stratos.inventory.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.stratos.inventory.entity.InventoryLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 库存日志Mapper
 *
 * @author StratosCommerce
 * @since 1.0.0
 */
@Mapper
public interface InventoryLogMapper extends BaseMapper<InventoryLog> {
}

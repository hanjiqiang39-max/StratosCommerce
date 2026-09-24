package com.stratos.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.stratos.order.entity.OrderItem;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单明细表 Mapper
 *
 * @author StratosCommerce
 * @since 1.0.0
 */
@Mapper
public interface OrderItemMapper extends BaseMapper<OrderItem> {
}

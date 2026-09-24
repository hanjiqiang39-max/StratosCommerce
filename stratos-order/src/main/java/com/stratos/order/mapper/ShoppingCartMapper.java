package com.stratos.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.stratos.order.entity.ShoppingCart;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ShoppingCartMapper extends BaseMapper<ShoppingCart> {
}

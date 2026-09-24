package com.stratos.payment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.stratos.payment.entity.PayOrder;
import org.apache.ibatis.annotations.Mapper;

/**
 * 支付单Mapper
 *
 * @author StratosCommerce
 * @since 1.0.0
 */
@Mapper
public interface PayOrderMapper extends BaseMapper<PayOrder> {
}

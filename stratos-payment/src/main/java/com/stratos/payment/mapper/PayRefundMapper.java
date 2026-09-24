package com.stratos.payment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.stratos.payment.entity.PayRefund;
import org.apache.ibatis.annotations.Mapper;

/**
 * 退款单Mapper
 *
 * @author StratosCommerce
 * @since 1.0.0
 */
@Mapper
public interface PayRefundMapper extends BaseMapper<PayRefund> {
}

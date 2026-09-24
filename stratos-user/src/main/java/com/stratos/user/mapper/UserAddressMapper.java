package com.stratos.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.stratos.user.entity.UserAddress;
import org.apache.ibatis.annotations.Mapper;

/**
 * 收货地址Mapper接口
 *
 * @author StratosCommerce
 * @since 1.0.0
 */
@Mapper
public interface UserAddressMapper extends BaseMapper<UserAddress> {

}

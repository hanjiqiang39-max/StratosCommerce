package com.stratos.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.stratos.user.entity.UserAuth;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户认证Mapper接口
 *
 * @author StratosCommerce
 * @since 1.0.0
 */
@Mapper
public interface UserAuthMapper extends BaseMapper<UserAuth> {

}

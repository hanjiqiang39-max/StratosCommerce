package com.stratos.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.stratos.user.entity.UserLoginLog;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserLoginLogMapper extends BaseMapper<UserLoginLog> {
}

package com.stratos.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.stratos.system.entity.SysAdmin;
import org.apache.ibatis.annotations.Mapper;

/**
 * 管理员Mapper
 *
 * @author StratosCommerce
 * @since 1.0.0
 */
@Mapper
public interface SysAdminMapper extends BaseMapper<SysAdmin> {
}

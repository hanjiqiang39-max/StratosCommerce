package com.stratos.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.stratos.system.entity.SysMenu;
import org.apache.ibatis.annotations.Mapper;

/**
 * 菜单Mapper
 *
 * @author StratosCommerce
 * @since 1.0.0
 */
@Mapper
public interface SysMenuMapper extends BaseMapper<SysMenu> {
}

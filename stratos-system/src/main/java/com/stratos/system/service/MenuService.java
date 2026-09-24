package com.stratos.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.stratos.system.entity.SysMenu;

import java.util.List;

/**
 * 菜单服务接口
 *
 * @author StratosCommerce
 * @since 1.0.0
 */
public interface MenuService extends IService<SysMenu> {

    /**
     * 查询菜单�?
     */
    List<SysMenu> listMenuTree();

    /**
     * 查询管理员的菜单�?
     */
    List<SysMenu> getMenuTreeByAdminId(Long adminId);

}

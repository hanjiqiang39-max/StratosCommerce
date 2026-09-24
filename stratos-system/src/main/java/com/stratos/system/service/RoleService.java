package com.stratos.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.stratos.system.entity.SysRole;

import java.util.List;

/**
 * 角色服务接口
 *
 * @author StratosCommerce
 * @since 1.0.0
 */
public interface RoleService extends IService<SysRole> {

    /**
     * 查询管理员的角色列表
     */
    List<SysRole> getRolesByAdminId(Long adminId);

    /**
     * 分配角色
     */
    void assignRoles(Long adminId, List<Long> roleIds);

    /**
     * 查询角色的菜单ID列表
     */
    List<Long> getMenuIdsByRoleId(Long roleId);

    /**
     * 分配菜单权限
     */
    void assignMenus(Long roleId, List<Long> menuIds);

}

package com.stratos.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.stratos.common.util.SnowflakeIdGenerator;
import com.stratos.system.entity.SysAdminRole;
import com.stratos.system.entity.SysRole;
import com.stratos.system.entity.SysRoleMenu;
import com.stratos.system.mapper.SysAdminRoleMapper;
import com.stratos.system.mapper.SysRoleMapper;
import com.stratos.system.mapper.SysRoleMenuMapper;
import com.stratos.system.service.RoleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 角色服务实现
 *
 * @author StratosCommerce
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRole> implements RoleService {

    private final SysAdminRoleMapper adminRoleMapper;
    private final SysRoleMenuMapper roleMenuMapper;
    private final SnowflakeIdGenerator snowflakeIdGenerator;

    @Override
    public List<SysRole> getRolesByAdminId(Long adminId) {
        List<SysAdminRole> relations = adminRoleMapper.selectList(
                new LambdaQueryWrapper<SysAdminRole>().eq(SysAdminRole::getAdminId, adminId));
        if (relations.isEmpty()) {
            return List.of();
        }
        List<Long> roleIds = relations.stream().map(SysAdminRole::getRoleId).toList();
        return listByIds(roleIds);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignRoles(Long adminId, List<Long> roleIds) {
        adminRoleMapper.delete(new LambdaQueryWrapper<SysAdminRole>().eq(SysAdminRole::getAdminId, adminId));
        if (roleIds == null || roleIds.isEmpty()) {
            return;
        }
        for (Long roleId : roleIds) {
            SysAdminRole relation = new SysAdminRole();
            relation.setId(snowflakeIdGenerator.nextId());
            relation.setAdminId(adminId);
            relation.setRoleId(roleId);
            adminRoleMapper.insert(relation);
        }
        log.info("为管理员 {} 分配角色: {}", adminId, roleIds);
    }

    @Override
    public List<Long> getMenuIdsByRoleId(Long roleId) {
        return roleMenuMapper.selectList(new LambdaQueryWrapper<SysRoleMenu>().eq(SysRoleMenu::getRoleId, roleId))
                .stream()
                .map(SysRoleMenu::getMenuId)
                .toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignMenus(Long roleId, List<Long> menuIds) {
        roleMenuMapper.delete(new LambdaQueryWrapper<SysRoleMenu>().eq(SysRoleMenu::getRoleId, roleId));
        if (menuIds == null || menuIds.isEmpty()) {
            return;
        }
        for (Long menuId : menuIds) {
            SysRoleMenu relation = new SysRoleMenu();
            relation.setId(snowflakeIdGenerator.nextId());
            relation.setRoleId(roleId);
            relation.setMenuId(menuId);
            roleMenuMapper.insert(relation);
        }
        log.info("为角色 {} 分配菜单: {}", roleId, menuIds);
    }
}

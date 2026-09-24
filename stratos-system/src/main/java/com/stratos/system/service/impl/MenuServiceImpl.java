package com.stratos.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.stratos.system.entity.SysAdmin;
import com.stratos.system.entity.SysAdminRole;
import com.stratos.system.entity.SysMenu;
import com.stratos.system.entity.SysRoleMenu;
import com.stratos.system.mapper.SysAdminMapper;
import com.stratos.system.mapper.SysAdminRoleMapper;
import com.stratos.system.mapper.SysMenuMapper;
import com.stratos.system.mapper.SysRoleMenuMapper;
import com.stratos.system.service.MenuService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 菜单服务实现
 *
 * @author StratosCommerce
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MenuServiceImpl extends ServiceImpl<SysMenuMapper, SysMenu> implements MenuService {

    private final SysAdminMapper sysAdminMapper;
    private final SysAdminRoleMapper sysAdminRoleMapper;
    private final SysRoleMenuMapper sysRoleMenuMapper;

    @Override
    public List<SysMenu> listMenuTree() {
        List<SysMenu> all = lambdaQuery().eq(SysMenu::getStatus, 1).orderByAsc(SysMenu::getSortOrder).list();
        return buildTree(all, 0L);
    }

    @Override
    public List<SysMenu> getMenuTreeByAdminId(Long adminId) {
        List<SysMenu> all = lambdaQuery().eq(SysMenu::getStatus, 1).orderByAsc(SysMenu::getSortOrder).list();
        SysAdmin admin = sysAdminMapper.selectById(adminId);
        if (admin != null && admin.getIsSuperAdmin() != null && admin.getIsSuperAdmin() == 1) {
            return buildTree(all, 0L);
        }
        List<SysAdminRole> relations = sysAdminRoleMapper.selectList(
                new LambdaQueryWrapper<SysAdminRole>().eq(SysAdminRole::getAdminId, adminId));
        if (relations.isEmpty()) {
            return new ArrayList<>();
        }
        List<Long> roleIds = relations.stream().map(SysAdminRole::getRoleId).toList();
        Set<Long> assigned = sysRoleMenuMapper.selectList(
                        new LambdaQueryWrapper<SysRoleMenu>().in(SysRoleMenu::getRoleId, roleIds))
                .stream()
                .map(SysRoleMenu::getMenuId)
                .collect(Collectors.toSet());
        if (assigned.isEmpty()) {
            return new ArrayList<>();
        }
        Map<Long, SysMenu> byId = all.stream().collect(Collectors.toMap(SysMenu::getId, Function.identity(), (a, b) -> a));
        Set<Long> visible = new HashSet<>(assigned);
        for (Long menuId : assigned) {
            SysMenu current = byId.get(menuId);
            while (current != null && current.getParentId() != null && current.getParentId() != 0L) {
                visible.add(current.getParentId());
                current = byId.get(current.getParentId());
            }
        }
        List<SysMenu> filtered = all.stream().filter(item -> visible.contains(item.getId())).toList();
        return buildTree(filtered, 0L);
    }

    private List<SysMenu> buildTree(List<SysMenu> all, Long parentId) {
        List<SysMenu> children = all.stream()
                .filter(m -> m.getParentId().equals(parentId))
                .collect(Collectors.toList());
        
        for (SysMenu menu : children) {
            List<SysMenu> subChildren = buildTree(all, menu.getId());
            menu.setChildren(subChildren);
        }
        
        return children.isEmpty() ? new ArrayList<>() : children;
    }

}

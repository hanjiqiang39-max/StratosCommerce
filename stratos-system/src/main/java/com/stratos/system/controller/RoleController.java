package com.stratos.system.controller;

import com.stratos.common.result.Result;
import com.stratos.system.dto.AssignMenusDTO;
import com.stratos.system.dto.AssignRolesDTO;
import com.stratos.system.entity.SysRole;
import com.stratos.system.service.RoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "角色管理")
@RestController
@RequestMapping("/role")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @Operation(summary = "角色列表")
    @GetMapping("/list")
    public Result<List<SysRole>> list() {
        return Result.success(roleService.list());
    }

    @Operation(summary = "保存角色")
    @PostMapping
    public Result<Boolean> save(@RequestBody SysRole role) {
        return Result.success(roleService.saveOrUpdate(role));
    }

    @Operation(summary = "删除角色")
    @DeleteMapping("/{roleId}")
    public Result<Boolean> delete(@PathVariable Long roleId) {
        return Result.success(roleService.removeById(roleId));
    }

    @Operation(summary = "管理员角色")
    @GetMapping("/admin/{adminId}")
    public Result<List<SysRole>> byAdmin(@PathVariable Long adminId) {
        return Result.success(roleService.getRolesByAdminId(adminId));
    }

    @Operation(summary = "分配角色")
    @PostMapping("/assign")
    public Result<Void> assignRoles(@Valid @RequestBody AssignRolesDTO dto) {
        roleService.assignRoles(dto.getAdminId(), dto.getRoleIds());
        return Result.success();
    }

    @Operation(summary = "角色菜单ID")
    @GetMapping("/{roleId}/menus")
    public Result<List<Long>> menus(@PathVariable Long roleId) {
        return Result.success(roleService.getMenuIdsByRoleId(roleId));
    }

    @Operation(summary = "分配菜单")
    @PostMapping("/assign-menus")
    public Result<Void> assignMenus(@Valid @RequestBody AssignMenusDTO dto) {
        roleService.assignMenus(dto.getRoleId(), dto.getMenuIds());
        return Result.success();
    }
}

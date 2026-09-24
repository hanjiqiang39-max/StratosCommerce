package com.stratos.system.controller;

import com.stratos.common.result.Result;
import com.stratos.system.entity.SysMenu;
import com.stratos.system.service.MenuService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 菜单控制器
 *
 * @author StratosCommerce
 * @since 1.0.0
 */
@Tag(name = "菜单管理")
@RestController
@RequestMapping("/menu")
@RequiredArgsConstructor
public class MenuController {

    private final MenuService menuService;

    @Operation(summary = "查询菜单树")
    @GetMapping("/tree")
    public Result<List<SysMenu>> listMenuTree() {
        return Result.success(menuService.listMenuTree());
    }

    @Operation(summary = "查询管理员菜单树")
    @GetMapping("/tree/{adminId}")
    public Result<List<SysMenu>> getMenuTreeByAdminId(@PathVariable Long adminId) {
        return Result.success(menuService.getMenuTreeByAdminId(adminId));
    }

    @Operation(summary = "菜单列表")
    @GetMapping("/list")
    public Result<List<SysMenu>> list() {
        return Result.success(menuService.list());
    }

    @Operation(summary = "保存菜单")
    @PostMapping
    public Result<Boolean> save(@RequestBody SysMenu menu) {
        return Result.success(menuService.saveOrUpdate(menu));
    }

    @Operation(summary = "删除菜单")
    @DeleteMapping("/{menuId}")
    public Result<Boolean> delete(@PathVariable Long menuId) {
        return Result.success(menuService.removeById(menuId));
    }

}

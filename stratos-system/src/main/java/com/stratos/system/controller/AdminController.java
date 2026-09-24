package com.stratos.system.controller;

import com.stratos.common.base.PageQuery;
import com.stratos.common.base.PageResult;
import com.stratos.common.result.Result;
import com.stratos.system.dto.AdminLoginDTO;
import com.stratos.system.dto.AdminSaveDTO;
import com.stratos.system.dto.ChangePasswordDTO;
import com.stratos.system.entity.SysAdmin;
import com.stratos.system.service.AdminService;
import com.stratos.system.vo.AdminLoginVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "管理员管理")
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @Operation(summary = "管理员登录")
    @PostMapping("/login")
    public Result<AdminLoginVO> login(@Valid @RequestBody AdminLoginDTO dto) {
        return Result.success("登录成功", adminService.login(dto.getUsername(), dto.getPassword()));
    }

    @Operation(summary = "管理员列表")
    @GetMapping("/list")
    public Result<PageResult<SysAdmin>> list(PageQuery pageQuery) {
        return Result.success(adminService.listAdmins(pageQuery));
    }

    @Operation(summary = "查询管理员详情")
    @GetMapping("/{adminId}")
    public Result<SysAdmin> getById(@PathVariable("adminId") Long adminId) {
        SysAdmin admin = adminService.getById(adminId);
        if (admin != null) {
            admin.setPassword(null);
        }
        return Result.success(admin);
    }

    @Operation(summary = "查询管理员角色编码")
    @GetMapping("/roles/{adminId}")
    public Result<List<String>> getRoleCodes(@PathVariable("adminId") Long adminId) {
        return Result.success(adminService.getRoleCodes(adminId));
    }

    @Operation(summary = "查询管理员权限")
    @GetMapping("/permissions/{adminId}")
    public Result<List<String>> getPermissions(@PathVariable("adminId") Long adminId) {
        return Result.success(adminService.getPermissions(adminId));
    }

    @Operation(summary = "新增或更新管理员")
    @PostMapping
    public Result<Long> save(@Valid @RequestBody AdminSaveDTO dto) {
        return Result.success("保存成功", adminService.saveAdmin(dto));
    }

    @Operation(summary = "修改密码")
    @PutMapping("/password")
    public Result<Void> changePassword(@Valid @RequestBody ChangePasswordDTO dto) {
        adminService.changePassword(dto.getAdminId(), dto.getOldPassword(), dto.getNewPassword());
        return Result.success();
    }

    @Operation(summary = "删除管理员")
    @DeleteMapping("/{adminId}")
    public Result<Void> delete(@PathVariable Long adminId) {
        adminService.deleteAdmin(adminId);
        return Result.success();
    }

}

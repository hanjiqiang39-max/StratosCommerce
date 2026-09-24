package com.stratos.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.stratos.common.base.PageQuery;
import com.stratos.common.base.PageResult;
import com.stratos.system.dto.AdminSaveDTO;
import com.stratos.system.entity.SysAdmin;
import com.stratos.system.vo.AdminLoginVO;

import java.util.List;

public interface AdminService extends IService<SysAdmin> {

    AdminLoginVO login(String username, String password);

    SysAdmin getByUsername(String username);

    List<String> getRoleCodes(Long adminId);

    List<String> getPermissions(Long adminId);

    PageResult<SysAdmin> listAdmins(PageQuery pageQuery);

    Long saveAdmin(AdminSaveDTO dto);

    void changePassword(Long adminId, String oldPassword, String newPassword);

    void deleteAdmin(Long adminId);
}

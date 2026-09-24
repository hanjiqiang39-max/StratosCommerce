package com.stratos.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("sys_admin_role")
public class SysAdminRole {

    private Long id;

    private Long adminId;

    private Long roleId;
}

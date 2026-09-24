package com.stratos.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.stratos.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 管理员实�?
 *
 * @author StratosCommerce
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_admin")
public class SysAdmin extends BaseEntity {

    private Long id;

    private String username;

    private String password;

    private String realName;

    private String nickname;

    private String avatar;

    private String phoneEncrypted;

    private String phoneHash;

    private String email;

    private Integer status;

    private Integer isSuperAdmin;

    private Long deptId;

    private LocalDateTime lastLoginTime;

    private String lastLoginIp;

    private String remark;

}

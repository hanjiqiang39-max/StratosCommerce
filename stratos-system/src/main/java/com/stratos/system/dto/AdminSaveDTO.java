package com.stratos.system.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AdminSaveDTO {

    private Long id;

    @NotBlank(message = "用户名不能为空")
    private String username;

    private String password;

    private String nickname;

    private String realName;

    private String email;

    private Integer status;

    private Integer isSuperAdmin;

    private String remark;
}

package com.stratos.user.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;

/**
 * 用户登录请求DTO
 *
 * @author StratosCommerce
 * @since 1.0.0
 */
@Data
public class UserLoginDTO {

    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;

}

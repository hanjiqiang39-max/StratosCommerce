package com.stratos.system.vo;

import lombok.Data;

import java.util.List;

@Data
public class AdminLoginVO {

    private Long adminId;

    private String username;

    private String nickname;

    private String realName;

    private Integer isSuperAdmin;

    private String token;

    private List<String> roles;

    private List<String> permissions;
}

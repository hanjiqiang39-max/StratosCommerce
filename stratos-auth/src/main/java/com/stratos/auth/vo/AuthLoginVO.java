package com.stratos.auth.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AuthLoginVO {

    private String token;

    private String refreshToken;

    private LocalDateTime expireTime;

    private Long userId;

    private String username;

}

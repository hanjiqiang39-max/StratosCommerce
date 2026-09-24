package com.stratos.user.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 登录响应
 *
 * @author StratosCommerce
 * @since 1.0.0
 */
@Data
public class LoginVO {

    private String token;

    private String refreshToken;

    private LocalDateTime expireTime;

    private Long userId;

    private String username;

    private String nickname;

}

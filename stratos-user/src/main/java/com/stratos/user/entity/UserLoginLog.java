package com.stratos.user.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户登录日志。表无审计/软删字段。
 *
 * @author StratosCommerce
 * @since 1.0.0
 */
@Data
@TableName("user_login_log")
public class UserLoginLog {

    private Long id;

    private Long userId;

    private Integer loginType;

    private String loginIp;

    private String loginLocation;

    private String userAgent;

    private Integer deviceType;

    private Integer loginStatus;

    private String failReason;

    private LocalDateTime loginTime;

}

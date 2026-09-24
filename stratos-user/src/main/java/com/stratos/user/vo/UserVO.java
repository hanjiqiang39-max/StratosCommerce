package com.stratos.user.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 用户信息响应VO
 *
 * @author StratosCommerce
 * @since 1.0.0
 */
@Data
public class UserVO {

    private Long id;

    private String username;

    private String nickname;

    private String avatar;

    private Integer gender;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthday;

    private String phone;

    private String email;

    private Integer status;

    private Long levelId;

    private Integer points;

    private Integer growthValue;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastLoginTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

}

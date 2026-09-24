package com.stratos.user.dto;

import lombok.Data;

import java.time.LocalDate;

/**
 * 更新个人资料
 *
 * @author StratosCommerce
 * @since 1.0.0
 */
@Data
public class UserProfileDTO {

    private String nickname;

    private String avatar;

    private Integer gender;

    private LocalDate birthday;

}

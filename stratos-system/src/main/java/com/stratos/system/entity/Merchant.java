package com.stratos.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.stratos.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 商家账号
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("merchant")
public class Merchant extends BaseEntity {

    private Long id;

    private String username;

    private String password;

    private String realName;

    private String nickname;

    private String phone;

    private String email;

    /**
     * 0=待审核，1=正常，2=禁用
     */
    private Integer status;

    /**
     * 0=待审核，1=通过，2=拒绝
     */
    private Integer auditStatus;

    private String auditRemark;

    private LocalDateTime lastLoginTime;

    private String lastLoginIp;

    private String remark;
}

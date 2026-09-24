package com.stratos.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.stratos.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 用户认证表实体
 *
 * @author StratosCommerce
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "user_auth", excludeProperty = {"createBy", "updateBy"})
public class UserAuth extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long userId;

    /**
     * 认证类型：1=密码，2=手机验证码，3=微信，4=支付宝
     */
    private Integer authType;

    private String identifier;

    private String credential;

    private Integer verified;

    private LocalDateTime expireTime;

}

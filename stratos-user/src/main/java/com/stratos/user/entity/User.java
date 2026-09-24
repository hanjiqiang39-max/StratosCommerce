package com.stratos.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import com.stratos.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 用户主表实体
 *
 * @author StratosCommerce
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("`user`")
public class User extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String username;

    private String nickname;

    private String avatar;

    private Integer gender;

    private LocalDate birthday;

    private String phoneEncrypted;

    private String phoneHash;

    private String emailEncrypted;

    private String emailHash;

    private Integer status;

    private Long levelId;

    private Integer points;

    private Integer growthValue;

    private Integer registerSource;

    private String registerIp;

    private LocalDateTime lastLoginTime;

    private String lastLoginIp;

    @Version
    private Integer version;

}

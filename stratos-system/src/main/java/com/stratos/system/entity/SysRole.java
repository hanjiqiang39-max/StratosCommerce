package com.stratos.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.stratos.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 角色实体
 *
 * @author StratosCommerce
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_role")
public class SysRole extends BaseEntity {

    private Long id;

    private String roleName;

    private String roleCode;

    private Integer dataScope;

    private String deptIds;

    private Integer sortOrder;

    private Integer status;

    private String remark;

}

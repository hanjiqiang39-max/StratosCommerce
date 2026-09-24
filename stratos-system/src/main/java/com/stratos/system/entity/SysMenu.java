package com.stratos.system.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.stratos.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

/**
 * 菜单实体
 *
 * @author StratosCommerce
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_menu")
public class SysMenu extends BaseEntity {

    private Long id;

    private Long parentId;

    private String menuName;

    private Integer menuType;

    private String path;

    private String component;

    private String permission;

    private String icon;

    private Integer sortOrder;

    private Integer visible;

    private Integer status;

    private String remark;

    @TableField(exist = false)
    private List<SysMenu> children = new ArrayList<>();

}

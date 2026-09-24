package com.stratos.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.stratos.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 字典数据实体
 *
 * @author StratosCommerce
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_dict_data")
public class SysDictData extends BaseEntity {

    private Long id;

    private String dictType;

    private String dictLabel;

    private String dictValue;

    private Integer dictSort;

    private String cssClass;

    private String listClass;

    private Integer isDefault;

    private Integer status;

    private String remark;

}

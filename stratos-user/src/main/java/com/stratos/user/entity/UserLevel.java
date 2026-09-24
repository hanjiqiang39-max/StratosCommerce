package com.stratos.user.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.stratos.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 会员等级
 *
 * @author StratosCommerce
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "user_level", excludeProperty = {"createBy", "updateBy"})
public class UserLevel extends BaseEntity {

    private Long id;

    private String levelName;

    private String levelCode;

    private Integer growthValueMin;

    private Integer growthValueMax;

    private BigDecimal discountRate;

    private BigDecimal freeShippingThreshold;

    private String privilegeDesc;

    private Integer sortOrder;

    private Integer status;

}

package com.stratos.logistics.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.stratos.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 物流公司实体
 *
 * @author StratosCommerce
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "logistics_company", excludeProperty = {"createBy", "updateBy"})
public class LogisticsCompany extends BaseEntity {

    private Long id;

    private String companyCode;

    private String companyName;

    private String companyLogo;

    private String contactPhone;

    private String website;

    private Integer sortOrder;

    private Integer status;

}

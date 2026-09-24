package com.stratos.logistics.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.stratos.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("logistics_freight_template")
public class LogisticsFreightTemplate extends BaseEntity {

    private Long id;

    private String templateName;

    /**
     * 1=按件数，2=按重量，3=按体积
     */
    private Integer chargeType;

    private Integer isFreeShipping;

    private BigDecimal freeShippingAmount;

    private BigDecimal defaultFirstUnit;

    private BigDecimal defaultFirstFee;

    private BigDecimal defaultContinueUnit;

    private BigDecimal defaultContinueFee;

    private Integer status;
}

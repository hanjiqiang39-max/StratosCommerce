package com.stratos.logistics.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

@Data
@TableName("logistics_freight_region")
public class LogisticsFreightRegion {

    private Long id;

    private Long templateId;

    private Integer regionType;

    private String regionCodes;

    private BigDecimal firstUnit;

    private BigDecimal firstFee;

    private BigDecimal continueUnit;

    private BigDecimal continueFee;
}

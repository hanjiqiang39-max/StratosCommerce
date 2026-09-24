package com.stratos.logistics.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.stratos.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 物流轨迹实体
 *
 * @author StratosCommerce
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "logistics_trace", excludeProperty = {"updateTime", "createBy", "updateBy", "isDeleted"})
public class LogisticsTrace extends BaseEntity {

    private Long id;

    private Long orderId;

    private String orderNo;

    private String logisticsCompanyCode;

    private String logisticsNo;

    private LocalDateTime traceTime;

    private String traceStatus;

    private String traceDesc;

    private String traceLocation;

    private String operator;

}

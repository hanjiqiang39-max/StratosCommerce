package com.stratos.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.stratos.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 操作日志实体
 *
 * @author StratosCommerce
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sys_operation_log", excludeProperty = {"updateTime", "createBy", "updateBy", "isDeleted"})
public class SysOperationLog extends BaseEntity {

    private Long id;

    private String module;

    private Integer businessType;

    private String method;

    private String requestMethod;

    private Integer operatorType;

    private Long operatorId;

    private String operatorName;

    private String deptName;

    private String requestUrl;

    private String requestIp;

    private String requestLocation;

    private String requestParam;

    private String responseResult;

    private Integer status;

    private String errorMsg;

    private Integer costTime;

}

package com.stratos.order.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 售后审核
 */
@Data
public class RefundAuditDTO {

    @NotNull(message = "售后单ID不能为空")
    private Long refundId;

    private Long shopId;

    /**
     * true=通过并发起退款，false=驳回
     */
    @NotNull(message = "审核结果不能为空")
    private Boolean approved;

    private String auditRemark;
}

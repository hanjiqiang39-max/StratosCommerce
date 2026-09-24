package com.stratos.system.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MerchantAuditDTO {

    @NotNull(message = "商家ID不能为空")
    private Long merchantId;

    /**
     * true=通过，false=拒绝
     */
    @NotNull(message = "审核结果不能为空")
    private Boolean approved;

    private String auditRemark;
}

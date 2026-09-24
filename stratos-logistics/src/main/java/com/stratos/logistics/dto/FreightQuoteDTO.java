package com.stratos.logistics.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 运费试算
 */
@Data
public class FreightQuoteDTO {

    /**
     * 模板ID，不传默认 1（全国包邮）
     */
    private Long templateId;

    private String province;

    /**
     * 件数，按件计费用
     */
    private Integer quantity;

    /**
     * 重量 kg，按重计费用
     */
    private BigDecimal weight;

    /**
     * 订单商品金额，用于满额包邮
     */
    private BigDecimal orderAmount;
}

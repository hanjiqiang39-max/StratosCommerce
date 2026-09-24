package com.stratos.promotion.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class SeckillResultVO {

    private Long recordId;

    private Long seckillId;

    private Long skuId;

    private Long userId;

    private Long orderId;

    private String orderNo;

    private Integer quantity;

    private BigDecimal seckillPrice;

    /**
     * 0=待下单/待支付，1=已支付，2=已取消
     */
    private Integer status;

    /**
     * true=本次未重新扣库存，复用已有抢购记录
     */
    private Boolean reused;
}

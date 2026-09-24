package com.stratos.common.mq;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 订单已创建
 *
 * @author StratosCommerce
 * @since 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderCreatedMessage implements Serializable {

    private Long orderId;
    private String orderNo;
    private Long userId;
    private BigDecimal payAmount;

}

package com.stratos.common.mq;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 订单已取消
 *
 * @author StratosCommerce
 * @since 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderCancelledMessage implements Serializable {

    private Long orderId;
    private String orderNo;
    private Long userId;

}

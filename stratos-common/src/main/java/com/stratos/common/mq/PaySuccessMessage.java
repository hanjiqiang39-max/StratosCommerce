package com.stratos.common.mq;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 支付成功
 *
 * @author StratosCommerce
 * @since 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaySuccessMessage implements Serializable {

    private Long orderId;
    private String orderNo;
    private String payNo;
    private String thirdPartyTradeNo;
    private Long userId;

}

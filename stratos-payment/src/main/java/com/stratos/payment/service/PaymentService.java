package com.stratos.payment.service;

import com.stratos.payment.dto.CreatePayDTO;
import com.stratos.payment.dto.RefundDTO;
import com.stratos.payment.entity.PayOrder;
import com.stratos.payment.vo.PayOrderVO;

import java.util.Map;

/**
 * 支付服务接口
 *
 * @author StratosCommerce
 * @since 1.0.0
 */
public interface PaymentService {

    PayOrderVO createPayOrder(CreatePayDTO dto);

    PayOrder queryByPayNo(String payNo);

    PayOrder queryLatestByOrderNo(String orderNo);

    java.util.List<PayOrder> adminList(Integer status);

    void paySuccess(String payNo, String thirdPartyTradeNo);

    void closePayOrder(String payNo);

    void refund(RefundDTO dto);

    void simulatePay(String payNo);

    /**
     * 支付宝异步通知，验签通过返回 success
     */
    String handleAlipayNotify(Map<String, String> params);

    /**
     * 同步回跳：本地收不到异步通知时，用回跳参数把支付单点亮。
     */
    void confirmReturn(String outTradeNo, String tradeNo);

}

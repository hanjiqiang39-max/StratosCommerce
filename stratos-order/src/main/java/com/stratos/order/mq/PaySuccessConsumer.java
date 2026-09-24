package com.stratos.order.mq;

import com.stratos.common.constant.MqConstants;
import com.stratos.common.mq.PaySuccessMessage;
import com.stratos.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

/**
 * 支付成功后把订单置为已支付
 *
 * @author StratosCommerce
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
@RocketMQMessageListener(topic = MqConstants.TOPIC_PAY_SUCCESS, consumerGroup = MqConstants.GID_ORDER)
public class PaySuccessConsumer implements RocketMQListener<PaySuccessMessage> {

    private final OrderService orderService;

    @Override
    public void onMessage(PaySuccessMessage message) {
        log.info("收到支付成功消息, orderNo={}, payNo={}", message.getOrderNo(), message.getPayNo());
        orderService.paySuccess(message.getOrderId());
    }

}

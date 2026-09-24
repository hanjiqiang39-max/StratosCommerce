package com.stratos.order.mq;

import com.stratos.common.constant.MqConstants;
import com.stratos.common.mq.OrderCancelledMessage;
import com.stratos.common.mq.OrderCreatedMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.stereotype.Component;

/**
 * 订单领域事件
 *
 * @author StratosCommerce
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventProducer {

    private final RocketMQTemplate rocketMQTemplate;

    public void sendCreated(OrderCreatedMessage message) {
        rocketMQTemplate.convertAndSend(MqConstants.TOPIC_ORDER_CREATED, message);
        log.info("已发送订单创建消息, orderNo={}", message.getOrderNo());
    }

    public void sendCancelled(OrderCancelledMessage message) {
        rocketMQTemplate.convertAndSend(MqConstants.TOPIC_ORDER_CANCELLED, message);
        log.info("已发送订单取消消息, orderNo={}", message.getOrderNo());
    }

}

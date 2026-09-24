package com.stratos.payment.mq;

import com.stratos.common.constant.MqConstants;
import com.stratos.common.mq.PaySuccessMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.stereotype.Component;

/**
 * 支付成功消息
 *
 * @author StratosCommerce
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PaySuccessProducer {

    private final RocketMQTemplate rocketMQTemplate;

    public void send(PaySuccessMessage message) {
        rocketMQTemplate.convertAndSend(MqConstants.TOPIC_PAY_SUCCESS, message);
        log.info("已发送支付成功消息, payNo={}, orderNo={}", message.getPayNo(), message.getOrderNo());
    }

}

package com.stratos.notification.mq;

import com.stratos.common.constant.MqConstants;
import com.stratos.common.mq.PaySuccessMessage;
import com.stratos.notification.dto.SendNotificationDTO;
import com.stratos.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

/**
 * 支付成功后写站内信。
 *
 * @author StratosCommerce
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
@RocketMQMessageListener(topic = MqConstants.TOPIC_PAY_SUCCESS, consumerGroup = MqConstants.GID_NOTIFICATION)
public class PaySuccessNotifyListener implements RocketMQListener<PaySuccessMessage> {

    private final NotificationService notificationService;

    @Override
    public void onMessage(PaySuccessMessage message) {
        SendNotificationDTO dto = new SendNotificationDTO();
        dto.setUserId(message.getUserId());
        dto.setType(2);
        dto.setTitle("支付成功");
        dto.setContent("订单 " + message.getOrderNo() + " 已支付成功");
        dto.setLinkUrl("/order/" + message.getOrderId());
        notificationService.sendNotification(dto);
        log.info("支付成功站内信已写入 userId={} orderNo={}", message.getUserId(), message.getOrderNo());
    }

}

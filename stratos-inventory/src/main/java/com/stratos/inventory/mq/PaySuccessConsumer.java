package com.stratos.inventory.mq;

import com.stratos.common.constant.MqConstants;
import com.stratos.common.mq.PaySuccessMessage;
import com.stratos.inventory.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

/**
 * 支付成功后把锁定库存转为实扣
 *
 * @author StratosCommerce
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
@RocketMQMessageListener(topic = MqConstants.TOPIC_PAY_SUCCESS, consumerGroup = MqConstants.GID_INVENTORY)
public class PaySuccessConsumer implements RocketMQListener<PaySuccessMessage> {

    private final InventoryService inventoryService;

    @Override
    public void onMessage(PaySuccessMessage message) {
        log.info("收到支付成功，扣减锁定库存, orderId={}", message.getOrderId());
        inventoryService.deductStock(message.getOrderId());
    }

}

package com.stratos.inventory.mq;

import com.stratos.common.constant.MqConstants;
import com.stratos.common.mq.OrderCancelledMessage;
import com.stratos.inventory.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

/**
 * 订单取消后释放锁定库存（补偿 Feign 解锁失败）
 *
 * @author StratosCommerce
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
@RocketMQMessageListener(topic = MqConstants.TOPIC_ORDER_CANCELLED, consumerGroup = MqConstants.GID_INVENTORY + "_CANCEL")
public class OrderCancelledConsumer implements RocketMQListener<OrderCancelledMessage> {

    private final InventoryService inventoryService;

    @Override
    public void onMessage(OrderCancelledMessage message) {
        log.info("收到订单取消，释放库存, orderId={}", message.getOrderId());
        inventoryService.unlockStock(message.getOrderId());
    }

}

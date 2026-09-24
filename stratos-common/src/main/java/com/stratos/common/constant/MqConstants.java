package com.stratos.common.constant;

/**
 * RocketMQ Topic / ConsumerGroup
 *
 * @author StratosCommerce
 * @since 1.0.0
 */
public interface MqConstants {

    String TOPIC_ORDER_CREATED = "TOPIC_ORDER_CREATED";
    String TOPIC_ORDER_CANCELLED = "TOPIC_ORDER_CANCELLED";
    String TOPIC_PAY_SUCCESS = "TOPIC_PAY_SUCCESS";

    String GID_ORDER = "GID_ORDER";
    String GID_INVENTORY = "GID_INVENTORY";
    String GID_PAYMENT = "GID_PAYMENT";
    String GID_NOTIFICATION = "GID_NOTIFICATION";
    String GID_SEARCH = "GID_SEARCH";

}

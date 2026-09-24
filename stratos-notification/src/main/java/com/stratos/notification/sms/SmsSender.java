package com.stratos.notification.sms;

/**
 * 短信通道
 *
 * @author StratosCommerce
 * @since 1.0.0
 */
public interface SmsSender {

    void send(String phone, String content, String templateParamJson);

}

package com.stratos.notification.sms;

import lombok.extern.slf4j.Slf4j;

/**
 * 开发环境：不调用运营商，验证码看控制台。
 *
 * @author StratosCommerce
 * @since 1.0.0
 */
@Slf4j
public class MockSmsSender implements SmsSender {

    @Override
    public void send(String phone, String content, String templateParamJson) {
        log.info("[MOCK SMS] phone={} content={} param={}", phone, content, templateParamJson);
    }

}

package com.stratos.notification.sms;

import com.aliyun.dysmsapi20170525.Client;
import com.aliyun.dysmsapi20170525.models.SendSmsRequest;
import com.aliyun.dysmsapi20170525.models.SendSmsResponse;
import com.aliyun.teaopenapi.models.Config;
import com.stratos.common.exception.BusinessException;
import com.stratos.common.result.ResultCode;
import com.stratos.notification.config.SmsProperties;
import lombok.extern.slf4j.Slf4j;

/**
 * 阿里云短信
 *
 * @author StratosCommerce
 * @since 1.0.0
 */
@Slf4j
public class AliyunSmsSender implements SmsSender {

    private final SmsProperties properties;
    private final Client client;

    public AliyunSmsSender(SmsProperties properties) {
        this.properties = properties;
        try {
            Config config = new Config()
                    .setAccessKeyId(properties.getAccessKeyId())
                    .setAccessKeySecret(properties.getAccessKeySecret())
                    .setEndpoint("dysmsapi.aliyuncs.com");
            this.client = new Client(config);
        } catch (Exception e) {
            throw new IllegalStateException("初始化阿里云短信客户端失败", e);
        }
    }

    @Override
    public void send(String phone, String content, String templateParamJson) {
        try {
            SendSmsRequest request = new SendSmsRequest()
                    .setPhoneNumbers(phone)
                    .setSignName(properties.getSignName())
                    .setTemplateCode(properties.getTemplateCode())
                    .setTemplateParam(templateParamJson);
            SendSmsResponse response = client.sendSms(request);
            if (response == null || response.getBody() == null || !"OK".equals(response.getBody().getCode())) {
                String msg = response == null || response.getBody() == null
                        ? "无响应" : response.getBody().getMessage();
                throw new BusinessException(ResultCode.SMS_SEND_FAILED, "短信发送失败: " + msg);
            }
            log.info("阿里云短信已发送 phone={}", phone);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("阿里云短信发送异常 phone={}", phone, e);
            throw new BusinessException(ResultCode.SMS_SEND_FAILED, "短信发送失败: " + e.getMessage());
        }
    }

}

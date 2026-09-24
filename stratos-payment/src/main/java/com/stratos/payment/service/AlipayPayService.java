package com.stratos.payment.service;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.request.AlipayTradeRefundRequest;
import com.alipay.api.response.AlipayTradeRefundResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stratos.common.exception.BusinessException;
import com.stratos.common.result.ResultCode;
import com.stratos.payment.config.AlipayProperties;
import com.stratos.payment.entity.PayOrder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * 支付宝电脑网站支付 / 退款 / 验签
 *
 * @author StratosCommerce
 * @since 1.0.0
 */
@Slf4j
@Service
public class AlipayPayService {

    private final AlipayProperties properties;
    private final ObjectMapper objectMapper;
    private final AlipayClient alipayClient;

    public AlipayPayService(AlipayProperties properties,
                            ObjectMapper objectMapper,
                            ObjectProvider<AlipayClient> alipayClientProvider) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.alipayClient = alipayClientProvider.getIfAvailable();
    }

    public boolean isReady() {
        return alipayClient != null
                && StringUtils.hasText(properties.getPrivateKey())
                && StringUtils.hasText(properties.getAlipayPublicKey());
    }

    public String buildPagePayForm(PayOrder payOrder) {
        ensureReady();
        try {
            AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
            request.setNotifyUrl(properties.getNotifyUrl());
            request.setReturnUrl(properties.getReturnUrl());

            Map<String, Object> biz = new HashMap<>();
            biz.put("out_trade_no", payOrder.getOutTradeNo());
            biz.put("total_amount", payOrder.getPayAmount().toPlainString());
            biz.put("subject", StringUtils.hasText(payOrder.getSubject()) ? payOrder.getSubject() : "StratosCommerce订单");
            biz.put("body", payOrder.getBody());
            biz.put("product_code", "FAST_INSTANT_TRADE_PAY");
            biz.put("timeout_express", "30m");
            request.setBizContent(objectMapper.writeValueAsString(biz));

            // 沙箱网关对 page.pay 的 POST 常返回 404，官方建议 GET 生成收银台链接
            return alipayClient.pageExecute(request, "GET").getBody();
        } catch (Exception e) {
            log.error("生成支付宝支付表单失败, payNo={}", payOrder.getPayNo(), e);
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "生成支付宝支付表单失败: " + e.getMessage());
        }
    }

    public void refund(String outTradeNo, String refundNo, BigDecimal refundAmount, String reason) {
        ensureReady();
        try {
            AlipayTradeRefundRequest request = new AlipayTradeRefundRequest();
            Map<String, Object> biz = new HashMap<>();
            biz.put("out_trade_no", outTradeNo);
            biz.put("out_request_no", refundNo);
            biz.put("refund_amount", refundAmount.toPlainString());
            if (StringUtils.hasText(reason)) {
                biz.put("refund_reason", reason);
            }
            request.setBizContent(objectMapper.writeValueAsString(biz));
            AlipayTradeRefundResponse response = alipayClient.execute(request);
            if (response == null || !response.isSuccess()) {
                String msg = response == null ? "无响应" : response.getSubMsg();
                throw new BusinessException(ResultCode.BUSINESS_ERROR, "支付宝退款失败: " + msg);
            }
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("支付宝退款失败, outTradeNo={}", outTradeNo, e);
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "支付宝退款失败: " + e.getMessage());
        }
    }

    public boolean verifyNotify(Map<String, String> params) {
        if (!isReady()) {
            return false;
        }
        try {
            return AlipaySignature.rsaCheckV1(
                    params,
                    properties.getAlipayPublicKey(),
                    properties.getCharset(),
                    properties.getSignType()
            );
        } catch (AlipayApiException e) {
            log.error("支付宝验签失败", e);
            return false;
        }
    }

    private void ensureReady() {
        if (!isReady()) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR,
                    "支付宝密钥未配置：请在 application-local.yml 或环境变量 ALIPAY_PRIVATE_KEY / ALIPAY_PUBLIC_KEY 中填写");
        }
    }

}

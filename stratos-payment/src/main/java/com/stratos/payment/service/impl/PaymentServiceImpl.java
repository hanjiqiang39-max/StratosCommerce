package com.stratos.payment.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.stratos.common.exception.BusinessException;
import com.stratos.common.mq.PaySuccessMessage;
import com.stratos.common.result.ResultCode;
import com.stratos.payment.dto.CreatePayDTO;
import com.stratos.payment.dto.RefundDTO;
import com.stratos.payment.entity.PayOrder;
import com.stratos.payment.entity.PayRefund;
import com.stratos.payment.mapper.PayOrderMapper;
import com.stratos.payment.mapper.PayRefundMapper;
import com.stratos.payment.mq.PaySuccessProducer;
import com.stratos.payment.service.AlipayPayService;
import com.stratos.payment.service.PaymentService;
import com.stratos.payment.vo.PayOrderVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 支付服务实现
 *
 * @author StratosCommerce
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl extends ServiceImpl<PayOrderMapper, PayOrder> implements PaymentService {

    private static final int CHANNEL_ALIPAY = 1;

    private final PayRefundMapper payRefundMapper;
    private final AlipayPayService alipayPayService;
    private final PaySuccessProducer paySuccessProducer;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${stratos.order.base-url:http://localhost:8084}")
    private String orderBaseUrl;

    @Value("${stratos.notification.base-url:http://localhost:8092}")
    private String notificationBaseUrl;

    @Value("${stratos.promotion.base-url:http://localhost:8086}")
    private String promotionBaseUrl;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PayOrderVO createPayOrder(CreatePayDTO dto) {
        assertGroupPayable(dto.getOrderId());
        PayOrder payOrder = new PayOrder();
        payOrder.setPayNo(generatePayNo());
        payOrder.setOutTradeNo(UUID.randomUUID().toString().replace("-", ""));
        payOrder.setOrderId(dto.getOrderId());
        payOrder.setOrderNo(dto.getOrderNo());
        payOrder.setUserId(dto.getUserId());
        payOrder.setPayChannel(dto.getPayChannel());
        payOrder.setPayType(1);
        payOrder.setPayAmount(dto.getPayAmount());
        payOrder.setCurrency("CNY");
        payOrder.setStatus(0);
        payOrder.setExpireTime(LocalDateTime.now().plusMinutes(30));
        payOrder.setSubject(dto.getSubject());
        payOrder.setBody(dto.getBody());
        payOrder.setNotifyCount(0);
        save(payOrder);

        PayOrderVO vo = toVo(payOrder);
        if (CHANNEL_ALIPAY == dto.getPayChannel()) {
            String form = alipayPayService.buildPagePayForm(payOrder);
            payOrder.setStatus(1);
            updateById(payOrder);
            vo.setStatus(1);
            vo.setPayForm(form);
        }
        return vo;
    }

    @Override
    public PayOrder queryByPayNo(String payNo) {
        return lambdaQuery().eq(PayOrder::getPayNo, payNo).one();
    }

    @Override
    public java.util.List<PayOrder> adminList(Integer status) {
        return lambdaQuery()
                .eq(status != null, PayOrder::getStatus, status)
                .orderByDesc(PayOrder::getCreateTime)
                .last("LIMIT 200")
                .list();
    }

    @Override
    public PayOrder queryLatestByOrderNo(String orderNo) {
        return lambdaQuery()
                .eq(PayOrder::getOrderNo, orderNo)
                .orderByDesc(PayOrder::getCreateTime)
                .last("LIMIT 1")
                .one();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void paySuccess(String payNo, String thirdPartyTradeNo) {
        PayOrder payOrder = queryByPayNo(payNo);
        if (payOrder == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "支付单不存在");
        }
        markPaidAndNotify(payOrder, thirdPartyTradeNo);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void closePayOrder(String payNo) {
        PayOrder payOrder = queryByPayNo(payNo);
        if (payOrder == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "支付单不存在");
        }
        if (payOrder.getStatus() != 0 && payOrder.getStatus() != 1) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "支付单状态不允许关闭");
        }
        payOrder.setStatus(4);
        updateById(payOrder);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void refund(RefundDTO dto) {
        PayOrder payOrder = queryByPayNo(dto.getPayNo());
        if (payOrder == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "支付单不存在");
        }
        if (payOrder.getStatus() != 2) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "支付单未支付成功");
        }
        if (dto.getRefundAmount().compareTo(payOrder.getPayAmount()) > 0) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "退款金额超过支付金额");
        }

        PayRefund refund = new PayRefund();
        refund.setRefundNo(generateRefundNo());
        refund.setOutRefundNo(UUID.randomUUID().toString().replace("-", ""));
        refund.setPayId(payOrder.getId());
        refund.setPayNo(payOrder.getPayNo());
        refund.setOrderId(payOrder.getOrderId());
        refund.setOrderNo(payOrder.getOrderNo());
        refund.setUserId(payOrder.getUserId());
        refund.setRefundChannel(payOrder.getPayChannel());
        refund.setRefundAmount(dto.getRefundAmount());
        refund.setRefundReason(dto.getRefundReason());
        refund.setStatus(0);
        refund.setNotifyCount(0);

        if (CHANNEL_ALIPAY == payOrder.getPayChannel()) {
            alipayPayService.refund(payOrder.getOutTradeNo(), refund.getOutRefundNo(),
                    dto.getRefundAmount(), dto.getRefundReason());
        }

        refund.setStatus(2);
        refund.setRefundTime(LocalDateTime.now());
        payRefundMapper.insert(refund);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void simulatePay(String payNo) {
        PayOrder payOrder = queryByPayNo(payNo);
        if (payOrder != null) {
            assertGroupPayable(payOrder.getOrderId());
        }
        paySuccess(payNo, "MOCK" + System.currentTimeMillis());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String handleAlipayNotify(Map<String, String> params) {
        if (!alipayPayService.verifyNotify(params)) {
            log.warn("支付宝回调验签失败, params={}", params);
            return "failure";
        }
        String tradeStatus = params.get("trade_status");
        if (!"TRADE_SUCCESS".equals(tradeStatus) && !"TRADE_FINISHED".equals(tradeStatus)) {
            return "success";
        }
        String outTradeNo = params.get("out_trade_no");
        String tradeNo = params.get("trade_no");
        PayOrder payOrder = lambdaQuery().eq(PayOrder::getOutTradeNo, outTradeNo).one();
        if (payOrder == null) {
            log.warn("支付宝回调找不到支付单, outTradeNo={}", outTradeNo);
            return "failure";
        }
        if (StringUtils.hasText(params.get("buyer_id"))) {
            payOrder.setThirdPartyUserId(params.get("buyer_id"));
        }
        markPaidAndNotify(payOrder, tradeNo);
        return "success";
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmReturn(String outTradeNo, String tradeNo) {
        if (!StringUtils.hasText(outTradeNo)) {
            return;
        }
        PayOrder payOrder = lambdaQuery().eq(PayOrder::getOutTradeNo, outTradeNo).one();
        if (payOrder == null) {
            log.warn("支付宝回跳找不到支付单, outTradeNo={}", outTradeNo);
            return;
        }
        markPaidAndNotify(payOrder, StringUtils.hasText(tradeNo) ? tradeNo : "RETURN");
    }

    private void markPaidAndNotify(PayOrder payOrder, String thirdPartyTradeNo) {
        boolean alreadyPaid = payOrder.getStatus() != null && payOrder.getStatus() == 2;
        if (!alreadyPaid) {
            payOrder.setStatus(2);
            payOrder.setPayTime(LocalDateTime.now());
            payOrder.setThirdPartyTradeNo(thirdPartyTradeNo);
            payOrder.setNotifyCount((payOrder.getNotifyCount() == null ? 0 : payOrder.getNotifyCount()) + 1);
            payOrder.setLastNotifyTime(LocalDateTime.now());
            updateById(payOrder);
        }
        paySuccessProducer.send(new PaySuccessMessage(
                payOrder.getOrderId(),
                payOrder.getOrderNo(),
                payOrder.getPayNo(),
                thirdPartyTradeNo,
                payOrder.getUserId()
        ));
        if (payOrder.getOrderId() != null) {
            try {
                restTemplate.postForEntity(
                        orderBaseUrl + "/order/" + payOrder.getOrderId() + "/pay-success",
                        null,
                        String.class);
            } catch (Exception ex) {
                log.warn("同步通知订单已支付失败 orderId={}: {}", payOrder.getOrderId(), ex.getMessage());
            }
        }
        if (payOrder.getUserId() != null) {
            try {
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                Map<String, Object> payload = new HashMap<>();
                payload.put("userId", payOrder.getUserId());
                payload.put("type", 2);
                payload.put("title", "支付成功");
                payload.put("content", "订单 " + payOrder.getOrderNo() + " 已支付成功");
                payload.put("linkUrl", "/order/" + payOrder.getOrderId());
                restTemplate.postForEntity(notificationBaseUrl + "/notification/send",
                        new HttpEntity<>(payload, headers), String.class);
            } catch (Exception ex) {
                log.warn("同步写入支付站内信失败 userId={}: {}", payOrder.getUserId(), ex.getMessage());
            }
        }
    }

    private void assertGroupPayable(Long orderId) {
        if (orderId == null) {
            return;
        }
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> body = restTemplate.getForObject(
                    promotionBaseUrl + "/group/payable?orderId=" + orderId, Map.class);
            if (body != null && body.get("data") != null && Boolean.FALSE.equals(body.get("data"))) {
                throw new BusinessException(ResultCode.BUSINESS_ERROR, "拼团未成团，暂不可支付");
            }
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            log.warn("校验拼团可支付失败 orderId={}: {}", orderId, ex.getMessage());
        }
    }

    private PayOrderVO toVo(PayOrder payOrder) {
        PayOrderVO vo = new PayOrderVO();
        vo.setPayNo(payOrder.getPayNo());
        vo.setOutTradeNo(payOrder.getOutTradeNo());
        vo.setStatus(payOrder.getStatus());
        vo.setPayAmount(payOrder.getPayAmount());
        return vo;
    }

    private String generatePayNo() {
        return "PAY" + System.currentTimeMillis() + (int) (Math.random() * 1000);
    }

    private String generateRefundNo() {
        return "REF" + System.currentTimeMillis() + (int) (Math.random() * 1000);
    }

}

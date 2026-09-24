package com.stratos.payment.controller;

import com.stratos.common.result.Result;
import com.stratos.payment.dto.CreatePayDTO;
import com.stratos.payment.dto.RefundDTO;
import com.stratos.payment.entity.PayOrder;
import com.stratos.payment.service.AlipayPayService;
import com.stratos.payment.service.PaymentService;
import com.stratos.payment.vo.PayOrderVO;
import com.stratos.payment.config.AlipayProperties;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 支付控制器
 *
 * @author StratosCommerce
 * @since 1.0.0
 */
@RestController
@RequestMapping("/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final AlipayPayService alipayPayService;
    private final AlipayProperties alipayProperties;

    @GetMapping("/health")
    public Result<Map<String, Object>> health() {
        Map<String, Object> data = new HashMap<>();
        data.put("service", "Payment Service is running");
        data.put("alipayReady", alipayPayService.isReady());
        return Result.success(data);
    }

    @PostMapping("/create")
    public Result<PayOrderVO> createPayOrder(@Valid @RequestBody CreatePayDTO dto) {
        return Result.success(paymentService.createPayOrder(dto));
    }

    @GetMapping("/query/{payNo}")
    public Result<PayOrder> queryPayOrder(@PathVariable String payNo) {
        return Result.success(paymentService.queryByPayNo(payNo));
    }

    @GetMapping("/order/{orderNo}")
    public Result<PayOrder> queryByOrderNo(@PathVariable("orderNo") String orderNo) {
        return Result.success(paymentService.queryLatestByOrderNo(orderNo));
    }

    @GetMapping("/admin/list")
    public Result<java.util.List<PayOrder>> adminList(
            @RequestParam(value = "status", required = false) Integer status) {
        return Result.success(paymentService.adminList(status));
    }

    @PostMapping("/close/{payNo}")
    public Result<Void> closePayOrder(@PathVariable String payNo) {
        paymentService.closePayOrder(payNo);
        return Result.success();
    }

    @PostMapping("/refund")
    public Result<Void> refund(@Valid @RequestBody RefundDTO dto) {
        paymentService.refund(dto);
        return Result.success();
    }

    /**
     * 支付宝异步通知（必须返回纯文本 success / failure）
     */
    @PostMapping("/alipay/notify")
    public String alipayNotify(HttpServletRequest request) {
        return paymentService.handleAlipayNotify(toParamMap(request));
    }

    /**
     * 支付宝同步跳转。本地收不到异步通知，这里点亮支付单并回到商城订单页。
     */
    @GetMapping("/alipay/return")
    public void alipayReturn(@RequestParam(value = "out_trade_no", required = false) String out_trade_no,
                             @RequestParam(value = "trade_no", required = false) String trade_no,
                             HttpServletResponse response) throws IOException {
        paymentService.confirmReturn(out_trade_no, trade_no);
        String target = StringUtils.hasText(alipayProperties.getShopReturnUrl())
                ? alipayProperties.getShopReturnUrl()
                : "http://localhost:5173/orders";
        response.sendRedirect(target + (target.contains("?") ? "&" : "?") + "paid=1");
    }

    @PostMapping("/callback/{payNo}")
    public String payCallback(@PathVariable("payNo") String payNo,
                             @RequestParam("thirdPartyTradeNo") String thirdPartyTradeNo) {
        paymentService.paySuccess(payNo, thirdPartyTradeNo);
        return "success";
    }

    @PostMapping("/simulate/{payNo}")
    public Result<Void> simulatePay(@PathVariable String payNo) {
        paymentService.simulatePay(payNo);
        return Result.<Void>success("模拟支付成功", null);
    }

    private Map<String, String> toParamMap(HttpServletRequest request) {
        Map<String, String> params = new HashMap<>();
        request.getParameterMap().forEach((key, values) -> {
            if (values != null && values.length > 0) {
                params.put(key, String.join(",", values));
            }
        });
        return params;
    }

}

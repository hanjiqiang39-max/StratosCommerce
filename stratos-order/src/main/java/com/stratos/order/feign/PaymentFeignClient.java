package com.stratos.order.feign;

import com.stratos.common.result.Result;
import com.stratos.order.dto.PaymentRefundCommand;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient(name = "stratos-payment", path = "/payment")
public interface PaymentFeignClient {

    @GetMapping("/order/{orderNo}")
    Result<Map<String, Object>> queryByOrderNo(@PathVariable("orderNo") String orderNo);

    @PostMapping("/refund")
    Result<Void> refund(@RequestBody PaymentRefundCommand command);
}

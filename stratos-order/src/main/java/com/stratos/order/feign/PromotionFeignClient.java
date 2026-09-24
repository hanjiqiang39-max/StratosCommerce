package com.stratos.order.feign;

import com.stratos.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.Map;

@FeignClient(name = "stratos-promotion")
public interface PromotionFeignClient {

    @PostMapping("/full-discount/calculate")
    Result<Map<String, Object>> calculateFullDiscount(@RequestBody Map<String, Object> payload);

    @GetMapping("/group/activity/{activityId}")
    Result<Map<String, Object>> getGroupActivity(@PathVariable("activityId") Long activityId);

    @PostMapping("/group/bind-order")
    Result<Void> bindGroupOrder(@RequestBody Map<String, Object> payload);

    @GetMapping("/seckill/detail/{seckillId}")
    Result<Map<String, Object>> getSeckill(@PathVariable("seckillId") Long seckillId);

    @PostMapping("/seckill/bind-order")
    Result<Void> bindSeckillOrder(@RequestBody Map<String, Object> payload);

    @GetMapping("/coupon/quote")
    Result<BigDecimal> quoteCoupon(@RequestParam("userCouponId") Long userCouponId,
                                   @RequestParam("userId") Long userId,
                                   @RequestParam("amount") BigDecimal amount);

    @PostMapping("/coupon/use")
    Result<Void> useCoupon(@RequestParam("userCouponId") Long userCouponId,
                           @RequestParam("orderId") Long orderId,
                           @RequestParam("orderNo") String orderNo);
}

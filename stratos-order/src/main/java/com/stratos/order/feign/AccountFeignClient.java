package com.stratos.order.feign;

import com.stratos.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@FeignClient(name = "stratos-user", contextId = "accountFeignClient", path = "/user")
public interface AccountFeignClient {

    @GetMapping("/{userId}/points")
    Result<Map<String, Object>> getPoints(@PathVariable("userId") Long userId);

    @PostMapping("/points/deduct")
    Result<Void> deductPoints(@RequestParam("userId") Long userId,
                              @RequestParam("points") Integer points,
                              @RequestParam("orderId") Long orderId);
}

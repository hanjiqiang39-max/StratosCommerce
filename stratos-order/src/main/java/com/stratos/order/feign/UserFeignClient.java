package com.stratos.order.feign;

import com.stratos.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.Map;

@FeignClient(name = "stratos-user", contextId = "userAddressFeignClient", path = "/address")
public interface UserFeignClient {

    @GetMapping("/{userId}/{addressId}")
    Result<Map<String, Object>> getAddress(@PathVariable("userId") Long userId,
                                           @PathVariable("addressId") Long addressId);

    @GetMapping("/list/{userId}")
    Result<List<Map<String, Object>>> listAddress(@PathVariable("userId") Long userId);
}

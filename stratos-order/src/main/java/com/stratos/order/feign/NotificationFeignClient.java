package com.stratos.order.feign;

import com.stratos.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient(name = "stratos-notification", path = "/notification")
public interface NotificationFeignClient {

    @PostMapping("/send")
    Result<Void> send(@RequestBody Map<String, Object> payload);
}

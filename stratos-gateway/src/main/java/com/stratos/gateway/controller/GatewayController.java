package com.stratos.gateway.controller;

import com.stratos.common.result.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 网关控制器
 *
 * @author StratosCommerce
 * @since 1.0.0
 */
@RestController
@RequestMapping("/gateway")
public class GatewayController {

    @GetMapping("/health")
    public Result<String> health() {
        return Result.success("Gateway is running");
    }

}

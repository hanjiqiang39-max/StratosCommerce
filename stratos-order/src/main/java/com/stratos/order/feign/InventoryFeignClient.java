package com.stratos.order.feign;

import com.stratos.common.result.Result;
import com.stratos.order.dto.LockStockCommand;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 库存服务
 *
 * @author StratosCommerce
 * @since 1.0.0
 */
@FeignClient(name = "stratos-inventory", path = "/inventory")
public interface InventoryFeignClient {

    @PostMapping("/lock")
    Result<Void> lockStock(@RequestBody LockStockCommand command);

    @PostMapping("/unlock/{orderId}")
    Result<Void> unlockStock(@PathVariable("orderId") Long orderId);

}

package com.stratos.order.feign;

import com.stratos.common.result.Result;
import com.stratos.order.dto.CreateShipmentCommand;
import com.stratos.order.dto.FreightQuoteCommand;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.math.BigDecimal;

@FeignClient(name = "stratos-logistics", path = "/logistics")
public interface LogisticsFeignClient {

    @PostMapping("/freight")
    Result<BigDecimal> quoteFreight(@RequestBody FreightQuoteCommand command);

    @PostMapping("/ship")
    Result<String> ship(@RequestBody CreateShipmentCommand command);
}

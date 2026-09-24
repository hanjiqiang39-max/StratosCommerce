package com.stratos.order.job;

import com.stratos.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 待支付订单超时关闭（默认 30 分钟）。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UnpaidOrderCloser {

    private final OrderService orderService;

    @Scheduled(fixedDelay = 60000)
    public void closeExpired() {
        orderService.closeExpiredUnpaidOrders();
    }
}

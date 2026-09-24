package com.stratos.gateway.filter;

import org.slf4j.MDC;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * 网关注入 / 透传 traceId，下游可用 X-Trace-Id 关联日志。
 *
 * @author StratosCommerce
 * @since 1.0.0
 */
@Component
public class TraceIdFilter implements GlobalFilter, Ordered {

    public static final String TRACE_HEADER = "X-Trace-Id";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String traceId = exchange.getRequest().getHeaders().getFirst(TRACE_HEADER);
        if (traceId == null || traceId.isBlank()) {
            traceId = UUID.randomUUID().toString().replace("-", "");
        }
        String resolved = traceId;
        MDC.put("traceId", resolved);
        ServerHttpRequest mutated = exchange.getRequest().mutate()
                .header(TRACE_HEADER, resolved)
                .build();
        return chain.filter(exchange.mutate().request(mutated).build())
                .doFinally(signal -> MDC.remove("traceId"));
    }

    @Override
    public int getOrder() {
        return -200;
    }
}

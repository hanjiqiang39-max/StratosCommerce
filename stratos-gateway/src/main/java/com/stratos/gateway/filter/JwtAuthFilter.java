package com.stratos.gateway.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.stratos.common.result.Result;
import com.stratos.common.result.ResultCode;
import com.stratos.common.util.JwtUtil;
import io.jsonwebtoken.Claims;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 网关 JWT：白名单放行，其余 /api/** 校验 Bearer；管理路径要求 userType=admin。
 *
 * @author StratosCommerce
 * @since 1.0.0
 */
@Component
public class JwtAuthFilter implements GlobalFilter, Ordered {

    private static final AntPathMatcher MATCHER = new AntPathMatcher();
    private static final ObjectMapper MAPPER = new ObjectMapper().registerModule(new JavaTimeModule());

    private static final List<String> WHITELIST = List.of(
            "/auth/**",
            "/api/auth/**",
            "/api/user/register",
            "/api/user/login",
            "/api/user/sms/**",
            "/api/user/oauth/**",
            "/api/user/auth/options",
            "/api/admin/login",
            "/api/merchant/login",
            "/api/merchant/sms/**",
            "/api/merchant/register",
            "/api/merchant/shop/public/**",
            "/api/search/**",
            "/api/product/list",
            "/api/product/categories",
            "/api/product/brand/list",
            "/api/product/index-docs",
            "/api/product/health",
            "/api/banner/list",
            "/api/notice/list",
            "/api/notice/*",
            "/api/comment/product/**",
            "/api/files/**",
            "/api/seckill/list",
            "/api/seckill/detail/**",
            "/api/seckill/health",
            "/api/full-discount/list",
            "/api/full-discount/calculate",
            "/api/group/list",
            "/api/group/activity/**",
            "/api/coupon/list",
            "/api/payment/alipay/**",
            "/api/payment/health",
            "/actuator/**",
            "/**/health"
    );

    private static final List<String> PUBLIC_GET_PREFIXES = List.of(
            "/api/product/",
            "/api/banner/",
            "/api/notice/",
            "/api/comment/product/",
            "/api/files/",
            "/api/seckill/",
            "/api/group/"
    );

    private static final List<String> ADMIN_PATHS = List.of(
            "/api/admin/**",
            "/api/role/**",
            "/api/menu/**",
            "/api/dict/**",
            "/api/config/**",
            "/api/system/**",
            "/api/order/admin/**",
            "/api/promotion/admin/**",
            "/api/user/admin/**",
            "/api/banner/admin/**",
            "/api/notice/admin/**",
            "/api/comment/admin/**",
            "/api/logistics/company",
            "/api/logistics/company/**",
            "/api/product/category/**",
            "/api/product/brand",
            "/api/product/brand/**"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();
        if (isWhitelisted(path, request.getMethod())) {
            return chain.filter(exchange);
        }
        if (!path.startsWith("/api/")) {
            return chain.filter(exchange);
        }
        String header = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (header == null || !header.startsWith("Bearer ")) {
            return unauthorized(exchange, ResultCode.UNAUTHORIZED);
        }
        String token = header.substring(7);
        Claims claims;
        try {
            if (JwtUtil.isTokenExpired(token, null)) {
                return unauthorized(exchange, ResultCode.TOKEN_EXPIRED);
            }
            claims = JwtUtil.parseToken(token, null);
        } catch (Exception ex) {
            return unauthorized(exchange, ResultCode.TOKEN_INVALID);
        }
        if (isAdminPath(path)) {
            Object userType = claims.get("userType");
            if (userType == null || !"admin".equalsIgnoreCase(userType.toString())) {
                return unauthorized(exchange, ResultCode.FORBIDDEN);
            }
        }
        ServerHttpRequest mutated = request.mutate()
                .header("X-User-Id", claims.getSubject())
                .header("X-User-Type", claims.get("userType") == null ? "" : claims.get("userType").toString())
                .build();
        return chain.filter(exchange.mutate().request(mutated).build());
    }

    private boolean isWhitelisted(String path, HttpMethod method) {
        for (String pattern : WHITELIST) {
            if (MATCHER.match(pattern, path)) {
                return true;
            }
        }
        if (HttpMethod.GET.equals(method)) {
            for (String prefix : PUBLIC_GET_PREFIXES) {
                if (path.startsWith(prefix)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isAdminPath(String path) {
        for (String pattern : ADMIN_PATHS) {
            if (MATCHER.match(pattern, path)) {
                return true;
            }
        }
        return false;
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange, ResultCode code) {
        HttpStatus status = code == ResultCode.FORBIDDEN ? HttpStatus.FORBIDDEN : HttpStatus.UNAUTHORIZED;
        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        byte[] bytes;
        try {
            bytes = MAPPER.writeValueAsBytes(Result.failed(code));
        } catch (JsonProcessingException e) {
            bytes = ("{\"code\":" + code.getCode() + ",\"message\":\"" + code.getMessage() + "\"}")
                    .getBytes(StandardCharsets.UTF_8);
        }
        DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(bytes);
        return exchange.getResponse().writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        return -100;
    }
}

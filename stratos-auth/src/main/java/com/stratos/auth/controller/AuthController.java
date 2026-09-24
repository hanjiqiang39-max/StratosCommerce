package com.stratos.auth.controller;

import com.stratos.auth.dto.AuthLoginDTO;
import com.stratos.auth.dto.AuthRegisterDTO;
import com.stratos.auth.dto.AuthSmsLoginDTO;
import com.stratos.auth.dto.AuthSmsSendDTO;
import com.stratos.auth.vo.AuthLoginVO;
import com.stratos.common.result.Result;
import com.stratos.common.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * 认证中心：转发用户服务完成注册登录，并校验 JWT。
 *
 * @author StratosCommerce
 * @since 1.0.0
 */
@Tag(name = "认证管理", description = "登录、注册、Token验证")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final RestTemplate restTemplate;

    @Value("${stratos.user.base-url:http://localhost:8082}")
    private String userBaseUrl;

    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public Result<AuthLoginVO> login(@Validated @RequestBody AuthLoginDTO dto) {
        Result<Map<String, Object>> remote;
        try {
            remote = restTemplate.exchange(
                    userBaseUrl + "/user/login",
                    HttpMethod.POST,
                    new HttpEntity<>(dto),
                    new ParameterizedTypeReference<Result<Map<String, Object>>>() {
                    }).getBody();
        } catch (RestClientException ex) {
            return Result.failed("用户服务不可用: " + ex.getMessage());
        }
        if (remote == null || !remote.isSuccess() || remote.getData() == null) {
            return Result.failed(remote != null ? remote.getMessage() : "登录失败");
        }
        Map<String, Object> data = remote.getData();
        AuthLoginVO vo = new AuthLoginVO();
        vo.setToken((String) data.get("token"));
        vo.setRefreshToken((String) data.get("refreshToken"));
        vo.setUserId(data.get("userId") == null ? null : Long.valueOf(String.valueOf(data.get("userId"))));
        vo.setUsername((String) data.get("username"));
        return Result.success("登录成功", vo);
    }

    @Operation(summary = "用户注册")
    @PostMapping("/register")
    public Result<Long> register(@Validated @RequestBody AuthRegisterDTO dto) {
        Result<Long> remote;
        try {
            remote = restTemplate.exchange(
                    userBaseUrl + "/user/register",
                    HttpMethod.POST,
                    new HttpEntity<>(dto),
                    new ParameterizedTypeReference<Result<Long>>() {
                    }).getBody();
        } catch (RestClientException ex) {
            return Result.failed("用户服务不可用: " + ex.getMessage());
        }
        if (remote == null || !remote.isSuccess()) {
            return Result.failed(remote != null ? remote.getMessage() : "注册失败");
        }
        return Result.success("注册成功", remote.getData());
    }

    @Operation(summary = "发送短信验证码")
    @PostMapping("/sms/send")
    public Result<Void> sendSms(@Validated @RequestBody AuthSmsSendDTO dto) {
        try {
            Result<Void> remote = restTemplate.exchange(
                    userBaseUrl + "/user/sms/send",
                    HttpMethod.POST,
                    new HttpEntity<>(dto),
                    new ParameterizedTypeReference<Result<Void>>() {
                    }).getBody();
            if (remote == null || !remote.isSuccess()) {
                return Result.failed(remote != null ? remote.getMessage() : "发送失败");
            }
            return Result.<Void>success("验证码已发送", null);
        } catch (RestClientException ex) {
            return Result.failed("用户服务不可用: " + ex.getMessage());
        }
    }

    @Operation(summary = "短信验证码登录")
    @PostMapping("/sms/login")
    public Result<AuthLoginVO> smsLogin(@Validated @RequestBody AuthSmsLoginDTO dto) {
        Result<Map<String, Object>> remote;
        try {
            remote = restTemplate.exchange(
                    userBaseUrl + "/user/sms/login",
                    HttpMethod.POST,
                    new HttpEntity<>(dto),
                    new ParameterizedTypeReference<Result<Map<String, Object>>>() {
                    }).getBody();
        } catch (RestClientException ex) {
            return Result.failed("用户服务不可用: " + ex.getMessage());
        }
        if (remote == null || !remote.isSuccess() || remote.getData() == null) {
            return Result.failed(remote != null ? remote.getMessage() : "登录失败");
        }
        Map<String, Object> data = remote.getData();
        AuthLoginVO vo = new AuthLoginVO();
        vo.setToken((String) data.get("token"));
        vo.setRefreshToken((String) data.get("refreshToken"));
        vo.setUserId(data.get("userId") == null ? null : Long.valueOf(String.valueOf(data.get("userId"))));
        vo.setUsername((String) data.get("username"));
        return Result.success("登录成功", vo);
    }

    @Operation(summary = "Token验证")
    @GetMapping("/verify")
    public Result<Boolean> verifyToken(@RequestHeader("Authorization") String authorization) {
        String token = authorization;
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        return Result.success(!JwtUtil.isTokenExpired(token, null));
    }

    @Operation(summary = "健康检查")
    @GetMapping("/health")
    public Result<String> health() {
        return Result.success("Auth Service is running");
    }

}

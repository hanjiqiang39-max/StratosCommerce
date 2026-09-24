package com.stratos.user.controller;

import com.stratos.common.base.PageQuery;
import com.stratos.common.base.PageResult;
import com.stratos.common.result.Result;
import com.stratos.user.dto.SmsLoginDTO;
import com.stratos.user.dto.SmsRegisterDTO;
import com.stratos.user.dto.SmsSendDTO;
import com.stratos.user.dto.SmsVerifyDTO;
import com.stratos.user.dto.UserLoginDTO;
import com.stratos.user.dto.UserProfileDTO;
import com.stratos.user.dto.UserRegisterDTO;
import com.stratos.user.entity.UserLevel;
import com.stratos.user.entity.UserPointsAccount;
import com.stratos.user.service.OauthService;
import com.stratos.user.service.UserService;
import com.stratos.user.vo.AuthOptionsVO;
import com.stratos.user.vo.LoginVO;
import com.stratos.user.vo.UserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

/**
 * 用户控制器
 *
 * @author StratosCommerce
 * @since 1.0.0
 */
@Tag(name = "用户管理", description = "用户注册、登录、信息查询")
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final OauthService oauthService;

    @Operation(summary = "用户注册")
    @PostMapping("/register")
    public Result<Long> register(@Validated @RequestBody UserRegisterDTO dto) {
        Long userId = userService.register(dto);
        return Result.success("注册成功", userId);
    }

    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public Result<LoginVO> login(@Validated @RequestBody UserLoginDTO dto, HttpServletRequest request) {
        LoginVO vo = userService.login(dto, clientIp(request), request.getHeader("User-Agent"));
        return Result.success("登录成功", vo);
    }

    @Operation(summary = "发送短信验证码")
    @PostMapping("/sms/send")
    public Result<Void> sendSmsCode(@Validated @RequestBody SmsSendDTO dto) {
        userService.sendSmsCode(dto.getPhone());
        return Result.<Void>success("验证码已发送", null);
    }

    @Operation(summary = "短信验证码登录")
    @PostMapping("/sms/login")
    public Result<LoginVO> smsLogin(@Validated @RequestBody SmsLoginDTO dto, HttpServletRequest request) {
        return Result.success("登录成功",
                userService.smsLogin(dto, clientIp(request), request.getHeader("User-Agent")));
    }

    @Operation(summary = "手机号验证码注册")
    @PostMapping("/sms/register")
    public Result<LoginVO> smsRegister(@Validated @RequestBody SmsRegisterDTO dto, HttpServletRequest request) {
        return Result.success("注册成功",
                userService.smsRegister(dto, clientIp(request), request.getHeader("User-Agent")));
    }

    @Operation(summary = "校验短信验证码")
    @PostMapping("/sms/verify")
    public Result<Void> verifySms(@Validated @RequestBody SmsVerifyDTO dto) {
        userService.consumeSmsCode(dto.getPhone(), dto.getCode());
        return Result.success();
    }

    @Operation(summary = "可用的登录方式")
    @GetMapping("/auth/options")
    public Result<AuthOptionsVO> authOptions() {
        return Result.success(userService.authOptions());
    }

    @Operation(summary = "跳转微信/支付宝授权页")
    @GetMapping("/oauth/{provider}/authorize")
    public void oauthAuthorize(@PathVariable String provider,
                               @RequestParam(value = "redirect", required = false) String redirect,
                               HttpServletResponse response) throws IOException {
        response.sendRedirect(oauthService.buildAuthorizeUrl(provider, redirect));
    }

    @Operation(summary = "微信/支付宝授权回调")
    @GetMapping("/oauth/{provider}/callback")
    public void oauthCallback(@PathVariable String provider,
                              @RequestParam(value = "code", required = false) String code,
                              @RequestParam(value = "auth_code", required = false) String authCode,
                              @RequestParam(value = "state", required = false) String state,
                              HttpServletRequest request,
                              HttpServletResponse response) throws IOException {
        try {
            String target = oauthService.finishCallback(provider, code, authCode, state,
                    clientIp(request), request.getHeader("User-Agent"));
            response.sendRedirect(target);
        } catch (Exception ex) {
            String message = java.net.URLEncoder.encode(
                    ex.getMessage() == null ? "第三方登录失败" : ex.getMessage(),
                    java.nio.charset.StandardCharsets.UTF_8);
            response.sendRedirect("http://localhost:5173/oauth/callback?error=" + message);
        }
    }

    @Operation(summary = "查询用户信息")
    @GetMapping("/{userId:\\d+}")
    public Result<UserVO> getUserById(@PathVariable Long userId) {
        return Result.success(userService.getUserById(userId));
    }

    @Operation(summary = "更新个人资料")
    @PutMapping("/{userId}/profile")
    public Result<Void> updateProfile(@PathVariable Long userId, @RequestBody UserProfileDTO dto) {
        userService.updateProfile(userId, dto);
        return Result.<Void>success("更新成功", null);
    }

    @Operation(summary = "会员等级列表")
    @GetMapping("/level/list")
    public Result<List<UserLevel>> listLevels() {
        return Result.success(userService.listLevels());
    }

    @Operation(summary = "查询用户会员等级")
    @GetMapping("/{userId}/level")
    public Result<UserLevel> getUserLevel(@PathVariable Long userId) {
        return Result.success(userService.getUserLevel(userId));
    }

    @Operation(summary = "查询积分账户")
    @GetMapping("/{userId}/points")
    public Result<UserPointsAccount> getPoints(@PathVariable Long userId) {
        return Result.success(userService.getPointsAccount(userId));
    }

    @Operation(summary = "扣减积分（下单）")
    @PostMapping("/points/deduct")
    public Result<Void> deductPoints(@RequestParam("userId") Long userId,
                                     @RequestParam("points") Integer points,
                                     @RequestParam(value = "orderId", required = false) Long orderId) {
        userService.deductPoints(userId, points, orderId);
        return Result.success();
    }

    @Operation(summary = "后台用户列表")
    @GetMapping("/admin/list")
    public Result<PageResult<UserVO>> adminList(PageQuery pageQuery,
                                                @RequestParam(value = "keyword", required = false) String keyword,
                                                @RequestParam(value = "status", required = false) Integer status) {
        return Result.success(userService.adminList(pageQuery, keyword, status));
    }

    @Operation(summary = "后台启用/禁用用户")
    @PutMapping("/admin/{userId}/status")
    public Result<Void> updateStatus(@PathVariable Long userId, @RequestParam("status") Integer status) {
        userService.updateStatus(userId, status);
        return Result.success();
    }

    @Operation(summary = "健康检查")
    @GetMapping("/health")
    public Result<String> health() {
        return Result.success("User Service is running");
    }

    private String clientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isBlank() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

}

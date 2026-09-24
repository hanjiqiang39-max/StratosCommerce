package com.stratos.system.controller;

import com.stratos.common.result.Result;
import com.stratos.system.dto.MerchantLoginDTO;
import com.stratos.system.dto.MerchantRegisterDTO;
import com.stratos.system.dto.MerchantSmsLoginDTO;
import com.stratos.system.dto.MerchantShopDTO;
import com.stratos.system.entity.MerchantShop;
import com.stratos.system.service.MerchantService;
import com.stratos.system.vo.MerchantLoginVO;
import com.stratos.system.vo.MerchantProfileVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "商家中心")
@RestController
@RequestMapping("/merchant")
@RequiredArgsConstructor
public class MerchantController {

    private final MerchantService merchantService;

    @Operation(summary = "商家登录")
    @PostMapping("/login")
    public Result<MerchantLoginVO> login(@Valid @RequestBody MerchantLoginDTO dto) {
        return Result.success("登录成功", merchantService.login(dto.getUsername(), dto.getPassword()));
    }

    @Operation(summary = "短信验证码登录")
    @PostMapping("/sms/login")
    public Result<MerchantLoginVO> smsLogin(@Valid @RequestBody MerchantSmsLoginDTO dto) {
        return Result.success("登录成功", merchantService.smsLogin(dto.getPhone(), dto.getCode()));
    }

    @Operation(summary = "商家入驻注册")
    @PostMapping("/register")
    public Result<Long> register(@Valid @RequestBody MerchantRegisterDTO dto) {
        return Result.success("已提交入驻申请，等待审核", merchantService.register(dto));
    }

    @Operation(summary = "商家资料")
    @GetMapping("/profile")
    public Result<MerchantProfileVO> profile(@RequestParam("merchantId") Long merchantId) {
        return Result.success(merchantService.profile(merchantId));
    }

    @Operation(summary = "查询自己的店铺")
    @GetMapping("/shop")
    public Result<MerchantShop> shop(@RequestParam("merchantId") Long merchantId) {
        return Result.success(merchantService.getShop(merchantId));
    }

    @Operation(summary = "公开店铺信息")
    @GetMapping("/shop/public/{shopId}")
    public Result<MerchantShop> publicShop(@PathVariable("shopId") Long shopId) {
        return Result.success(merchantService.getShopById(shopId));
    }

    @Operation(summary = "更新店铺")
    @PutMapping("/shop")
    public Result<Long> updateShop(@Valid @RequestBody MerchantShopDTO dto) {
        return Result.success("保存成功", merchantService.updateShop(dto));
    }
}

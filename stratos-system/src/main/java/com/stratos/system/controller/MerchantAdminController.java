package com.stratos.system.controller;

import com.stratos.common.base.PageQuery;
import com.stratos.common.base.PageResult;
import com.stratos.common.result.Result;
import com.stratos.system.dto.MerchantAuditDTO;
import com.stratos.system.entity.Merchant;
import com.stratos.system.service.MerchantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "商家入驻审核")
@RestController
@RequestMapping("/admin/merchant")
@RequiredArgsConstructor
public class MerchantAdminController {

    private final MerchantService merchantService;

    @Operation(summary = "商家列表")
    @GetMapping("/list")
    public Result<PageResult<Merchant>> list(PageQuery pageQuery,
                                             @RequestParam(value = "auditStatus", required = false) Integer auditStatus) {
        return Result.success(merchantService.listMerchants(pageQuery, auditStatus));
    }

    @Operation(summary = "审核入驻")
    @PutMapping("/audit")
    public Result<Void> audit(@Valid @RequestBody MerchantAuditDTO dto) {
        merchantService.audit(dto);
        return Result.success();
    }

    @Operation(summary = "启用/禁用商家")
    @PutMapping("/{merchantId}/status/{status}")
    public Result<Void> updateStatus(@PathVariable Long merchantId, @PathVariable Integer status) {
        merchantService.updateStatus(merchantId, status);
        return Result.success();
    }

    @Operation(summary = "店铺列表")
    @GetMapping("/shops")
    public Result<java.util.List<com.stratos.system.entity.MerchantShop>> shops() {
        return Result.success(merchantService.listShops());
    }
}

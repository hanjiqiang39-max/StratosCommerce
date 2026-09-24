package com.stratos.system.controller;

import com.stratos.common.result.Result;
import com.stratos.system.entity.CmsBanner;
import com.stratos.system.service.BannerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "轮播图")
@RestController
@RequestMapping("/banner")
@RequiredArgsConstructor
public class BannerController {

    private final BannerService bannerService;

    @Operation(summary = "前台轮播")
    @GetMapping("/list")
    public Result<List<CmsBanner>> list(@RequestParam(value = "position", required = false) Integer position) {
        return Result.success(bannerService.listPublished(position));
    }

    @Operation(summary = "后台轮播列表")
    @GetMapping("/admin/list")
    public Result<List<CmsBanner>> adminList() {
        return Result.success(bannerService.adminList());
    }

    @Operation(summary = "保存轮播")
    @PostMapping("/admin")
    public Result<Long> save(@RequestBody CmsBanner banner) {
        return Result.success("保存成功", bannerService.save(banner));
    }

    @Operation(summary = "删除轮播")
    @DeleteMapping("/admin/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        bannerService.delete(id);
        return Result.success();
    }
}

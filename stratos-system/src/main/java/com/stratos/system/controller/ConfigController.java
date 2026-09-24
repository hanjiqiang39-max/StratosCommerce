package com.stratos.system.controller;

import com.stratos.common.result.Result;
import com.stratos.system.entity.SysConfig;
import com.stratos.system.service.ConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 系统配置控制器
 *
 * @author StratosCommerce
 * @since 1.0.0
 */
@Tag(name = "系统配置")
@RestController
@RequestMapping("/config")
@RequiredArgsConstructor
public class ConfigController {

    private final ConfigService configService;

    @Operation(summary = "根据键获取配置值")
    @GetMapping("/value/{configKey}")
    public Result<String> getConfigValueByKey(@PathVariable String configKey) {
        return Result.success(configService.getConfigValueByKey(configKey));
    }

    @GetMapping("/list")
    public Result<java.util.List<SysConfig>> list() {
        return Result.success(configService.listAll());
    }

    @PostMapping
    public Result<Boolean> save(@RequestBody SysConfig config) {
        return Result.success(configService.saveConfig(config));
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        return Result.success(configService.deleteConfig(id));
    }

}

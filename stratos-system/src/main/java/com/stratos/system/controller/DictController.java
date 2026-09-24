package com.stratos.system.controller;

import com.stratos.common.result.Result;
import com.stratos.system.entity.SysDictData;
import com.stratos.system.entity.SysDictType;
import com.stratos.system.service.DictService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 字典控制�?
 *
 * @author StratosCommerce
 * @since 1.0.0
 */
@Tag(name = "字典管理")
@RestController
@RequestMapping("/dict")
@RequiredArgsConstructor
public class DictController {

    private final DictService dictService;

    @Operation(summary = "根据类型查询字典数据")
    @GetMapping("/data/{dictType}")
    public Result<List<SysDictData>> getDictDataByType(@PathVariable String dictType) {
        return Result.success(dictService.getDictDataByType(dictType));
    }

    @GetMapping("/type/list")
    public Result<List<SysDictType>> listTypes() {
        return Result.success(dictService.listTypes());
    }

    @PostMapping("/type")
    public Result<Boolean> saveType(@RequestBody SysDictType type) {
        return Result.success(dictService.saveType(type));
    }

    @DeleteMapping("/type/{id}")
    public Result<Boolean> deleteType(@PathVariable Long id) {
        return Result.success(dictService.deleteType(id));
    }

    @PostMapping("/data")
    public Result<Boolean> saveData(@RequestBody SysDictData data) {
        return Result.success(dictService.saveData(data));
    }

    @DeleteMapping("/data/{id}")
    public Result<Boolean> deleteData(@PathVariable Long id) {
        return Result.success(dictService.deleteData(id));
    }

}

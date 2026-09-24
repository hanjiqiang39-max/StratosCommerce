package com.stratos.inventory.controller;

import com.stratos.common.result.Result;
import com.stratos.inventory.dto.LockInventoryDTO;
import com.stratos.inventory.entity.InventoryStock;
import com.stratos.inventory.service.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 库存控制�?
 *
 * @author StratosCommerce
 * @since 1.0.0
 */
@Tag(name = "库存管理")
@RestController
@RequestMapping("/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @Operation(summary = "查询库存")
    @GetMapping("/stock")
    public Result<InventoryStock> queryStock(@RequestParam("skuId") Long skuId,
                                            @RequestParam("warehouseId") Long warehouseId) {
        return Result.success(inventoryService.queryStock(skuId, warehouseId));
    }

    @Operation(summary = "批量查询库存")
    @PostMapping("/stock/batch")
    public Result<List<InventoryStock>> batchQueryStock(@RequestBody List<Long> skuIds) {
        return Result.success(inventoryService.batchQueryStock(skuIds));
    }

    @Operation(summary = "同步/调整库存")
    @PostMapping("/stock/upsert")
    public Result<InventoryStock> upsertStock(@Valid @RequestBody com.stratos.inventory.dto.UpsertStockDTO dto) {
        return Result.success(inventoryService.upsertStock(dto.getSkuId(), dto.getWarehouseId(), dto.getStock()));
    }

    @Operation(summary = "锁定库存")
    @PostMapping("/lock")
    public Result<Void> lockStock(@Valid @RequestBody LockInventoryDTO dto) {
        inventoryService.lockStock(dto);
        return Result.success();
    }

    @Operation(summary = "释放库存")
    @PostMapping("/unlock/{orderId}")
    public Result<Void> unlockStock(@PathVariable Long orderId) {
        inventoryService.unlockStock(orderId);
        return Result.success();
    }

    @Operation(summary = "扣减库存")
    @PostMapping("/deduct/{orderId}")
    public Result<Void> deductStock(@PathVariable Long orderId) {
        inventoryService.deductStock(orderId);
        return Result.success();
    }

}

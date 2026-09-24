package com.stratos.logistics.controller;

import com.stratos.common.result.Result;
import com.stratos.logistics.dto.CreateShipmentDTO;
import com.stratos.logistics.dto.FreightQuoteDTO;
import com.stratos.logistics.entity.LogisticsCompany;
import com.stratos.logistics.entity.LogisticsTrace;
import com.stratos.logistics.service.LogisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 物流
 *
 * @author StratosCommerce
 * @since 1.0.0
 */
@Tag(name = "物流管理")
@RestController
@RequestMapping("/logistics")
@RequiredArgsConstructor
public class LogisticsController {

    private final LogisticsService logisticsService;

    @GetMapping("/health")
    public Result<String> health() {
        return Result.success("Logistics Service is running");
    }

    @GetMapping("/companies")
    public Result<List<LogisticsCompany>> companies() {
        return Result.success(logisticsService.listCompanies());
    }

    @GetMapping("/company/admin/list")
    public Result<List<LogisticsCompany>> adminCompanies() {
        return Result.success(logisticsService.adminListCompanies());
    }

    @PostMapping("/company")
    public Result<Long> saveCompany(@RequestBody LogisticsCompany company) {
        return Result.success("保存成功", logisticsService.saveCompany(company));
    }

    @DeleteMapping("/company/{id}")
    public Result<Void> deleteCompany(@PathVariable Long id) {
        logisticsService.deleteCompany(id);
        return Result.success();
    }

    @PostMapping("/freight")
    public Result<java.math.BigDecimal> freight(@RequestBody FreightQuoteDTO dto) {
        return Result.success(logisticsService.quoteFreight(dto));
    }

    @PostMapping("/ship")
    public Result<String> ship(@Valid @RequestBody CreateShipmentDTO dto) {
        return Result.success("已发货", logisticsService.createShipment(dto));
    }

    @Operation(summary = "根据订单号查询物流轨迹")
    @GetMapping("/trace/order/{orderNo}")
    public Result<List<LogisticsTrace>> queryTraceByOrderNo(@PathVariable("orderNo") String orderNo) {
        return Result.success(logisticsService.queryTraceByOrderNo(orderNo));
    }

    @Operation(summary = "根据物流单号查询物流轨迹")
    @GetMapping("/trace/logistics/{logisticsNo}")
    public Result<List<LogisticsTrace>> queryTraceByLogisticsNo(@PathVariable("logisticsNo") String logisticsNo) {
        return Result.success(logisticsService.queryTraceByLogisticsNo(logisticsNo));
    }

    @Operation(summary = "同步物流轨迹")
    @PostMapping("/trace/sync/{logisticsNo}")
    public Result<Void> syncTrace(@PathVariable("logisticsNo") String logisticsNo) {
        logisticsService.syncTrace(logisticsNo);
        return Result.success();
    }

}

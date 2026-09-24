package com.stratos.order.controller;

import com.stratos.common.base.PageQuery;
import com.stratos.common.base.PageResult;
import com.stratos.common.result.Result;
import com.stratos.order.dto.OrderRemarkDTO;
import com.stratos.order.dto.OrderShipDTO;
import com.stratos.order.dto.RefundAuditDTO;
import com.stratos.order.entity.OrderInfo;
import com.stratos.order.entity.OrderRefund;
import com.stratos.order.service.OrderService;
import com.stratos.order.vo.OrderDetailVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Tag(name = "商家订单")
@RestController
@RequestMapping("/merchant")
@RequiredArgsConstructor
public class MerchantOrderController {

    private final OrderService orderService;

    @Operation(summary = "本店订单列表")
    @GetMapping("/order/list")
    public Result<PageResult<OrderInfo>> list(PageQuery pageQuery,
                                              @RequestParam("shopId") Long shopId,
                                              @RequestParam(value = "status", required = false) Integer status,
                                              @RequestParam(value = "orderNo", required = false) String orderNo) {
        return Result.success(orderService.adminList(pageQuery, status, orderNo, shopId));
    }

    @Operation(summary = "本店订单详情")
    @GetMapping("/order/{orderId}")
    public Result<OrderDetailVO> detail(@PathVariable Long orderId, @RequestParam("shopId") Long shopId) {
        return Result.success(orderService.getMerchantOrderDetail(orderId, shopId));
    }

    @Operation(summary = "按订单号查本店订单")
    @GetMapping("/order/no/{orderNo}")
    public Result<OrderDetailVO> detailByNo(@PathVariable String orderNo, @RequestParam("shopId") Long shopId) {
        return Result.success(orderService.getMerchantOrderDetailByNo(orderNo, shopId));
    }

    @Operation(summary = "卖家备注")
    @PutMapping("/order/remark")
    public Result<Void> remark(@Validated @RequestBody OrderRemarkDTO dto) {
        orderService.updateSellerRemark(dto.getOrderId(), dto.getSellerRemark(), dto.getShopId());
        return Result.success();
    }

    @Operation(summary = "发货")
    @PostMapping("/order/ship")
    public Result<String> ship(@Validated @RequestBody OrderShipDTO dto) {
        return Result.success("已发货", orderService.ship(dto.getOrderId(), dto.getCompanyCode(), dto.getShopId()));
    }

    @Operation(summary = "本店报表")
    @GetMapping("/order/report")
    public Result<Map<String, Object>> report(@RequestParam("shopId") Long shopId,
                                              @RequestParam(value = "start", required = false) String start,
                                              @RequestParam(value = "end", required = false) String end) {
        return Result.success(orderService.report(parseDateTime(start, true), parseDateTime(end, false), shopId));
    }

    @Operation(summary = "本店售后")
    @GetMapping("/refund/list")
    public Result<List<OrderRefund>> refunds(@RequestParam("shopId") Long shopId) {
        return Result.success(orderService.listRefundsByShop(shopId));
    }

    @Operation(summary = "审核售后")
    @PutMapping("/refund/audit")
    public Result<Void> audit(@Validated @RequestBody RefundAuditDTO dto) {
        orderService.auditRefund(dto);
        return Result.success();
    }

    private LocalDateTime parseDateTime(String value, boolean startOfDay) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        if (value.length() <= 10) {
            LocalDate date = LocalDate.parse(value);
            return startOfDay ? date.atStartOfDay() : date.plusDays(1).atStartOfDay();
        }
        return LocalDateTime.parse(value);
    }
}

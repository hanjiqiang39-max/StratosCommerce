package com.stratos.order.controller;

import com.stratos.common.base.PageQuery;
import com.stratos.common.base.PageResult;
import com.stratos.common.result.Result;
import com.stratos.order.dto.CheckoutDTO;
import com.stratos.order.dto.CreateOrderDTO;
import com.stratos.order.dto.OrderRemarkDTO;
import com.stratos.order.dto.OrderShipDTO;
import com.stratos.order.dto.RefundApplyDTO;
import com.stratos.order.dto.RefundAuditDTO;
import com.stratos.order.entity.OrderInfo;
import com.stratos.order.entity.OrderRefund;
import com.stratos.order.service.OrderService;
import com.stratos.order.vo.OrderCreateVO;
import com.stratos.order.vo.OrderDetailVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 订单控制器
 *
 * @author StratosCommerce
 * @since 1.0.0
 */
@Tag(name = "订单管理", description = "订单创建、查询、取消、售后")
@RestController
@RequestMapping("/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @Operation(summary = "创建订单")
    @PostMapping("/create")
    public Result<OrderCreateVO> createOrder(@Validated @RequestBody CreateOrderDTO dto) {
        return Result.success("订单创建成功", orderService.createOrder(dto));
    }

    @Operation(summary = "购物车结算下单")
    @PostMapping("/create-from-cart")
    public Result<OrderCreateVO> createFromCart(@Validated @RequestBody CheckoutDTO dto) {
        return Result.success("订单创建成功", orderService.createFromCart(dto));
    }

    @Operation(summary = "后台订单列表")
    @GetMapping("/admin/list")
    public Result<PageResult<OrderInfo>> adminList(PageQuery pageQuery,
                                                   @RequestParam(value = "status", required = false) Integer status,
                                                   @RequestParam(value = "orderNo", required = false) String orderNo) {
        return Result.success(orderService.adminList(pageQuery, status, orderNo));
    }

    @Operation(summary = "后台备注")
    @PutMapping("/admin/remark")
    public Result<Void> remark(@Validated @RequestBody OrderRemarkDTO dto) {
        orderService.updateSellerRemark(dto.getOrderId(), dto.getSellerRemark());
        return Result.success();
    }

    @Operation(summary = "后台发货")
    @PostMapping("/admin/ship")
    public Result<String> ship(@Validated @RequestBody OrderShipDTO dto) {
        return Result.success("已发货", orderService.ship(dto.getOrderId(), dto.getCompanyCode()));
    }

    @Operation(summary = "后台简易报表")
    @GetMapping("/admin/report")
    public Result<java.util.Map<String, Object>> report(
            @RequestParam(value = "start", required = false) String start,
            @RequestParam(value = "end", required = false) String end) {
        java.time.LocalDateTime startTime = parseDateTime(start, true);
        java.time.LocalDateTime endTime = parseDateTime(end, false);
        return Result.success(orderService.report(startTime, endTime));
    }

    @Operation(summary = "查询用户订单列表")
    @GetMapping("/user/{userId}")
    public Result<PageResult<OrderInfo>> getUserOrders(@PathVariable("userId") Long userId, PageQuery pageQuery) {
        return Result.success(orderService.getUserOrders(userId, pageQuery));
    }

    @Operation(summary = "支付成功回调（内部）")
    @PostMapping("/{orderId}/pay-success")
    public Result<Void> paySuccess(@PathVariable("orderId") Long orderId) {
        orderService.paySuccess(orderId);
        return Result.<Void>success("订单已支付", null);
    }

    @Operation(summary = "确认收货")
    @PutMapping("/{orderId}/confirm")
    public Result<Void> confirm(@PathVariable("orderId") Long orderId) {
        orderService.confirmReceive(orderId);
        return Result.<Void>success("确认收货成功", null);
    }

    @Operation(summary = "申请售后")
    @PostMapping("/refund")
    public Result<Long> applyRefund(@Validated @RequestBody RefundApplyDTO dto) {
        return Result.success("申请已提交", orderService.applyRefund(dto));
    }

    @Operation(summary = "后台售后列表")
    @GetMapping("/admin/refunds")
    public Result<java.util.List<OrderRefund>> adminRefunds(
            @RequestParam(value = "status", required = false) Integer status) {
        return Result.success(orderService.listAllRefunds(status));
    }

    @Operation(summary = "用户售后列表")
    @GetMapping("/refund/user/{userId}")
    public Result<java.util.List<OrderRefund>> listRefunds(@PathVariable("userId") Long userId) {
        return Result.success(orderService.listRefunds(userId));
    }

    @Operation(summary = "售后详情")
    @GetMapping("/refund/{refundId}")
    public Result<OrderRefund> getRefund(@PathVariable("refundId") Long refundId) {
        return Result.success(orderService.getRefund(refundId));
    }

    @Operation(summary = "审核售后")
    @PutMapping("/refund/audit")
    public Result<Void> auditRefund(@Validated @RequestBody RefundAuditDTO dto) {
        orderService.auditRefund(dto);
        return Result.<Void>success("审核完成", null);
    }

    @Operation(summary = "按订单号查询详情")
    @GetMapping("/no/{orderNo}")
    public Result<OrderDetailVO> getOrderDetailByNo(@PathVariable("orderNo") String orderNo) {
        return Result.success(orderService.getOrderDetailByNo(orderNo));
    }

    @Operation(summary = "查询订单详情")
    @GetMapping("/{orderId:\\d+}")
    public Result<OrderDetailVO> getOrderDetail(@PathVariable("orderId") Long orderId) {
        return Result.success(orderService.getOrderDetail(orderId));
    }

    @Operation(summary = "取消订单")
    @PutMapping("/{orderId}/cancel")
    public Result<Void> cancelOrder(@PathVariable("orderId") Long orderId) {
        orderService.cancelOrder(orderId);
        return Result.<Void>success("订单取消成功", null);
    }

    @Operation(summary = "健康检查")
    @GetMapping("/health")
    public Result<String> health() {
        return Result.success("Order Service is running");
    }

    private java.time.LocalDateTime parseDateTime(String value, boolean startOfDay) {
        if (!org.springframework.util.StringUtils.hasText(value)) {
            return null;
        }
        if (value.length() <= 10) {
            java.time.LocalDate date = java.time.LocalDate.parse(value);
            return startOfDay ? date.atStartOfDay() : date.plusDays(1).atStartOfDay();
        }
        return java.time.LocalDateTime.parse(value);
    }

}

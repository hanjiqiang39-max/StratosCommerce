package com.stratos.promotion.controller;

import com.stratos.common.result.Result;
import com.stratos.promotion.dto.ReceiveCouponDTO;
import com.stratos.promotion.entity.PromotionCoupon;
import com.stratos.promotion.service.CouponService;
import com.stratos.promotion.vo.UserCouponVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 优惠券控制器
 *
 * @author StratosCommerce
 * @since 1.0.0
 */
@Tag(name = "优惠券管理")
@RestController
@RequestMapping("/coupon")
@RequiredArgsConstructor
public class CouponController {

    private final CouponService couponService;

    @Operation(summary = "可领取优惠券列表")
    @GetMapping("/list")
    public Result<List<PromotionCoupon>> listCoupons() {
        return Result.success(couponService.listAvailableCoupons());
    }

    @Operation(summary = "使用优惠券")
    @PostMapping("/use")
    public Result<Void> useCoupon(@RequestParam("userCouponId") Long userCouponId,
                                  @RequestParam("orderId") Long orderId,
                                  @RequestParam("orderNo") String orderNo) {
        couponService.useCoupon(userCouponId, orderId, orderNo);
        return Result.success();
    }

    @Operation(summary = "领取优惠券")
    @PostMapping("/receive")
    public Result<Void> receiveCoupon(@Valid @RequestBody ReceiveCouponDTO dto) {
        couponService.receiveCoupon(dto);
        return Result.success();
    }

    @Operation(summary = "查询用户优惠券")
    @GetMapping("/user/{userId}")
    public Result<List<UserCouponVO>> queryUserCoupons(
            @PathVariable Long userId,
            @RequestParam(value = "status", required = false) Integer status) {
        return Result.success(couponService.queryUserCouponVOs(userId, status));
    }

    @Operation(summary = "优惠券试算")
    @GetMapping("/quote")
    public Result<java.math.BigDecimal> quote(@RequestParam("userCouponId") Long userCouponId,
                                              @RequestParam("userId") Long userId,
                                              @RequestParam("amount") java.math.BigDecimal amount) {
        return Result.success(couponService.quote(userCouponId, userId, amount));
    }

}

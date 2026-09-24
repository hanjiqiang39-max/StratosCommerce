package com.stratos.order.controller;

import com.stratos.common.result.Result;
import com.stratos.order.dto.CartItemDTO;
import com.stratos.order.entity.ShoppingCart;
import com.stratos.order.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 购物车
 *
 * @author StratosCommerce
 * @since 1.0.0
 */
@Tag(name = "购物车")
@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @Operation(summary = "购物车列表")
    @GetMapping("/list/{userId}")
    public Result<List<ShoppingCart>> list(@PathVariable("userId") Long userId) {
        return Result.success(cartService.list(userId));
    }

    @Operation(summary = "加入购物车")
    @PostMapping
    public Result<Long> add(@Validated @RequestBody CartItemDTO dto) {
        return Result.success("已加入购物车", cartService.add(dto));
    }

    @Operation(summary = "修改数量")
    @PutMapping("/{cartId}")
    public Result<Void> updateQuantity(@PathVariable("cartId") Long cartId,
                                       @RequestParam("userId") Long userId,
                                       @RequestParam("quantity") Integer quantity) {
        cartService.updateQuantity(cartId, userId, quantity);
        return Result.<Void>success("更新成功", null);
    }

    @Operation(summary = "删除购物车")
    @DeleteMapping("/{cartId}")
    public Result<Void> delete(@PathVariable("cartId") Long cartId, @RequestParam("userId") Long userId) {
        cartService.delete(cartId, userId);
        return Result.<Void>success("删除成功", null);
    }

    @Operation(summary = "勾选或取消勾选")
    @PutMapping("/{cartId}/selected")
    public Result<Void> selected(@PathVariable("cartId") Long cartId,
                                 @RequestParam("userId") Long userId,
                                 @RequestParam("selected") Integer selected) {
        cartService.updateSelected(cartId, userId, selected);
        return Result.success();
    }

    @Operation(summary = "全选或全不选")
    @PutMapping("/selected/all")
    public Result<Void> selectedAll(@RequestParam("userId") Long userId,
                                    @RequestParam("selected") Integer selected) {
        cartService.updateSelectedAll(userId, selected);
        return Result.success();
    }

}

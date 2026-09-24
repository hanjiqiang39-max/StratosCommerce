package com.stratos.user.controller;

import com.stratos.common.result.Result;
import com.stratos.user.dto.UserAddressDTO;
import com.stratos.user.entity.UserAddress;
import com.stratos.user.service.AddressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 收货地址
 *
 * @author StratosCommerce
 * @since 1.0.0
 */
@Tag(name = "收货地址")
@RestController
@RequestMapping("/address")
@RequiredArgsConstructor
public class AddressController {

    private final AddressService addressService;

    @Operation(summary = "地址列表")
    @GetMapping("/list/{userId}")
    public Result<List<UserAddress>> list(@PathVariable Long userId) {
        return Result.success(addressService.listByUserId(userId));
    }

    @Operation(summary = "地址详情")
    @GetMapping("/{userId}/{addressId}")
    public Result<UserAddress> detail(@PathVariable Long userId, @PathVariable Long addressId) {
        return Result.success(addressService.getById(addressId, userId));
    }

    @Operation(summary = "新增地址")
    @PostMapping("/{userId}")
    public Result<Long> create(@PathVariable Long userId, @Validated @RequestBody UserAddressDTO dto) {
        return Result.success("新增成功", addressService.create(userId, dto));
    }

    @Operation(summary = "修改地址")
    @PutMapping("/{userId}/{addressId}")
    public Result<Void> update(@PathVariable Long userId, @PathVariable Long addressId,
                               @Validated @RequestBody UserAddressDTO dto) {
        addressService.update(addressId, userId, dto);
        return Result.<Void>success("修改成功", null);
    }

    @Operation(summary = "删除地址")
    @DeleteMapping("/{userId}/{addressId}")
    public Result<Void> delete(@PathVariable Long userId, @PathVariable Long addressId) {
        addressService.delete(addressId, userId);
        return Result.<Void>success("删除成功", null);
    }

    @Operation(summary = "设为默认地址")
    @PutMapping("/{userId}/{addressId}/default")
    public Result<Void> setDefault(@PathVariable Long userId, @PathVariable Long addressId) {
        addressService.setDefault(addressId, userId);
        return Result.<Void>success("设置成功", null);
    }

}

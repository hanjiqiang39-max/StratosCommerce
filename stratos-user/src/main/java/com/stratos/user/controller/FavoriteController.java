package com.stratos.user.controller;

import com.stratos.common.result.Result;
import com.stratos.user.entity.UserFavorite;
import com.stratos.user.service.FavoriteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "用户收藏")
@RestController
@RequestMapping("/favorite")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;

    @Operation(summary = "添加收藏")
    @PostMapping
    public Result<Long> add(@RequestParam("userId") Long userId,
                            @RequestParam(value = "targetType", defaultValue = "1") Integer targetType,
                            @RequestParam("targetId") Long targetId) {
        return Result.success("已收藏", favoriteService.add(userId, targetType, targetId));
    }

    @Operation(summary = "取消收藏")
    @DeleteMapping
    public Result<Void> remove(@RequestParam("userId") Long userId,
                               @RequestParam(value = "targetType", defaultValue = "1") Integer targetType,
                               @RequestParam("targetId") Long targetId) {
        favoriteService.remove(userId, targetType, targetId);
        return Result.success();
    }

    @Operation(summary = "收藏列表")
    @GetMapping("/list")
    public Result<List<UserFavorite>> list(@RequestParam("userId") Long userId,
                                           @RequestParam(value = "targetType", required = false) Integer targetType) {
        return Result.success(favoriteService.list(userId, targetType));
    }

    @Operation(summary = "是否已收藏")
    @GetMapping("/exists")
    public Result<Boolean> exists(@RequestParam("userId") Long userId,
                                  @RequestParam(value = "targetType", defaultValue = "1") Integer targetType,
                                  @RequestParam("targetId") Long targetId) {
        return Result.success(favoriteService.exists(userId, targetType, targetId));
    }
}

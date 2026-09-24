package com.stratos.promotion.controller;

import com.stratos.common.result.Result;
import com.stratos.promotion.entity.PromotionCoupon;
import com.stratos.promotion.entity.PromotionGroupBuying;
import com.stratos.promotion.entity.PromotionSeckill;
import com.stratos.promotion.mapper.PromotionCouponMapper;
import com.stratos.promotion.mapper.PromotionGroupBuyingMapper;
import com.stratos.promotion.service.PromotionWriteSupport;
import com.stratos.promotion.service.SeckillService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Tag(name = "商家营销")
@RestController
@RequestMapping("/merchant/promotion")
@RequiredArgsConstructor
public class MerchantPromotionController {

    private final SeckillService seckillService;
    private final PromotionCouponMapper couponMapper;
    private final PromotionGroupBuyingMapper groupBuyingMapper;
    private final PromotionWriteSupport writeSupport;

    @GetMapping("/seckill")
    public Result<List<PromotionSeckill>> seckillList(@RequestParam(value = "skuIds", required = false) String skuIds) {
        return Result.success(filterBySku(seckillService.list(), skuIds, PromotionSeckill::getSkuId));
    }

    @PostMapping("/seckill")
    public Result<Boolean> saveSeckill(@RequestBody PromotionSeckill entity) {
        return Result.success(writeSupport.saveSeckill(entity));
    }

    @PutMapping("/seckill/{id}/status/{status}")
    public Result<Void> seckillStatus(@PathVariable Long id, @PathVariable Integer status) {
        PromotionSeckill entity = seckillService.getById(id);
        if (entity != null) {
            entity.setStatus(status);
            seckillService.updateById(entity);
            if (status != null && status == 1 && entity.getSeckillStock() != null) {
                seckillService.warmup(id);
            }
        }
        return Result.success();
    }

    @GetMapping("/coupon")
    public Result<List<PromotionCoupon>> couponList() {
        return Result.success(couponMapper.selectList(null));
    }

    @PostMapping("/coupon")
    public Result<Integer> saveCoupon(@RequestBody PromotionCoupon entity) {
        return Result.success(writeSupport.saveCoupon(entity));
    }

    @PutMapping("/coupon/{id}/status/{status}")
    public Result<Void> couponStatus(@PathVariable Long id, @PathVariable Integer status) {
        PromotionCoupon entity = couponMapper.selectById(id);
        if (entity != null) {
            entity.setStatus(status);
            couponMapper.updateById(entity);
        }
        return Result.success();
    }

    @GetMapping("/group")
    public Result<List<PromotionGroupBuying>> groupList(@RequestParam(value = "skuIds", required = false) String skuIds) {
        return Result.success(filterBySku(groupBuyingMapper.selectList(null), skuIds, PromotionGroupBuying::getSkuId));
    }

    @PostMapping("/group")
    public Result<Integer> saveGroup(@RequestBody PromotionGroupBuying entity) {
        return Result.success(writeSupport.saveGroup(entity));
    }

    @PutMapping("/group/{id}/status/{status}")
    public Result<Void> groupStatus(@PathVariable Long id, @PathVariable Integer status) {
        PromotionGroupBuying entity = groupBuyingMapper.selectById(id);
        if (entity != null) {
            entity.setStatus(status);
            entity.setUpdateTime(LocalDateTime.now());
            groupBuyingMapper.updateById(entity);
        }
        return Result.success();
    }

    private <T> List<T> filterBySku(List<T> source, String skuIds, java.util.function.Function<T, Long> getter) {
        if (!StringUtils.hasText(skuIds) || source == null) {
            return source;
        }
        Set<String> ids = Arrays.stream(skuIds.split(","))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .collect(Collectors.toSet());
        return source.stream()
                .filter(item -> getter.apply(item) != null && ids.contains(String.valueOf(getter.apply(item))))
                .toList();
    }
}

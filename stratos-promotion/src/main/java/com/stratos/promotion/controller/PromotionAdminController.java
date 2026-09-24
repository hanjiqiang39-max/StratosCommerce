package com.stratos.promotion.controller;

import com.stratos.common.result.Result;
import com.stratos.promotion.entity.PromotionCoupon;
import com.stratos.promotion.entity.PromotionFullDiscount;
import com.stratos.promotion.entity.PromotionGroupBuying;
import com.stratos.promotion.entity.PromotionSeckill;
import com.stratos.promotion.mapper.PromotionCouponMapper;
import com.stratos.promotion.mapper.PromotionGroupBuyingMapper;
import com.stratos.promotion.service.FullDiscountService;
import com.stratos.promotion.service.PromotionWriteSupport;
import com.stratos.promotion.service.SeckillService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Tag(name = "营销后台")
@RestController
@RequestMapping("/promotion/admin")
@RequiredArgsConstructor
public class PromotionAdminController {

    private final SeckillService seckillService;
    private final PromotionCouponMapper couponMapper;
    private final FullDiscountService fullDiscountService;
    private final PromotionGroupBuyingMapper groupBuyingMapper;
    private final PromotionWriteSupport writeSupport;

    @GetMapping("/seckill")
    public Result<List<PromotionSeckill>> seckillList() {
        return Result.success(seckillService.list());
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

    @GetMapping("/full-discount")
    public Result<List<PromotionFullDiscount>> fullList() {
        return Result.success(fullDiscountService.list());
    }

    @PostMapping("/full-discount")
    public Result<Boolean> saveFull(@RequestBody PromotionFullDiscount entity) {
        return Result.success(fullDiscountService.saveOrUpdate(entity));
    }

    @PutMapping("/full-discount/{id}/status/{status}")
    public Result<Void> fullStatus(@PathVariable Long id, @PathVariable Integer status) {
        PromotionFullDiscount entity = fullDiscountService.getById(id);
        if (entity != null) {
            entity.setStatus(status);
            entity.setUpdateTime(LocalDateTime.now());
            fullDiscountService.updateById(entity);
        }
        return Result.success();
    }

    @GetMapping("/group")
    public Result<List<PromotionGroupBuying>> groupList() {
        return Result.success(groupBuyingMapper.selectList(null));
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
}

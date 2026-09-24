package com.stratos.promotion.controller;

import com.stratos.common.result.Result;
import com.stratos.promotion.dto.BindSeckillOrderDTO;
import com.stratos.promotion.dto.SeckillDTO;
import com.stratos.promotion.entity.PromotionSeckill;
import com.stratos.promotion.entity.PromotionSeckillRecord;
import com.stratos.promotion.service.SeckillService;
import com.stratos.promotion.vo.SeckillResultVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 秒杀控制器
 *
 * @author StratosCommerce
 * @since 1.0.0
 */
@Tag(name = "秒杀管理")
@RestController
@RequestMapping("/seckill")
@RequiredArgsConstructor
public class SeckillController {

    private final SeckillService seckillService;

    @GetMapping("/health")
    public Result<String> health() {
        return Result.success("Promotion Service is running");
    }

    @Operation(summary = "秒杀活动列表")
    @GetMapping("/list")
    public Result<java.util.List<PromotionSeckill>> list() {
        return Result.success(seckillService.listActive());
    }

    @Operation(summary = "参与秒杀")
    @PostMapping("/do")
    public Result<SeckillResultVO> doSeckill(@Valid @RequestBody SeckillDTO dto) {
        SeckillResultVO vo = seckillService.doSeckill(dto);
        return Result.success(Boolean.TRUE.equals(vo.getReused()) ? "已抢过，继续完成订单" : "秒杀成功", vo);
    }

    @Operation(summary = "我的秒杀记录")
    @GetMapping("/user/{userId}")
    public Result<java.util.List<PromotionSeckillRecord>> userRecords(@PathVariable Long userId) {
        return Result.success(seckillService.listUserRecords(userId));
    }

    @Operation(summary = "某活动下我的秒杀记录")
    @GetMapping("/user/{userId}/activity/{seckillId}")
    public Result<PromotionSeckillRecord> userActivity(@PathVariable Long userId, @PathVariable Long seckillId) {
        return Result.success(seckillService.findUserRecord(seckillId, userId));
    }

    @Operation(summary = "按订单查秒杀记录")
    @GetMapping("/order/{orderId}")
    public Result<PromotionSeckillRecord> byOrder(@PathVariable Long orderId) {
        return Result.success(seckillService.findByOrderId(orderId));
    }

    @PostMapping("/warmup/{seckillId}")
    public Result<Void> warmup(@PathVariable("seckillId") Long seckillId) {
        seckillService.warmup(seckillId);
        return Result.<Void>success("库存已预热", null);
    }

    @Operation(summary = "查询秒杀活动详情")
    @GetMapping("/detail/{seckillId}")
    public Result<PromotionSeckill> querySeckillDetail(@PathVariable Long seckillId) {
        return Result.success(seckillService.querySeckillDetail(seckillId));
    }

    @Operation(summary = "绑定秒杀订单")
    @PostMapping("/bind-order")
    public Result<Void> bindOrder(@Valid @RequestBody BindSeckillOrderDTO dto) {
        seckillService.bindOrder(dto);
        return Result.success();
    }

}

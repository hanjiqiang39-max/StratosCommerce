package com.stratos.promotion.controller;

import com.stratos.common.result.Result;
import com.stratos.promotion.dto.FullDiscountCalcDTO;
import com.stratos.promotion.dto.FullDiscountCalcVO;
import com.stratos.promotion.entity.PromotionFullDiscount;
import com.stratos.promotion.service.FullDiscountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "满减活动")
@RestController
@RequestMapping({"/full-discount", "/promotion/full-discount"})
@RequiredArgsConstructor
public class FullDiscountController {

    private final FullDiscountService fullDiscountService;

    @Operation(summary = "进行中的满减")
    @GetMapping("/list")
    public Result<List<PromotionFullDiscount>> list() {
        return Result.success(fullDiscountService.listActive());
    }

    @Operation(summary = "满减试算")
    @PostMapping("/calculate")
    public Result<FullDiscountCalcVO> calculate(@Valid @RequestBody FullDiscountCalcDTO dto) {
        return Result.success(fullDiscountService.calculate(dto));
    }
}

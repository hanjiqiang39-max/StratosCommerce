package com.stratos.product.controller;

import com.stratos.common.result.Result;
import com.stratos.product.entity.ProductComment;
import com.stratos.product.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "商家评价")
@RestController
@RequestMapping("/merchant/comment")
@RequiredArgsConstructor
public class MerchantCommentController {

    private final CommentService commentService;

    @Operation(summary = "本店评价")
    @GetMapping("/list")
    public Result<List<ProductComment>> list(@RequestParam("shopId") Long shopId) {
        return Result.success(commentService.listByShop(shopId));
    }

    @Operation(summary = "回复评价")
    @PutMapping("/{id}/reply")
    public Result<Void> reply(@PathVariable Long id,
                              @RequestParam("shopId") Long shopId,
                              @RequestParam("replyContent") String replyContent) {
        commentService.replyByShop(id, shopId, replyContent);
        return Result.success();
    }
}

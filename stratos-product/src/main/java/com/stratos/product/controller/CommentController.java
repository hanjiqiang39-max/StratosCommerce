package com.stratos.product.controller;

import com.stratos.common.result.Result;
import com.stratos.product.dto.SaveCommentDTO;
import com.stratos.product.entity.ProductComment;
import com.stratos.product.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "商品评价")
@RestController
@RequestMapping("/comment")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @Operation(summary = "发表评价")
    @PostMapping
    public Result<Long> add(@Valid @RequestBody SaveCommentDTO dto) {
        return Result.success("评价成功", commentService.add(dto));
    }

    @Operation(summary = "商品评价列表")
    @GetMapping("/product/{spuId}")
    public Result<List<ProductComment>> listBySpu(@PathVariable Long spuId) {
        return Result.success(commentService.listBySpu(spuId));
    }

    @Operation(summary = "后台评价列表")
    @GetMapping("/admin/list")
    public Result<List<ProductComment>> adminList(@RequestParam(value = "auditStatus", required = false) Integer auditStatus) {
        return Result.success(commentService.adminList(auditStatus));
    }

    @Operation(summary = "商家回复")
    @PutMapping("/admin/{id}/reply")
    public Result<Void> reply(@PathVariable Long id, @RequestParam("replyContent") String replyContent) {
        commentService.reply(id, replyContent);
        return Result.success();
    }

    @Operation(summary = "审核评价")
    @PutMapping("/admin/{id}/audit")
    public Result<Void> audit(@PathVariable Long id, @RequestParam("auditStatus") Integer auditStatus) {
        commentService.audit(id, auditStatus);
        return Result.success();
    }
}

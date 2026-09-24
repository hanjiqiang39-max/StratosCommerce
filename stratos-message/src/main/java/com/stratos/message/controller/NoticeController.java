package com.stratos.message.controller;

import com.stratos.common.result.Result;
import com.stratos.message.entity.MessageNotice;
import com.stratos.message.service.NoticeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "系统公告")
@RestController
@RequestMapping("/notice")
@RequiredArgsConstructor
public class NoticeController {

    private final NoticeService noticeService;

    @Operation(summary = "前台公告")
    @GetMapping("/list")
    public Result<List<MessageNotice>> list() {
        return Result.success(noticeService.listPublished());
    }

    @Operation(summary = "公告详情")
    @GetMapping("/{id}")
    public Result<MessageNotice> detail(@PathVariable Long id) {
        return Result.success(noticeService.get(id));
    }

    @Operation(summary = "后台公告列表")
    @GetMapping("/admin/list")
    public Result<List<MessageNotice>> adminList() {
        return Result.success(noticeService.adminList());
    }

    @Operation(summary = "保存公告")
    @PostMapping("/admin")
    public Result<Long> save(@RequestBody MessageNotice notice) {
        return Result.success("保存成功", noticeService.save(notice));
    }

    @Operation(summary = "发布或下线")
    @PutMapping("/admin/{id}/publish")
    public Result<Void> publish(@PathVariable Long id, @RequestParam("status") Integer status) {
        noticeService.publish(id, status);
        return Result.success();
    }

    @Operation(summary = "删除公告")
    @DeleteMapping("/admin/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        noticeService.delete(id);
        return Result.success();
    }
}

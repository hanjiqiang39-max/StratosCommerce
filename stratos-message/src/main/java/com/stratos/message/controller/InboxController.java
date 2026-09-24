package com.stratos.message.controller;

import com.stratos.common.result.Result;
import com.stratos.message.entity.MessageInbox;
import com.stratos.message.service.InboxService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 收件箱控制器
 *
 * @author StratosCommerce
 * @since 1.0.0
 */
@Tag(name = "收件箱管理")
@RestController
@RequestMapping("/inbox")
@RequiredArgsConstructor
public class InboxController {

    private final InboxService inboxService;

    @Operation(summary = "查询用户收件箱")
    @GetMapping("/list/{userId}")
    public Result<List<MessageInbox>> queryUserInbox(@PathVariable("userId") Long userId,
                                                    @RequestParam(value = "status", required = false) Integer status) {
        return Result.success(inboxService.queryUserInbox(userId, status));
    }

    @Operation(summary = "标记消息为已读")
    @PutMapping("/read/{id}")
    public Result<Void> markAsRead(@PathVariable Long id) {
        inboxService.markAsRead(id);
        return Result.success();
    }

    @Operation(summary = "全部标记为已读")
    @PutMapping("/read/all/{userId}")
    public Result<Void> markAllAsRead(@PathVariable Long userId) {
        inboxService.markAllAsRead(userId);
        return Result.success();
    }

    @Operation(summary = "未读消息数量")
    @GetMapping("/unread/count/{userId}")
    public Result<Long> unreadCount(@PathVariable Long userId) {
        return Result.success(inboxService.unreadCount(userId));
    }

    @Operation(summary = "收件箱汇总")
    @GetMapping("/summary/{userId}")
    public Result<Map<String, Object>> inboxSummary(@PathVariable Long userId) {
        Map<String, Object> data = new HashMap<>();
        data.put("unreadCount", inboxService.unreadCount(userId));
        data.put("total", inboxService.queryUserInbox(userId, null).size());
        return Result.success(data);
    }

    @Operation(summary = "删除站内信")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id, @RequestParam(value = "userId", required = false) Long userId) {
        inboxService.delete(id, userId);
        return Result.success();
    }

}

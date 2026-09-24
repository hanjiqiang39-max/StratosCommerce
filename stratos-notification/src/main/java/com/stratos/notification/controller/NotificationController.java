package com.stratos.notification.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.stratos.common.result.Result;
import com.stratos.notification.dto.SendNotificationDTO;
import com.stratos.notification.dto.SendSmsDTO;
import com.stratos.notification.entity.NotificationRecord;
import com.stratos.notification.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 通知控制器
 *
 * @author StratosCommerce
 * @since 1.0.0
 */
@RestController
@RequestMapping("/notification")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/health")
    public Result<String> health() {
        return Result.success("Notification Service is running");
    }

    @PostMapping("/sms/send")
    public Result<Void> sendSms(@Valid @RequestBody SendSmsDTO dto) {
        notificationService.sendSms(dto.getPhone(), dto.getContent(), dto.getTemplateParam());
        return Result.success();
    }

    @GetMapping("/sms/status")
    public Result<java.util.Map<String, Object>> smsStatus() {
        return Result.success(notificationService.smsStatus());
    }

    /**
     * 发送通知
     */
    @PostMapping("/send")
    public Result<Void> sendNotification(@Valid @RequestBody SendNotificationDTO dto) {
        notificationService.sendNotification(dto);
        return Result.success();
    }

    /**
     * 查询用户通知列表
     */
    @GetMapping("/list")
    public Result<List<NotificationRecord>> getUserNotifications(
            @RequestParam("userId") Long userId,
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "size", defaultValue = "10") Integer size) {
        List<NotificationRecord> result = notificationService.getUserNotifications(userId, page, size);
        return Result.success(result);
    }

    /**
     * 标记通知已读
     */
    @PostMapping("/read/{notificationId}")
    public Result<Void> markAsRead(@PathVariable Long notificationId) {
        notificationService.markAsRead(notificationId);
        return Result.success();
    }

    /**
     * 标记所有通知已读
     */
    @PostMapping("/read/all")
    public Result<Void> markAllAsRead(@RequestParam("userId") Long userId) {
        notificationService.markAllAsRead(userId);
        return Result.success();
    }

    /**
     * 获取未读通知数量
     */
    @GetMapping("/unread/count")
    public Result<Long> getUnreadCount(@RequestParam("userId") Long userId) {
        Long count = notificationService.getUnreadCount(userId);
        return Result.success(count);
    }

    @DeleteMapping("/{notificationId}")
    public Result<Void> delete(@PathVariable Long notificationId,
                               @RequestParam(value = "userId", required = false) Long userId) {
        notificationService.delete(notificationId, userId);
        return Result.success();
    }

}

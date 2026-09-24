package com.stratos.notification.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.stratos.notification.dto.SendNotificationDTO;
import com.stratos.notification.entity.NotificationRecord;

import java.util.List;

/**
 * 通知服务接口
 *
 * @author StratosCommerce
 * @since 1.0.0
 */
public interface NotificationService extends IService<NotificationRecord> {

    /**
     * 发送通知
     */
    void sendNotification(SendNotificationDTO dto);

    void sendSms(String phone, String content, String templateParam);

    java.util.Map<String, Object> smsStatus();

    /**
     * 获取用户未读通知列表
     */
    List<NotificationRecord> getUnreadList(Long userId);

    /**
     * 获取用户通知列表
     */
    List<NotificationRecord> getUserNotifications(Long userId, Integer page, Integer size);

    /**
     * 标记为已读
     */
    void markAsRead(Long id);

    /**
     * 批量标记为已读
     */
    void markAllAsRead(Long userId);

    /**
     * 获取未读数量
     */
    Long getUnreadCount(Long userId);

    void delete(Long id, Long userId);

}

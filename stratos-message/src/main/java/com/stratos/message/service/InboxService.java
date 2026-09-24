package com.stratos.message.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.stratos.message.entity.MessageInbox;

import java.util.List;

/**
 * 站内信服务接�?
 *
 * @author StratosCommerce
 * @since 1.0.0
 */
public interface InboxService extends IService<MessageInbox> {

    /**
     * 发送站内信
     */
    void sendInbox(Long userId, String title, String content, String type, Long bizId, String bizType, String linkUrl);

    /**
     * 查询用户站内�?
     */
    List<MessageInbox> queryUserInbox(Long userId, Integer isRead);

    /**
     * 标记已读
     */
    void markAsRead(Long id);

    /**
     * 全部标记已读
     */
    void markAllAsRead(Long userId);

    /**
     * 未读数量
     */
    Long unreadCount(Long userId);

    void delete(Long id, Long userId);

}

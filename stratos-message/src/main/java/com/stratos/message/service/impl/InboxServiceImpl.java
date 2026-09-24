package com.stratos.message.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.stratos.message.entity.MessageInbox;
import com.stratos.message.mapper.MessageInboxMapper;
import com.stratos.message.service.InboxService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 站内信服务实�?
 *
 * @author StratosCommerce
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InboxServiceImpl extends ServiceImpl<MessageInboxMapper, MessageInbox> implements InboxService {

    @Override
    public void sendInbox(Long userId, String title, String content, String type,
                         Long bizId, String bizType, String linkUrl) {
        MessageInbox inbox = new MessageInbox();
        inbox.setUserId(userId);
        inbox.setTitle(title);
        inbox.setContent(content);
        inbox.setIsRead(0);
        inbox.setBizId(bizId != null ? String.valueOf(bizId) : null);
        inbox.setBizType(parseBizType(type != null ? type : bizType));
        inbox.setJumpUrl(linkUrl);
        save(inbox);
    }

    @Override
    public List<MessageInbox> queryUserInbox(Long userId, Integer isRead) {
        return lambdaQuery()
                .eq(MessageInbox::getUserId, userId)
                .eq(isRead != null, MessageInbox::getIsRead, isRead)
                .orderByDesc(MessageInbox::getCreateTime)
                .list();
    }

    @Override
    public void markAsRead(Long id) {
        MessageInbox inbox = getById(id);
        if (inbox != null && inbox.getIsRead() == 0) {
            inbox.setIsRead(1);
            inbox.setReadTime(LocalDateTime.now());
            updateById(inbox);
        }
    }

    @Override
    public void markAllAsRead(Long userId) {
        lambdaUpdate()
                .eq(MessageInbox::getUserId, userId)
                .eq(MessageInbox::getIsRead, 0)
                .set(MessageInbox::getIsRead, 1)
                .set(MessageInbox::getReadTime, LocalDateTime.now())
                .update();
    }

    @Override
    public Long unreadCount(Long userId) {
        return lambdaQuery()
                .eq(MessageInbox::getUserId, userId)
                .eq(MessageInbox::getIsRead, 0)
                .count();
    }

    @Override
    public void delete(Long id, Long userId) {
        lambdaUpdate()
                .eq(MessageInbox::getId, id)
                .eq(userId != null, MessageInbox::getUserId, userId)
                .remove();
    }

    private Integer parseBizType(String bizType) {
        if (bizType == null || bizType.isBlank()) {
            return 1;
        }
        try {
            return Integer.parseInt(bizType);
        } catch (NumberFormatException ex) {
            return 1;
        }
    }

}

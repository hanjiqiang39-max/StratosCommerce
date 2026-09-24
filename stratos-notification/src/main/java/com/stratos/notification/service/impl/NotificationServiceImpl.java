package com.stratos.notification.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.stratos.common.util.SnowflakeIdGenerator;
import com.stratos.notification.dto.SendNotificationDTO;
import com.stratos.notification.entity.NotificationRecord;
import com.stratos.notification.mapper.NotificationRecordMapper;
import com.stratos.notification.service.NotificationService;
import com.stratos.notification.sms.SmsSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 通知服务实现
 *
 * @author StratosCommerce
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl extends ServiceImpl<NotificationRecordMapper, NotificationRecord> implements NotificationService {

    private final SnowflakeIdGenerator snowflakeIdGenerator;
    private final SmsSender smsSender;
    private final com.stratos.notification.config.SmsProperties smsProperties;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sendNotification(SendNotificationDTO dto) {
        NotificationRecord record = new NotificationRecord();
        record.setId(snowflakeIdGenerator.nextId());
        record.setUserId(dto.getUserId());
        record.setNotificationType(dto.getType());
        record.setTitle(dto.getTitle());
        record.setContent(dto.getContent());
        record.setJumpUrl(dto.getLinkUrl());
        record.setIsRead(0);
        record.setPriority(1);
        record.setCreateTime(LocalDateTime.now());
        record.setUpdateTime(LocalDateTime.now());
        save(record);
        if (Boolean.TRUE.equals(dto.getSendSms()) && StringUtils.hasText(dto.getPhone())) {
            sendSms(dto.getPhone(), dto.getContent(), null);
        }
        log.info("通知已发送，用户ID: {}, 标题: {}", dto.getUserId(), dto.getTitle());
    }

    @Override
    public void sendSms(String phone, String content, String templateParam) {
        smsSender.send(phone, content, templateParam);
    }

    @Override
    public java.util.Map<String, Object> smsStatus() {
        java.util.Map<String, Object> data = new java.util.LinkedHashMap<>();
        data.put("vendor", smsProperties.getVendor());
        data.put("ready", smsProperties.aliyunReady());
        data.put("signName", smsProperties.getSignName());
        return data;
    }

    @Override
    public List<NotificationRecord> getUnreadList(Long userId) {
        return lambdaQuery()
                .eq(NotificationRecord::getUserId, userId)
                .eq(NotificationRecord::getIsRead, 0)
                .orderByDesc(NotificationRecord::getCreateTime)
                .list();
    }

    @Override
    public List<NotificationRecord> getUserNotifications(Long userId, Integer page, Integer size) {
        return lambdaQuery()
                .eq(NotificationRecord::getUserId, userId)
                .orderByDesc(NotificationRecord::getCreateTime)
                .last("LIMIT " + (page - 1) * size + "," + size)
                .list();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markAsRead(Long id) {
        NotificationRecord record = getById(id);
        if (record != null && record.getIsRead() == 0) {
            record.setIsRead(1);
            updateById(record);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markAllAsRead(Long userId) {
        lambdaUpdate()
                .eq(NotificationRecord::getUserId, userId)
                .eq(NotificationRecord::getIsRead, 0)
                .set(NotificationRecord::getIsRead, 1)
                .update();
    }

    @Override
    public Long getUnreadCount(Long userId) {
        return lambdaQuery()
                .eq(NotificationRecord::getUserId, userId)
                .eq(NotificationRecord::getIsRead, 0)
                .count();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id, Long userId) {
        lambdaUpdate()
                .eq(NotificationRecord::getId, id)
                .eq(userId != null, NotificationRecord::getUserId, userId)
                .remove();
    }

}

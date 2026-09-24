package com.stratos.message.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.stratos.common.result.ResultCode;
import com.stratos.common.exception.BusinessException;
import com.stratos.message.dto.SendMessageDTO;
import com.stratos.message.entity.MessageRecord;
import com.stratos.message.entity.MessageTemplate;
import com.stratos.message.mapper.MessageRecordMapper;
import com.stratos.message.mapper.MessageTemplateMapper;
import com.stratos.message.service.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 消息服务实现
 *
 * @author StratosCommerce
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MessageServiceImpl extends ServiceImpl<MessageRecordMapper, MessageRecord> implements MessageService {

    private final MessageTemplateMapper templateMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sendMessage(SendMessageDTO dto) {
        MessageTemplate template = templateMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<MessageTemplate>()
                        .eq("template_code", dto.getTemplateCode())
                        .eq("status", 1)
        );

        if (template == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "消息模板不存在或已停用");
        }

        String title = renderTemplate(template.getTitleTemplate(), dto.getParams());
        String content = renderTemplate(template.getContentTemplate(), dto.getParams());

        MessageRecord record = new MessageRecord();
        record.setMsgNo(UUID.randomUUID().toString().replace("-", ""));
        record.setTemplateCode(dto.getTemplateCode());
        record.setChannel(dto.getChannel());
        record.setReceiverId(dto.getUserId());
        record.setReceiverTargetEncrypted(dto.getReceiver());
        record.setTitle(title);
        record.setContent(content);
        record.setSendStatus(0);
        record.setRetryCount(0);
        record.setMaxRetry(3);
        record.setBizId(dto.getBizId() != null ? String.valueOf(dto.getBizId()) : null);
        record.setBizType(parseBizType(dto.getBizType()));

        save(record);

        // 异步调用短信/邮件/推送服务
        simulateSend(record);
    }

    @Override
    public void batchSendMessage(List<SendMessageDTO> dtos) {
        for (SendMessageDTO dto : dtos) {
            try {
                sendMessage(dto);
            } catch (Exception e) {
                log.error("批量发送消息失败: {}", dto, e);
            }
        }
    }

    @Override
    public List<MessageRecord> queryUserMessages(Long userId, Integer status) {
        return lambdaQuery()
                .eq(MessageRecord::getReceiverId, userId)
                .eq(status != null, MessageRecord::getSendStatus, status)
                .orderByDesc(MessageRecord::getCreateTime)
                .list();
    }

    private void simulateSend(MessageRecord record) {
        record.setSendStatus(2);
        record.setSendTime(LocalDateTime.now());
        updateById(record);
        log.info("消息发送成功: {}", record.getMsgNo());
    }

    private Integer parseBizType(String bizType) {
        if (bizType == null || bizType.isBlank()) {
            return 2;
        }
        try {
            return Integer.parseInt(bizType);
        } catch (NumberFormatException ex) {
            return 2;
        }
    }

    private String renderTemplate(String template, Map<String, Object> params) {
        if (template == null || params == null) {
            return template;
        }
        String result = template;
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            result = result.replace("${" + entry.getKey() + "}", String.valueOf(entry.getValue()));
        }
        return result;
    }

}

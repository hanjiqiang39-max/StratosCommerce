package com.stratos.message.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.stratos.message.dto.SendMessageDTO;
import com.stratos.message.entity.MessageRecord;

import java.util.List;

/**
 * 消息服务接口
 *
 * @author StratosCommerce
 * @since 1.0.0
 */
public interface MessageService extends IService<MessageRecord> {

    /**
     * 发送消息
     */
    void sendMessage(SendMessageDTO dto);

    /**
     * 批量发送消息
     */
    void batchSendMessage(List<SendMessageDTO> dtos);

    /**
     * 查询用户消息记录
     */
    List<MessageRecord> queryUserMessages(Long userId, Integer status);

}

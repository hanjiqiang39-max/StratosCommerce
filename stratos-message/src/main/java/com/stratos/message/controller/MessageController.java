package com.stratos.message.controller;

import com.stratos.common.result.Result;
import com.stratos.message.dto.SendMessageDTO;
import com.stratos.message.entity.MessageRecord;
import com.stratos.message.service.MessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 消息控制器
 *
 * @author StratosCommerce
 * @since 1.0.0
 */
@Tag(name = "消息管理")
@RestController
@RequestMapping("/message")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    @Operation(summary = "发送系统消息")
    @PostMapping("/send")
    public Result<Void> sendMessage(@Valid @RequestBody SendMessageDTO dto) {
        messageService.sendMessage(dto);
        return Result.success();
    }

    @Operation(summary = "批量发送系统消息")
    @PostMapping("/send/batch")
    public Result<Void> batchSendMessage(@Valid @RequestBody List<SendMessageDTO> dtos) {
        messageService.batchSendMessage(dtos);
        return Result.success();
    }

    @Operation(summary = "查询用户消息记录")
    @GetMapping("/list/{userId}")
    public Result<List<MessageRecord>> queryUserMessages(
            @PathVariable Long userId,
            @RequestParam(value = "status", required = false) Integer status) {
        return Result.success(messageService.queryUserMessages(userId, status));
    }

}

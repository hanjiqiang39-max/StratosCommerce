package com.stratos.message.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Map;

/**
 * 发送消息DTO
 *
 * @author StratosCommerce
 * @since 1.0.0
 */
@Data
public class SendMessageDTO {

    @NotBlank(message = "模板编码不能为空")
    private String templateCode;

    @NotNull(message = "渠道不能为空")
    private Integer channel;

    @NotNull(message = "用户ID不能为空")
    private Long userId;

    private String receiver;

    private Map<String, Object> params;

    private Long bizId;

    private String bizType;

}

package com.stratos.notification.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 发送通知DTO
 *
 * @author StratosCommerce
 * @since 1.0.0
 */
@Data
public class SendNotificationDTO {

    @NotNull(message = "用户ID不能为空")
    private Long userId;

    @NotNull(message = "通知类型不能为空")
    private Integer type;

    @NotBlank(message = "通知标题不能为空")
    private String title;

    @NotBlank(message = "通知内容不能为空")
    private String content;

    private String linkUrl;

    /**
     * 同时发短信时填写手机号
     */
    private String phone;

    private Boolean sendSms;

}

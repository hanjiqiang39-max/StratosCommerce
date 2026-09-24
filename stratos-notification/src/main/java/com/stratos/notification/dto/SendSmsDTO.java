package com.stratos.notification.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 发送短信
 *
 * @author StratosCommerce
 * @since 1.0.0
 */
@Data
public class SendSmsDTO {

    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    @NotBlank(message = "短信内容不能为空")
    private String content;

    /**
     * 阿里云模板变量 JSON，例如 {"code":"123456"}
     */
    private String templateParam;

}

package com.stratos.common.result;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 统一响应状态码枚举
 *
 * @author StratosCommerce
 * @since 1.0.0
 */
@Getter
@AllArgsConstructor
public enum ResultCode {

    // 成功
    SUCCESS(200, "操作成功"),

    // 客户端错误 4xx
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未授权，请先登录"),
    FORBIDDEN(403, "权限不足，拒绝访问"),
    NOT_FOUND(404, "请求的资源不存在"),
    METHOD_NOT_ALLOWED(405, "请求方法不支持"),
    REQUEST_TIMEOUT(408, "请求超时"),
    CONFLICT(409, "数据冲突"),
    TOO_MANY_REQUESTS(429, "请求过于频繁，请稍后再试"),

    // 服务端错误 5xx
    INTERNAL_SERVER_ERROR(500, "服务器内部错误"),
    SERVICE_UNAVAILABLE(503, "服务暂时不可用"),
    GATEWAY_TIMEOUT(504, "网关超时"),

    // 业务错误 1xxx
    BUSINESS_ERROR(1000, "业务处理失败"),
    PARAM_VALIDATE_ERROR(1001, "参数校验失败"),
    DATA_NOT_FOUND(1002, "数据不存在"),
    DATA_ALREADY_EXISTS(1003, "数据已存在"),
    OPERATION_FAILED(1004, "操作失败"),
    
    // 用户相关错误 2xxx
    USER_NOT_FOUND(2001, "用户不存在"),
    USER_ALREADY_EXISTS(2002, "用户已存在"),
    USERNAME_OR_PASSWORD_ERROR(2003, "用户名或密码错误"),
    USER_ACCOUNT_DISABLED(2004, "用户账号已禁用"),
    USER_ACCOUNT_LOCKED(2005, "用户账号已锁定"),
    USER_PASSWORD_ERROR(2006, "密码错误"),
    USER_PHONE_EXISTS(2007, "手机号已被注册"),
    USER_EMAIL_EXISTS(2008, "邮箱已被注册"),
    
    // Token相关错误 3xxx
    TOKEN_INVALID(3001, "Token无效"),
    TOKEN_EXPIRED(3002, "Token已过期"),
    TOKEN_MISSING(3003, "Token缺失"),
    REFRESH_TOKEN_INVALID(3004, "刷新令牌无效"),
    
    // 商品相关错误 4xxx
    PRODUCT_NOT_FOUND(4001, "商品不存在"),
    PRODUCT_STOCK_INSUFFICIENT(4002, "商品库存不足"),
    PRODUCT_OFF_SHELF(4003, "商品已下架"),
    PRODUCT_CATEGORY_NOT_FOUND(4004, "商品分类不存在"),
    
    // 订单相关错误 5xxx
    ORDER_NOT_FOUND(5001, "订单不存在"),
    ORDER_STATUS_ERROR(5002, "订单状态异常"),
    ORDER_CANNOT_CANCEL(5003, "订单不可取消"),
    ORDER_ALREADY_PAID(5004, "订单已支付"),
    ORDER_TIMEOUT(5005, "订单已超时"),
    
    // 支付相关错误 6xxx
    PAYMENT_FAILED(6001, "支付失败"),
    PAYMENT_CHANNEL_ERROR(6002, "支付渠道错误"),
    PAYMENT_AMOUNT_ERROR(6003, "支付金额错误"),
    REFUND_FAILED(6004, "退款失败"),
    
    // 库存相关错误 7xxx
    INVENTORY_INSUFFICIENT(7001, "库存不足"),
    INVENTORY_LOCK_FAILED(7002, "库存锁定失败"),
    
    // 优惠券相关错误 8xxx
    COUPON_NOT_FOUND(8001, "优惠券不存在"),
    COUPON_EXPIRED(8002, "优惠券已过期"),
    COUPON_STOCK_EMPTY(8003, "优惠券已领完"),
    COUPON_ALREADY_RECEIVED(8004, "优惠券已领取"),
    COUPON_NOT_AVAILABLE(8005, "优惠券不可用"),
    
    // 第三方服务错误 9xxx
    OSS_UPLOAD_FAILED(9001, "文件上传失败"),
    SMS_SEND_FAILED(9002, "短信发送失败"),
    EMAIL_SEND_FAILED(9003, "邮件发送失败");

    /**
     * 状态码
     */
    private final Integer code;

    /**
     * 响应消息
     */
    private final String message;

}

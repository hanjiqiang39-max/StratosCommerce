package com.stratos.common.constant;

/**
 * 系统常量
 *
 * @author StratosCommerce
 * @since 1.0.0
 */
public interface CommonConstants {

    /**
     * 成功标记
     */
    Integer SUCCESS = 200;

    /**
     * 失败标记
     */
    Integer FAIL = 500;

    /**
     * 是否标识 - 是
     */
    Integer YES = 1;

    /**
     * 是否标识 - 否
     */
    Integer NO = 0;

    /**
     * 状态 - 启用
     */
    Integer STATUS_ENABLE = 1;

    /**
     * 状态 - 禁用
     */
    Integer STATUS_DISABLE = 0;

    /**
     * 删除标识 - 已删除
     */
    Integer DELETED = 1;

    /**
     * 删除标识 - 未删除
     */
    Integer NOT_DELETED = 0;

    /**
     * 默认页码
     */
    Integer DEFAULT_PAGE_NUM = 1;

    /**
     * 默认每页大小
     */
    Integer DEFAULT_PAGE_SIZE = 10;

    /**
     * 最大每页大小
     */
    Integer MAX_PAGE_SIZE = 100;

    /**
     * UTF-8 编码
     */
    String UTF8 = "UTF-8";

    /**
     * 默认日期格式
     */
    String DATE_FORMAT = "yyyy-MM-dd";

    /**
     * 默认时间格式
     */
    String DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    /**
     * Token 请求头
     */
    String TOKEN_HEADER = "Authorization";

    /**
     * Token 前缀
     */
    String TOKEN_PREFIX = "Bearer ";

    /**
     * 用户ID请求头
     */
    String USER_ID_HEADER = "X-User-Id";

    /**
     * 用户名请求头
     */
    String USERNAME_HEADER = "X-Username";

    /**
     * 租户ID请求头
     */
    String TENANT_ID_HEADER = "X-Tenant-Id";

}

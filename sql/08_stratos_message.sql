-- =============================================================
-- 消息服务数据库表结构 (stratos_message)
-- 职责：站内信、短信、邮件、APP推送、消息模板
-- =============================================================

USE `stratos_message`;

-- 消息模板表
DROP TABLE IF EXISTS `message_template`;
CREATE TABLE `message_template` (
    `id` BIGINT UNSIGNED NOT NULL COMMENT '模板ID（雪花算法）',
    `template_code` VARCHAR(64) NOT NULL COMMENT '模板编码，如 ORDER_PAID_SMS',
    `template_name` VARCHAR(128) NOT NULL COMMENT '模板名称',
    `channel` TINYINT NOT NULL COMMENT '发送渠道：1=站内信，2=短信，3=邮件，4=APP推送，5=微信',
    `biz_type` TINYINT NOT NULL COMMENT '业务类型：1=账号，2=订单，3=支付，4=物流，5=营销，6=售后',
    `title_template` VARCHAR(255) DEFAULT NULL COMMENT '标题模板，占位符 ${name}',
    `content_template` TEXT NOT NULL COMMENT '内容模板，占位符 ${name}',
    `third_party_template_id` VARCHAR(64) DEFAULT NULL COMMENT '第三方模板ID（阿里云短信等）',
    `param_desc` JSON DEFAULT NULL COMMENT '参数说明（JSON）',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0=停用，1=启用',
    `remark` VARCHAR(255) DEFAULT NULL COMMENT '备注',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by` VARCHAR(64) DEFAULT NULL COMMENT '创建人',
    `update_by` VARCHAR(64) DEFAULT NULL COMMENT '更新人',
    `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0=未删除，1=已删除',
    `version` INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_template_code` (`template_code`),
    KEY `idx_channel_biz` (`channel`, `biz_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='消息模板表';

-- 消息发送记录表
DROP TABLE IF EXISTS `message_record`;
CREATE TABLE `message_record` (
    `id` BIGINT UNSIGNED NOT NULL COMMENT '记录ID（雪花算法）',
    `msg_no` VARCHAR(64) NOT NULL COMMENT '消息流水号（业务唯一，用于幂等）',
    `template_code` VARCHAR(64) DEFAULT NULL COMMENT '模板编码',
    `channel` TINYINT NOT NULL COMMENT '发送渠道：1=站内信，2=短信，3=邮件，4=APP推送，5=微信',
    `biz_type` TINYINT NOT NULL COMMENT '业务类型：1=账号，2=订单，3=支付，4=物流，5=营销，6=售后',
    `biz_id` VARCHAR(64) DEFAULT NULL COMMENT '业务ID（订单号等）',
    `receiver_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '接收人用户ID',
    `receiver_target_encrypted` VARCHAR(255) DEFAULT NULL COMMENT '接收目标（手机号/邮箱，密文）',
    `receiver_target_hash` CHAR(64) DEFAULT NULL COMMENT '接收目标哈希（用于查询）',
    `title` VARCHAR(255) DEFAULT NULL COMMENT '渲染后标题',
    `content` TEXT DEFAULT NULL COMMENT '渲染后内容',
    `send_status` TINYINT NOT NULL DEFAULT 0 COMMENT '发送状态：0=待发送，1=发送中，2=成功，3=失败，4=已取消',
    `retry_count` TINYINT NOT NULL DEFAULT 0 COMMENT '已重试次数',
    `max_retry` TINYINT NOT NULL DEFAULT 3 COMMENT '最大重试次数',
    `next_retry_time` DATETIME DEFAULT NULL COMMENT '下次重试时间',
    `send_time` DATETIME DEFAULT NULL COMMENT '实际发送时间',
    `third_party_msg_id` VARCHAR(128) DEFAULT NULL COMMENT '第三方消息ID',
    `fail_code` VARCHAR(64) DEFAULT NULL COMMENT '失败错误码',
    `fail_reason` VARCHAR(512) DEFAULT NULL COMMENT '失败原因',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0=未删除，1=已删除',
    `version` INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_msg_no` (`msg_no`),
    KEY `idx_receiver_id` (`receiver_id`),
    KEY `idx_send_status_retry` (`send_status`, `next_retry_time`),
    KEY `idx_biz` (`biz_type`, `biz_id`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='消息发送记录表';

-- 站内信表
DROP TABLE IF EXISTS `message_inbox`;
CREATE TABLE `message_inbox` (
    `id` BIGINT UNSIGNED NOT NULL COMMENT '站内信ID（雪花算法）',
    `user_id` BIGINT UNSIGNED NOT NULL COMMENT '接收人用户ID',
    `biz_type` TINYINT NOT NULL COMMENT '业务类型：1=账号，2=订单，3=支付，4=物流，5=营销，6=售后',
    `biz_id` VARCHAR(64) DEFAULT NULL COMMENT '业务ID（订单号等）',
    `title` VARCHAR(255) NOT NULL COMMENT '标题',
    `content` TEXT NOT NULL COMMENT '正文',
    `jump_url` VARCHAR(512) DEFAULT NULL COMMENT '跳转链接',
    `is_read` TINYINT NOT NULL DEFAULT 0 COMMENT '是否已读：0=未读，1=已读',
    `read_time` DATETIME DEFAULT NULL COMMENT '阅读时间',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0=未删除，1=已删除',
    PRIMARY KEY (`id`),
    KEY `idx_user_read_time` (`user_id`, `is_read`, `create_time`),
    KEY `idx_biz` (`biz_type`, `biz_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='站内信表';

-- 系统公告表
DROP TABLE IF EXISTS `message_notice`;
CREATE TABLE `message_notice` (
    `id` BIGINT UNSIGNED NOT NULL COMMENT '公告ID（雪花算法）',
    `title` VARCHAR(255) NOT NULL COMMENT '公告标题',
    `content` TEXT NOT NULL COMMENT '公告内容（HTML，输出需转义）',
    `notice_type` TINYINT NOT NULL DEFAULT 1 COMMENT '公告类型：1=系统通知，2=活动公告，3=维护通知',
    `target_scope` TINYINT NOT NULL DEFAULT 0 COMMENT '可见范围：0=全体用户，1=指定会员等级，2=指定用户',
    `target_value` JSON DEFAULT NULL COMMENT '范围取值（等级ID或用户ID数组）',
    `publish_status` TINYINT NOT NULL DEFAULT 0 COMMENT '发布状态：0=草稿，1=已发布，2=已下线',
    `publish_time` DATETIME DEFAULT NULL COMMENT '发布时间',
    `expire_time` DATETIME DEFAULT NULL COMMENT '过期时间',
    `top_flag` TINYINT NOT NULL DEFAULT 0 COMMENT '是否置顶：0=否，1=是',
    `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序（越小越前）',
    `read_count` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '阅读次数',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by` VARCHAR(64) DEFAULT NULL COMMENT '创建人',
    `update_by` VARCHAR(64) DEFAULT NULL COMMENT '更新人',
    `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0=未删除，1=已删除',
    `version` INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    PRIMARY KEY (`id`),
    KEY `idx_publish` (`publish_status`, `publish_time`),
    KEY `idx_top_sort` (`top_flag`, `sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统公告表';

-- 短信验证码表（防刷、限频、校验）
DROP TABLE IF EXISTS `message_verify_code`;
CREATE TABLE `message_verify_code` (
    `id` BIGINT UNSIGNED NOT NULL COMMENT '验证码ID（雪花算法）',
    `target_hash` CHAR(64) NOT NULL COMMENT '手机号/邮箱哈希',
    `scene` TINYINT NOT NULL COMMENT '使用场景：1=注册，2=登录，3=改密，4=支付，5=换绑',
    `code_hash` CHAR(64) NOT NULL COMMENT '验证码哈希（禁止明文存储）',
    `send_ip` VARCHAR(45) DEFAULT NULL COMMENT '请求IP',
    `expire_time` DATETIME NOT NULL COMMENT '过期时间',
    `verify_status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0=未使用，1=已使用，2=已失效',
    `verify_count` TINYINT NOT NULL DEFAULT 0 COMMENT '校验尝试次数（超限锁定）',
    `verify_time` DATETIME DEFAULT NULL COMMENT '校验通过时间',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_target_scene` (`target_hash`, `scene`, `create_time`),
    KEY `idx_expire_time` (`expire_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='验证码表';

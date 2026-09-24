-- =============================================================
-- 通知服务数据库表结构 (stratos_notification)
-- =============================================================

USE `stratos_notification`;

-- 通知记录表
DROP TABLE IF EXISTS `notification_record`;
CREATE TABLE `notification_record` (
    `id` BIGINT UNSIGNED NOT NULL COMMENT '通知ID',
    `user_id` BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
    `notification_type` TINYINT NOT NULL COMMENT '通知类型：1=订单通知，2=支付通知，3=物流通知，4=营销通知，5=系统通知',
    `title` VARCHAR(255) NOT NULL COMMENT '通知标题',
    `content` TEXT NOT NULL COMMENT '通知内容',
    `biz_type` TINYINT DEFAULT NULL COMMENT '业务类型：1=订单，2=支付，3=物流，4=秒杀，5=优惠券',
    `biz_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '业务ID',
    `biz_no` VARCHAR(64) DEFAULT NULL COMMENT '业务单号',
    `jump_url` VARCHAR(512) DEFAULT NULL COMMENT '跳转链接',
    `is_read` TINYINT NOT NULL DEFAULT 0 COMMENT '是否已读：0=未读，1=已读',
    `read_time` DATETIME DEFAULT NULL COMMENT '阅读时间',
    `priority` TINYINT DEFAULT 1 COMMENT '优先级：1=普通，2=重要，3=紧急',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `is_deleted` TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `idx_user_read_time` (`user_id`, `is_read`, `create_time`),
    KEY `idx_biz` (`biz_type`, `biz_id`),
    KEY `idx_biz_no` (`biz_no`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='通知记录表';

-- 通知配置表（用户偏好设置）
DROP TABLE IF EXISTS `notification_config`;
CREATE TABLE `notification_config` (
    `id` BIGINT UNSIGNED NOT NULL COMMENT '配置ID',
    `user_id` BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
    `notification_type` TINYINT NOT NULL COMMENT '通知类型：1=订单通知，2=支付通知，3=物流通知，4=营销通知，5=系统通知',
    `is_enabled` TINYINT NOT NULL DEFAULT 1 COMMENT '是否启用：0=禁用，1=启用',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_type` (`user_id`, `notification_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='通知配置表';

-- 批量通知任务表
DROP TABLE IF EXISTS `notification_batch_task`;
CREATE TABLE `notification_batch_task` (
    `id` BIGINT UNSIGNED NOT NULL COMMENT '任务ID',
    `task_name` VARCHAR(128) NOT NULL COMMENT '任务名称',
    `notification_type` TINYINT NOT NULL COMMENT '通知类型',
    `title` VARCHAR(255) NOT NULL COMMENT '通知标题',
    `content` TEXT NOT NULL COMMENT '通知内容',
    `target_scope` TINYINT NOT NULL COMMENT '目标范围：1=全体用户，2=指定会员等级，3=指定用户列表',
    `target_value` JSON DEFAULT NULL COMMENT '目标值（等级ID数组或用户ID数组）',
    `total_count` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '总用户数',
    `sent_count` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '已发送数',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0=待发送，1=发送中，2=已完成，3=已取消',
    `scheduled_time` DATETIME DEFAULT NULL COMMENT '计划发送时间',
    `start_time` DATETIME DEFAULT NULL COMMENT '开始时间',
    `end_time` DATETIME DEFAULT NULL COMMENT '结束时间',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `create_by` VARCHAR(64) DEFAULT NULL,
    `is_deleted` TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `idx_status` (`status`),
    KEY `idx_scheduled_time` (`scheduled_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='批量通知任务表';

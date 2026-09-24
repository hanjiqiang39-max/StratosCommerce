-- =============================================================
-- 支付服务数据库表结构 (stratos_payment)
-- =============================================================

USE `stratos_payment`;

-- 支付单表
DROP TABLE IF EXISTS `pay_order`;
CREATE TABLE `pay_order` (
    `id` BIGINT UNSIGNED NOT NULL COMMENT '支付单ID',
    `pay_no` VARCHAR(64) NOT NULL COMMENT '支付单号（唯一）',
    `out_trade_no` VARCHAR(64) NOT NULL COMMENT '外部交易号（对接支付宝/微信）',
    `order_id` BIGINT UNSIGNED NOT NULL COMMENT '订单ID',
    `order_no` VARCHAR(64) NOT NULL COMMENT '订单号',
    `user_id` BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
    `pay_channel` TINYINT NOT NULL COMMENT '支付渠道：1=支付宝，2=微信，3=银联，4=余额',
    `pay_type` TINYINT NOT NULL COMMENT '支付类型：1=订单支付，2=充值',
    `pay_amount` DECIMAL(12,2) NOT NULL COMMENT '支付金额',
    `currency` VARCHAR(8) DEFAULT 'CNY' COMMENT '币种',
    `status` TINYINT DEFAULT 0 COMMENT '支付状态：0=待支付，1=支付中，2=支付成功，3=支付失败，4=已关闭',
    `pay_time` DATETIME DEFAULT NULL COMMENT '支付完成时间',
    `expire_time` DATETIME NOT NULL COMMENT '过期时间（默认30分钟）',
    `notify_url` VARCHAR(255) DEFAULT NULL COMMENT '回调通知URL',
    `return_url` VARCHAR(255) DEFAULT NULL COMMENT '同步跳转URL',
    `subject` VARCHAR(255) DEFAULT NULL COMMENT '支付主题',
    `body` VARCHAR(512) DEFAULT NULL COMMENT '支付描述',
    `third_party_trade_no` VARCHAR(128) DEFAULT NULL COMMENT '第三方交易流水号',
    `third_party_user_id` VARCHAR(128) DEFAULT NULL COMMENT '第三方用户ID',
    `error_code` VARCHAR(64) DEFAULT NULL COMMENT '错误码',
    `error_msg` VARCHAR(255) DEFAULT NULL COMMENT '错误信息',
    `notify_count` INT DEFAULT 0 COMMENT '回调通知次数',
    `last_notify_time` DATETIME DEFAULT NULL COMMENT '最后通知时间',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `is_deleted` TINYINT NOT NULL DEFAULT 0,
    `version` INT NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_pay_no` (`pay_no`),
    UNIQUE KEY `uk_out_trade_no` (`out_trade_no`),
    KEY `idx_order_no` (`order_no`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_status` (`status`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='支付单表';

-- 支付渠道配置表
DROP TABLE IF EXISTS `pay_channel_config`;
CREATE TABLE `pay_channel_config` (
    `id` BIGINT UNSIGNED NOT NULL COMMENT '配置ID',
    `channel_code` VARCHAR(32) NOT NULL COMMENT '渠道编码：alipay/wechatpay/unionpay',
    `channel_name` VARCHAR(64) NOT NULL COMMENT '渠道名称',
    `app_id` VARCHAR(128) NOT NULL COMMENT '应用ID',
    `merchant_id` VARCHAR(128) NOT NULL COMMENT '商户号',
    `public_key` TEXT DEFAULT NULL COMMENT '公钥',
    `private_key` TEXT DEFAULT NULL COMMENT '私钥',
    `api_key` VARCHAR(255) DEFAULT NULL COMMENT 'API密钥',
    `notify_url` VARCHAR(255) DEFAULT NULL COMMENT '异步通知URL',
    `return_url` VARCHAR(255) DEFAULT NULL COMMENT '同步返回URL',
    `sign_type` VARCHAR(16) DEFAULT 'RSA2' COMMENT '签名类型',
    `env` TINYINT DEFAULT 1 COMMENT '环境：1=生产，2=沙箱',
    `status` TINYINT DEFAULT 1 COMMENT '状态：0=禁用，1=启用',
    `remark` VARCHAR(512) DEFAULT NULL COMMENT '备注',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `is_deleted` TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_channel_code` (`channel_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='支付渠道配置表';

-- 退款单表
DROP TABLE IF EXISTS `pay_refund`;
CREATE TABLE `pay_refund` (
    `id` BIGINT UNSIGNED NOT NULL COMMENT '退款单ID',
    `refund_no` VARCHAR(64) NOT NULL COMMENT '退款单号',
    `out_refund_no` VARCHAR(64) NOT NULL COMMENT '外部退款单号',
    `pay_id` BIGINT UNSIGNED NOT NULL COMMENT '支付单ID',
    `pay_no` VARCHAR(64) NOT NULL COMMENT '支付单号',
    `order_id` BIGINT UNSIGNED NOT NULL COMMENT '订单ID',
    `order_no` VARCHAR(64) NOT NULL COMMENT '订单号',
    `user_id` BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
    `refund_channel` TINYINT NOT NULL COMMENT '退款渠道：1=支付宝，2=微信，3=银联，4=余额',
    `refund_amount` DECIMAL(12,2) NOT NULL COMMENT '退款金额',
    `refund_reason` VARCHAR(255) DEFAULT NULL COMMENT '退款原因',
    `status` TINYINT DEFAULT 0 COMMENT '退款状态：0=待退款，1=退款中，2=退款成功，3=退款失败',
    `refund_time` DATETIME DEFAULT NULL COMMENT '退款完成时间',
    `third_party_refund_no` VARCHAR(128) DEFAULT NULL COMMENT '第三方退款流水号',
    `error_code` VARCHAR(64) DEFAULT NULL COMMENT '错误码',
    `error_msg` VARCHAR(255) DEFAULT NULL COMMENT '错误信息',
    `notify_count` INT DEFAULT 0 COMMENT '回调通知次数',
    `last_notify_time` DATETIME DEFAULT NULL COMMENT '最后通知时间',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `is_deleted` TINYINT NOT NULL DEFAULT 0,
    `version` INT NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_refund_no` (`refund_no`),
    UNIQUE KEY `uk_out_refund_no` (`out_refund_no`),
    KEY `idx_pay_no` (`pay_no`),
    KEY `idx_order_no` (`order_no`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_status` (`status`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='退款单表';

-- 对账记录表
DROP TABLE IF EXISTS `pay_reconcile`;
CREATE TABLE `pay_reconcile` (
    `id` BIGINT UNSIGNED NOT NULL COMMENT '对账ID',
    `reconcile_date` DATE NOT NULL COMMENT '对账日期',
    `channel_code` VARCHAR(32) NOT NULL COMMENT '支付渠道编码',
    `total_count` INT UNSIGNED DEFAULT 0 COMMENT '总笔数',
    `total_amount` DECIMAL(12,2) DEFAULT 0.00 COMMENT '总金额',
    `success_count` INT UNSIGNED DEFAULT 0 COMMENT '成功笔数',
    `success_amount` DECIMAL(12,2) DEFAULT 0.00 COMMENT '成功金额',
    `refund_count` INT UNSIGNED DEFAULT 0 COMMENT '退款笔数',
    `refund_amount` DECIMAL(12,2) DEFAULT 0.00 COMMENT '退款金额',
    `diff_count` INT UNSIGNED DEFAULT 0 COMMENT '差异笔数',
    `diff_amount` DECIMAL(12,2) DEFAULT 0.00 COMMENT '差异金额',
    `status` TINYINT DEFAULT 0 COMMENT '对账状态：0=待对账，1=对账中，2=对账完成，3=有差异',
    `file_url` VARCHAR(512) DEFAULT NULL COMMENT '对账文件URL',
    `remark` VARCHAR(512) DEFAULT NULL COMMENT '备注',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_date_channel` (`reconcile_date`, `channel_code`),
    KEY `idx_reconcile_date` (`reconcile_date`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='对账记录表';

-- 对账差异表
DROP TABLE IF EXISTS `pay_reconcile_diff`;
CREATE TABLE `pay_reconcile_diff` (
    `id` BIGINT UNSIGNED NOT NULL COMMENT '差异ID',
    `reconcile_id` BIGINT UNSIGNED NOT NULL COMMENT '对账记录ID',
    `reconcile_date` DATE NOT NULL COMMENT '对账日期',
    `channel_code` VARCHAR(32) NOT NULL COMMENT '支付渠道',
    `pay_no` VARCHAR(64) NOT NULL COMMENT '支付单号',
    `third_party_trade_no` VARCHAR(128) DEFAULT NULL COMMENT '第三方交易号',
    `diff_type` TINYINT NOT NULL COMMENT '差异类型：1=平台有第三方无，2=第三方有平台无，3=金额不一致，4=状态不一致',
    `platform_amount` DECIMAL(12,2) DEFAULT NULL COMMENT '平台金额',
    `third_party_amount` DECIMAL(12,2) DEFAULT NULL COMMENT '第三方金额',
    `platform_status` TINYINT DEFAULT NULL COMMENT '平台状态',
    `third_party_status` TINYINT DEFAULT NULL COMMENT '第三方状态',
    `handle_status` TINYINT DEFAULT 0 COMMENT '处理状态：0=待处理，1=已处理，2=已忽略',
    `handle_remark` VARCHAR(512) DEFAULT NULL COMMENT '处理备注',
    `handle_time` DATETIME DEFAULT NULL COMMENT '处理时间',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_reconcile_id` (`reconcile_id`),
    KEY `idx_pay_no` (`pay_no`),
    KEY `idx_handle_status` (`handle_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='对账差异表';

-- 支付回调日志表
DROP TABLE IF EXISTS `pay_notify_log`;
CREATE TABLE `pay_notify_log` (
    `id` BIGINT UNSIGNED NOT NULL COMMENT '日志ID',
    `pay_no` VARCHAR(64) NOT NULL COMMENT '支付单号',
    `channel_code` VARCHAR(32) NOT NULL COMMENT '支付渠道',
    `notify_type` TINYINT NOT NULL COMMENT '通知类型：1=支付通知，2=退款通知',
    `notify_data` TEXT NOT NULL COMMENT '通知原始数据',
    `verify_result` TINYINT DEFAULT 0 COMMENT '验签结果：0=失败，1=成功',
    `handle_result` TINYINT DEFAULT 0 COMMENT '处理结果：0=失败，1=成功',
    `error_msg` VARCHAR(512) DEFAULT NULL COMMENT '错误信息',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_pay_no` (`pay_no`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='支付回调日志表';

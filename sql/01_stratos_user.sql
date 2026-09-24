-- =============================================================
-- 用户服务数据库表结构 (stratos_user)
-- =============================================================

USE `stratos_user`;

-- 用户主表
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
    `id` BIGINT UNSIGNED NOT NULL COMMENT '用户ID（雪花算法）',
    `username` VARCHAR(32) NOT NULL COMMENT '用户名',
    `nickname` VARCHAR(64) DEFAULT NULL COMMENT '昵称',
    `avatar` VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
    `gender` TINYINT DEFAULT 0 COMMENT '性别：0=未知，1=男，2=女',
    `birthday` DATE DEFAULT NULL COMMENT '生日',
    `phone_encrypted` VARCHAR(128) DEFAULT NULL COMMENT '手机号（加密）',
    `phone_hash` CHAR(64) DEFAULT NULL COMMENT '手机号哈希（用于查询）',
    `email_encrypted` VARCHAR(128) DEFAULT NULL COMMENT '邮箱（加密）',
    `email_hash` CHAR(64) DEFAULT NULL COMMENT '邮箱哈希（用于查询）',
    `status` TINYINT DEFAULT 1 COMMENT '状态：0=禁用，1=正常，2=锁定',
    `level_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '会员等级ID',
    `points` INT UNSIGNED DEFAULT 0 COMMENT '当前积分',
    `growth_value` INT UNSIGNED DEFAULT 0 COMMENT '成长值',
    `register_source` TINYINT DEFAULT 0 COMMENT '注册来源：0=Web，1=Android，2=iOS，3=小程序',
    `register_ip` VARCHAR(45) DEFAULT NULL COMMENT '注册IP',
    `last_login_time` DATETIME DEFAULT NULL COMMENT '最后登录时间',
    `last_login_ip` VARCHAR(45) DEFAULT NULL COMMENT '最后登录IP',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by` VARCHAR(64) DEFAULT NULL COMMENT '创建人',
    `update_by` VARCHAR(64) DEFAULT NULL COMMENT '更新人',
    `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0=未删除，1=已删除',
    `version` INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    UNIQUE KEY `uk_phone_hash` (`phone_hash`),
    UNIQUE KEY `uk_email_hash` (`email_hash`),
    KEY `idx_status` (`status`),
    KEY `idx_level_id` (`level_id`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户主表';

-- 用户认证表
DROP TABLE IF EXISTS `user_auth`;
CREATE TABLE `user_auth` (
    `id` BIGINT UNSIGNED NOT NULL COMMENT '认证ID',
    `user_id` BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
    `auth_type` TINYINT NOT NULL COMMENT '认证类型：1=密码，2=手机验证码，3=微信，4=支付宝',
    `identifier` VARCHAR(128) NOT NULL COMMENT '认证标识（用户名/手机号/第三方OpenID）',
    `credential` VARCHAR(255) DEFAULT NULL COMMENT '认证凭证（Bcrypt密码/Token）',
    `verified` TINYINT DEFAULT 0 COMMENT '是否验证：0=未验证，1=已验证',
    `expire_time` DATETIME DEFAULT NULL COMMENT '凭证过期时间',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `is_deleted` TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_auth_type_identifier` (`auth_type`, `identifier`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户认证表';

-- 收货地址表
DROP TABLE IF EXISTS `user_address`;
CREATE TABLE `user_address` (
    `id` BIGINT UNSIGNED NOT NULL COMMENT '地址ID',
    `user_id` BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
    `receiver_name` VARCHAR(64) NOT NULL COMMENT '收货人姓名',
    `receiver_phone` VARCHAR(20) NOT NULL COMMENT '收货人手机',
    `province` VARCHAR(32) NOT NULL COMMENT '省份',
    `city` VARCHAR(32) NOT NULL COMMENT '城市',
    `district` VARCHAR(32) NOT NULL COMMENT '区县',
    `detail_address` VARCHAR(255) NOT NULL COMMENT '详细地址',
    `postal_code` VARCHAR(10) DEFAULT NULL COMMENT '邮政编码',
    `is_default` TINYINT DEFAULT 0 COMMENT '是否默认：0=否，1=是',
    `address_type` TINYINT DEFAULT 0 COMMENT '地址类型：0=家，1=公司，2=学校',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `is_deleted` TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_is_default` (`is_default`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='收货地址表';

-- 积分账户表
DROP TABLE IF EXISTS `user_points_account`;
CREATE TABLE `user_points_account` (
    `id` BIGINT UNSIGNED NOT NULL COMMENT '账户ID',
    `user_id` BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
    `total_points` INT UNSIGNED DEFAULT 0 COMMENT '累计获得积分',
    `available_points` INT UNSIGNED DEFAULT 0 COMMENT '当前可用积分',
    `used_points` INT UNSIGNED DEFAULT 0 COMMENT '已使用积分',
    `frozen_points` INT UNSIGNED DEFAULT 0 COMMENT '冻结积分',
    `expire_points` INT UNSIGNED DEFAULT 0 COMMENT '即将过期积分',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `version` INT NOT NULL DEFAULT 0 COMMENT '乐观锁',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='积分账户表';

-- 积分流水表
DROP TABLE IF EXISTS `user_points_log`;
CREATE TABLE `user_points_log` (
    `id` BIGINT UNSIGNED NOT NULL COMMENT '流水ID',
    `user_id` BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
    `change_type` TINYINT NOT NULL COMMENT '变更类型：1=获得，2=消费，3=过期，4=退回',
    `change_points` INT NOT NULL COMMENT '变更积分（正数=增加，负数=减少）',
    `before_points` INT UNSIGNED NOT NULL COMMENT '变更前积分',
    `after_points` INT UNSIGNED NOT NULL COMMENT '变更后积分',
    `source_type` TINYINT NOT NULL COMMENT '来源类型：1=注册，2=消费，3=签到，4=活动，5=退款',
    `source_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '来源ID（订单ID/活动ID）',
    `expire_time` DATETIME DEFAULT NULL COMMENT '过期时间',
    `remark` VARCHAR(255) DEFAULT NULL COMMENT '备注',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_source_type_id` (`source_type`, `source_id`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='积分流水表';

-- 会员等级表
DROP TABLE IF EXISTS `user_level`;
CREATE TABLE `user_level` (
    `id` BIGINT UNSIGNED NOT NULL COMMENT '等级ID',
    `level_name` VARCHAR(32) NOT NULL COMMENT '等级名称',
    `level_code` VARCHAR(32) NOT NULL COMMENT '等级编码',
    `growth_value_min` INT UNSIGNED NOT NULL COMMENT '成长值下限',
    `growth_value_max` INT UNSIGNED NOT NULL COMMENT '成长值上限',
    `discount_rate` DECIMAL(5,2) DEFAULT 100.00 COMMENT '折扣率（100=无折扣，95=95折）',
    `free_shipping_threshold` DECIMAL(10,2) DEFAULT NULL COMMENT '包邮门槛',
    `privilege_desc` TEXT DEFAULT NULL COMMENT '特权描述（JSON）',
    `sort_order` INT DEFAULT 0 COMMENT '排序',
    `status` TINYINT DEFAULT 1 COMMENT '状态：0=禁用，1=启用',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `is_deleted` TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_level_code` (`level_code`),
    KEY `idx_growth_value` (`growth_value_min`, `growth_value_max`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='会员等级表';

-- 用户登录日志表
DROP TABLE IF EXISTS `user_login_log`;
CREATE TABLE `user_login_log` (
    `id` BIGINT UNSIGNED NOT NULL COMMENT '日志ID',
    `user_id` BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
    `login_type` TINYINT NOT NULL COMMENT '登录方式：1=密码，2=验证码，3=第三方',
    `login_ip` VARCHAR(45) NOT NULL COMMENT '登录IP',
    `login_location` VARCHAR(64) DEFAULT NULL COMMENT '登录地点（IP解析）',
    `user_agent` VARCHAR(512) DEFAULT NULL COMMENT '用户代理',
    `device_type` TINYINT DEFAULT 0 COMMENT '设备类型：0=未知，1=PC，2=Android，3=iOS',
    `login_status` TINYINT NOT NULL COMMENT '登录状态：1=成功，2=失败',
    `fail_reason` VARCHAR(255) DEFAULT NULL COMMENT '失败原因',
    `login_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '登录时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_login_time` (`login_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户登录日志表';

-- 用户收藏表
DROP TABLE IF EXISTS `user_favorite`;
CREATE TABLE `user_favorite` (
    `id` BIGINT UNSIGNED NOT NULL COMMENT '收藏ID',
    `user_id` BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
    `target_type` TINYINT NOT NULL COMMENT '收藏类型：1=商品，2=店铺',
    `target_id` BIGINT UNSIGNED NOT NULL COMMENT '目标ID',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_target` (`user_id`, `target_type`, `target_id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户收藏表';

-- =============================================================
-- 营销服务数据库表结构 (stratos_promotion)
-- =============================================================

USE `stratos_promotion`;

-- 秒杀活动表
DROP TABLE IF EXISTS `promotion_seckill`;
CREATE TABLE `promotion_seckill` (
    `id` BIGINT UNSIGNED NOT NULL COMMENT '秒杀活动ID',
    `activity_name` VARCHAR(128) NOT NULL COMMENT '活动名称',
    `activity_code` VARCHAR(64) NOT NULL COMMENT '活动编码',
    `spu_id` BIGINT UNSIGNED NOT NULL COMMENT 'SPU ID',
    `sku_id` BIGINT UNSIGNED NOT NULL COMMENT 'SKU ID',
    `original_price` DECIMAL(12,2) NOT NULL COMMENT '原价',
    `seckill_price` DECIMAL(12,2) NOT NULL COMMENT '秒杀价',
    `seckill_stock` INT UNSIGNED NOT NULL COMMENT '秒杀库存',
    `limit_per_user` INT UNSIGNED DEFAULT 1 COMMENT '每人限购数量',
    `start_time` DATETIME NOT NULL COMMENT '开始时间',
    `end_time` DATETIME NOT NULL COMMENT '结束时间',
    `status` TINYINT DEFAULT 0 COMMENT '状态：0=未开始，1=进行中，2=已结束，3=已下架',
    `sold_count` INT UNSIGNED DEFAULT 0 COMMENT '已售数量',
    `sort_order` INT DEFAULT 0 COMMENT '排序',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `create_by` VARCHAR(64) DEFAULT NULL,
    `update_by` VARCHAR(64) DEFAULT NULL,
    `is_deleted` TINYINT NOT NULL DEFAULT 0,
    `version` INT NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_activity_code` (`activity_code`),
    KEY `idx_sku_id` (`sku_id`),
    KEY `idx_status` (`status`),
    KEY `idx_start_time` (`start_time`),
    KEY `idx_end_time` (`end_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='秒杀活动表';

-- 秒杀记录表（用户秒杀成功记录）
DROP TABLE IF EXISTS `promotion_seckill_record`;
CREATE TABLE `promotion_seckill_record` (
    `id` BIGINT UNSIGNED NOT NULL COMMENT '记录ID',
    `seckill_id` BIGINT UNSIGNED NOT NULL COMMENT '秒杀活动ID',
    `user_id` BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
    `sku_id` BIGINT UNSIGNED NOT NULL COMMENT 'SKU ID',
    `quantity` INT UNSIGNED NOT NULL COMMENT '购买数量',
    `seckill_price` DECIMAL(12,2) NOT NULL COMMENT '秒杀价',
    `order_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '订单ID',
    `order_no` VARCHAR(64) DEFAULT NULL COMMENT '订单号',
    `status` TINYINT DEFAULT 0 COMMENT '状态：0=待支付，1=已支付，2=已取消，3=超时取消',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_seckill_id` (`seckill_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_order_no` (`order_no`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='秒杀记录表';

-- 优惠券模板表
DROP TABLE IF EXISTS `promotion_coupon`;
CREATE TABLE `promotion_coupon` (
    `id` BIGINT UNSIGNED NOT NULL COMMENT '优惠券ID',
    `coupon_name` VARCHAR(128) NOT NULL COMMENT '优惠券名称',
    `coupon_code` VARCHAR(64) NOT NULL COMMENT '优惠券编码',
    `coupon_type` TINYINT NOT NULL COMMENT '优惠券类型：1=满减券，2=折扣券，3=代金券',
    `discount_type` TINYINT NOT NULL COMMENT '优惠方式：1=固定金额，2=折扣百分比',
    `discount_value` DECIMAL(12,2) NOT NULL COMMENT '优惠值（金额/折扣）',
    `min_amount` DECIMAL(12,2) DEFAULT 0.00 COMMENT '使用门槛（满X元）',
    `max_discount` DECIMAL(12,2) DEFAULT NULL COMMENT '最高优惠金额（折扣券）',
    `publish_count` INT UNSIGNED NOT NULL COMMENT '发行数量（-1=无限）',
    `received_count` INT UNSIGNED DEFAULT 0 COMMENT '已领取数量',
    `used_count` INT UNSIGNED DEFAULT 0 COMMENT '已使用数量',
    `limit_per_user` INT UNSIGNED DEFAULT 1 COMMENT '每人限领数量',
    `receive_type` TINYINT DEFAULT 1 COMMENT '领取方式：1=主动领取，2=系统发放，3=券码兑换',
    `use_type` TINYINT DEFAULT 1 COMMENT '使用范围：1=全平台，2=指定分类，3=指定商品',
    `category_ids` VARCHAR(512) DEFAULT NULL COMMENT '指定分类ID（逗号分隔）',
    `spu_ids` VARCHAR(1024) DEFAULT NULL COMMENT '指定商品SPU ID（逗号分隔）',
    `valid_days` INT UNSIGNED DEFAULT NULL COMMENT '有效天数（领取后N天内有效）',
    `start_time` DATETIME DEFAULT NULL COMMENT '使用开始时间（固定时间段）',
    `end_time` DATETIME DEFAULT NULL COMMENT '使用结束时间',
    `status` TINYINT DEFAULT 1 COMMENT '状态：0=禁用，1=启用',
    `remark` VARCHAR(512) DEFAULT NULL COMMENT '备注',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `create_by` VARCHAR(64) DEFAULT NULL,
    `update_by` VARCHAR(64) DEFAULT NULL,
    `is_deleted` TINYINT NOT NULL DEFAULT 0,
    `version` INT NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_coupon_code` (`coupon_code`),
    KEY `idx_coupon_type` (`coupon_type`),
    KEY `idx_status` (`status`),
    KEY `idx_start_time` (`start_time`),
    KEY `idx_end_time` (`end_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='优惠券模板表';

-- 用户优惠券表
DROP TABLE IF EXISTS `user_coupon`;
CREATE TABLE `user_coupon` (
    `id` BIGINT UNSIGNED NOT NULL COMMENT '用户优惠券ID',
    `coupon_id` BIGINT UNSIGNED NOT NULL COMMENT '优惠券模板ID',
    `user_id` BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
    `coupon_code` VARCHAR(64) NOT NULL COMMENT '优惠券编码',
    `receive_type` TINYINT NOT NULL COMMENT '获得方式：1=主动领取，2=系统发放，3=券码兑换',
    `status` TINYINT DEFAULT 0 COMMENT '状态：0=未使用，1=已使用，2=已过期，3=已冻结',
    `receive_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '领取时间',
    `start_time` DATETIME NOT NULL COMMENT '有效开始时间',
    `end_time` DATETIME NOT NULL COMMENT '有效结束时间',
    `use_time` DATETIME DEFAULT NULL COMMENT '使用时间',
    `order_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '使用的订单ID',
    `order_no` VARCHAR(64) DEFAULT NULL COMMENT '使用的订单号',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_coupon_id` (`coupon_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_status` (`status`),
    KEY `idx_end_time` (`end_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户优惠券表';

-- 满减活动表
DROP TABLE IF EXISTS `promotion_full_discount`;
CREATE TABLE `promotion_full_discount` (
    `id` BIGINT UNSIGNED NOT NULL COMMENT '活动ID',
    `activity_name` VARCHAR(128) NOT NULL COMMENT '活动名称',
    `activity_code` VARCHAR(64) NOT NULL COMMENT '活动编码',
    `full_amount` DECIMAL(12,2) NOT NULL COMMENT '满足金额',
    `discount_amount` DECIMAL(12,2) NOT NULL COMMENT '减免金额',
    `use_type` TINYINT DEFAULT 1 COMMENT '使用范围：1=全平台，2=指定分类，3=指定商品',
    `category_ids` VARCHAR(512) DEFAULT NULL COMMENT '指定分类ID',
    `spu_ids` VARCHAR(1024) DEFAULT NULL COMMENT '指定商品SPU ID',
    `start_time` DATETIME NOT NULL COMMENT '开始时间',
    `end_time` DATETIME NOT NULL COMMENT '结束时间',
    `status` TINYINT DEFAULT 1 COMMENT '状态：0=禁用，1=启用',
    `sort_order` INT DEFAULT 0 COMMENT '排序',
    `used_count` INT UNSIGNED DEFAULT 0 COMMENT '使用次数',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `create_by` VARCHAR(64) DEFAULT NULL,
    `update_by` VARCHAR(64) DEFAULT NULL,
    `is_deleted` TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_activity_code` (`activity_code`),
    KEY `idx_status` (`status`),
    KEY `idx_start_time` (`start_time`),
    KEY `idx_end_time` (`end_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='满减活动表';

-- 拼团活动表
DROP TABLE IF EXISTS `promotion_group_buying`;
CREATE TABLE `promotion_group_buying` (
    `id` BIGINT UNSIGNED NOT NULL COMMENT '拼团活动ID',
    `activity_name` VARCHAR(128) NOT NULL COMMENT '活动名称',
    `activity_code` VARCHAR(64) NOT NULL COMMENT '活动编码',
    `spu_id` BIGINT UNSIGNED NOT NULL COMMENT 'SPU ID',
    `sku_id` BIGINT UNSIGNED NOT NULL COMMENT 'SKU ID',
    `original_price` DECIMAL(12,2) NOT NULL COMMENT '原价',
    `group_price` DECIMAL(12,2) NOT NULL COMMENT '拼团价',
    `require_num` INT UNSIGNED NOT NULL COMMENT '成团人数',
    `limit_hours` INT UNSIGNED DEFAULT 24 COMMENT '拼团时限（小时）',
    `limit_per_user` INT UNSIGNED DEFAULT 1 COMMENT '每人限购数量',
    `start_time` DATETIME NOT NULL COMMENT '开始时间',
    `end_time` DATETIME NOT NULL COMMENT '结束时间',
    `status` TINYINT DEFAULT 1 COMMENT '状态：0=禁用，1=启用',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `is_deleted` TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_activity_code` (`activity_code`),
    KEY `idx_sku_id` (`sku_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='拼团活动表';

-- 拼团记录表
DROP TABLE IF EXISTS `promotion_group_record`;
CREATE TABLE `promotion_group_record` (
    `id` BIGINT UNSIGNED NOT NULL COMMENT '拼团记录ID',
    `group_buying_id` BIGINT UNSIGNED NOT NULL COMMENT '拼团活动ID',
    `group_no` VARCHAR(64) NOT NULL COMMENT '团号',
    `leader_user_id` BIGINT UNSIGNED NOT NULL COMMENT '团长用户ID',
    `require_num` INT UNSIGNED NOT NULL COMMENT '成团人数',
    `current_num` INT UNSIGNED DEFAULT 1 COMMENT '当前人数',
    `status` TINYINT DEFAULT 0 COMMENT '状态：0=拼团中，1=拼团成功，2=拼团失败',
    `expire_time` DATETIME NOT NULL COMMENT '过期时间',
    `success_time` DATETIME DEFAULT NULL COMMENT '成团时间',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_group_no` (`group_no`),
    KEY `idx_group_buying_id` (`group_buying_id`),
    KEY `idx_leader_user_id` (`leader_user_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='拼团记录表';

-- 拼团成员表
DROP TABLE IF EXISTS `promotion_group_member`;
CREATE TABLE `promotion_group_member` (
    `id` BIGINT UNSIGNED NOT NULL COMMENT '成员ID',
    `group_record_id` BIGINT UNSIGNED NOT NULL COMMENT '拼团记录ID',
    `group_no` VARCHAR(64) NOT NULL COMMENT '团号',
    `user_id` BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
    `order_id` BIGINT UNSIGNED NOT NULL COMMENT '订单ID',
    `order_no` VARCHAR(64) NOT NULL COMMENT '订单号',
    `is_leader` TINYINT DEFAULT 0 COMMENT '是否团长：0=否，1=是',
    `join_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '参团时间',
    PRIMARY KEY (`id`),
    KEY `idx_group_record_id` (`group_record_id`),
    KEY `idx_group_no` (`group_no`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='拼团成员表';

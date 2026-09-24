-- =============================================================
-- 订单服务数据库表结构 (stratos_order_0 / stratos_order_1)
-- 说明：订单表按 user_id % 2 分库，每个库内再按 user_id % 8 分表
-- =============================================================

-- 以下DDL在 stratos_order_0 和 stratos_order_1 两个库中都需要执行

USE `stratos_order_0`;

-- 订单主表（分表：order_info_0 ~ order_info_7）
DROP TABLE IF EXISTS `order_info_0`;
CREATE TABLE `order_info_0` (
    `id` BIGINT UNSIGNED NOT NULL COMMENT '订单ID',
    `order_no` VARCHAR(64) NOT NULL COMMENT '订单号（唯一）',
    `user_id` BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '订单状态：0=待支付，10=已支付，20=待发货，30=已发货，40=已收货，50=已完成，-10=已取消，-20=退款中，-30=已退款',
    `total_amount` DECIMAL(12,2) NOT NULL COMMENT '订单总金额',
    `pay_amount` DECIMAL(12,2) NOT NULL COMMENT '实付金额',
    `freight_amount` DECIMAL(12,2) DEFAULT 0.00 COMMENT '运费',
    `discount_amount` DECIMAL(12,2) DEFAULT 0.00 COMMENT '优惠金额',
    `coupon_amount` DECIMAL(12,2) DEFAULT 0.00 COMMENT '优惠券金额',
    `points_amount` DECIMAL(12,2) DEFAULT 0.00 COMMENT '积分抵扣金额',
    `use_points` INT UNSIGNED DEFAULT 0 COMMENT '使用积分',
    `gain_points` INT UNSIGNED DEFAULT 0 COMMENT '获得积分',
    `coupon_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '使用的优惠券ID',
    `payment_type` TINYINT DEFAULT NULL COMMENT '支付方式：1=支付宝，2=微信，3=银联，4=余额',
    `payment_time` DATETIME DEFAULT NULL COMMENT '支付时间',
    `delivery_type` TINYINT DEFAULT 1 COMMENT '配送方式：1=快递，2=自提',
    `receiver_name` VARCHAR(64) NOT NULL COMMENT '收货人姓名',
    `receiver_phone` VARCHAR(20) NOT NULL COMMENT '收货人手机',
    `receiver_province` VARCHAR(32) NOT NULL COMMENT '省份',
    `receiver_city` VARCHAR(32) NOT NULL COMMENT '城市',
    `receiver_district` VARCHAR(32) NOT NULL COMMENT '区县',
    `receiver_detail_address` VARCHAR(255) NOT NULL COMMENT '详细地址',
    `receiver_postal_code` VARCHAR(10) DEFAULT NULL COMMENT '邮编',
    `auto_confirm_day` INT DEFAULT 7 COMMENT '自动确认天数',
    `buyer_remark` VARCHAR(512) DEFAULT NULL COMMENT '买家备注',
    `seller_remark` VARCHAR(512) DEFAULT NULL COMMENT '卖家备注',
    `confirm_time` DATETIME DEFAULT NULL COMMENT '确认收货时间',
    `close_time` DATETIME DEFAULT NULL COMMENT '订单关闭时间',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `is_deleted` TINYINT NOT NULL DEFAULT 0,
    `version` INT NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_no` (`order_no`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_status` (`status`),
    KEY `idx_create_time` (`create_time`),
    KEY `idx_payment_time` (`payment_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='订单主表_0';

-- 创建其余7张分表（order_info_1 ~ order_info_7）
DROP TABLE IF EXISTS `order_info_1`;
CREATE TABLE `order_info_1` LIKE `order_info_0`;
ALTER TABLE `order_info_1` COMMENT='订单主表_1';

DROP TABLE IF EXISTS `order_info_2`;
CREATE TABLE `order_info_2` LIKE `order_info_0`;
ALTER TABLE `order_info_2` COMMENT='订单主表_2';

DROP TABLE IF EXISTS `order_info_3`;
CREATE TABLE `order_info_3` LIKE `order_info_0`;
ALTER TABLE `order_info_3` COMMENT='订单主表_3';

DROP TABLE IF EXISTS `order_info_4`;
CREATE TABLE `order_info_4` LIKE `order_info_0`;
ALTER TABLE `order_info_4` COMMENT='订单主表_4';

DROP TABLE IF EXISTS `order_info_5`;
CREATE TABLE `order_info_5` LIKE `order_info_0`;
ALTER TABLE `order_info_5` COMMENT='订单主表_5';

DROP TABLE IF EXISTS `order_info_6`;
CREATE TABLE `order_info_6` LIKE `order_info_0`;
ALTER TABLE `order_info_6` COMMENT='订单主表_6';

DROP TABLE IF EXISTS `order_info_7`;
CREATE TABLE `order_info_7` LIKE `order_info_0`;
ALTER TABLE `order_info_7` COMMENT='订单主表_7';

-- 订单明细表（分表：order_item_0 ~ order_item_7）
DROP TABLE IF EXISTS `order_item_0`;
CREATE TABLE `order_item_0` (
    `id` BIGINT UNSIGNED NOT NULL COMMENT '明细ID',
    `order_id` BIGINT UNSIGNED NOT NULL COMMENT '订单ID',
    `order_no` VARCHAR(64) NOT NULL COMMENT '订单号',
    `user_id` BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
    `spu_id` BIGINT UNSIGNED NOT NULL COMMENT 'SPU ID',
    `sku_id` BIGINT UNSIGNED NOT NULL COMMENT 'SKU ID',
    `sku_name` VARCHAR(255) NOT NULL COMMENT 'SKU名称',
    `sku_image` VARCHAR(512) DEFAULT NULL COMMENT 'SKU图片',
    `sku_code` VARCHAR(64) NOT NULL COMMENT 'SKU编码',
    `spec_desc` VARCHAR(255) DEFAULT NULL COMMENT '规格描述',
    `price` DECIMAL(12,2) NOT NULL COMMENT '单价',
    `quantity` INT UNSIGNED NOT NULL COMMENT '数量',
    `total_amount` DECIMAL(12,2) NOT NULL COMMENT '小计金额',
    `discount_amount` DECIMAL(12,2) DEFAULT 0.00 COMMENT '优惠金额',
    `real_amount` DECIMAL(12,2) NOT NULL COMMENT '实付金额',
    `promotion_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '促销活动ID',
    `promotion_type` TINYINT DEFAULT NULL COMMENT '促销类型：1=秒杀，2=满减，3=拼团',
    `refund_status` TINYINT DEFAULT 0 COMMENT '退款状态：0=无退款，1=退款中，2=已退款',
    `refund_amount` DECIMAL(12,2) DEFAULT 0.00 COMMENT '退款金额',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `is_deleted` TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `idx_order_id` (`order_id`),
    KEY `idx_order_no` (`order_no`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_sku_id` (`sku_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='订单明细表_0';

-- 创建其余7张分表
DROP TABLE IF EXISTS `order_item_1`;
CREATE TABLE `order_item_1` LIKE `order_item_0`;
ALTER TABLE `order_item_1` COMMENT='订单明细表_1';

DROP TABLE IF EXISTS `order_item_2`;
CREATE TABLE `order_item_2` LIKE `order_item_0`;
ALTER TABLE `order_item_2` COMMENT='订单明细表_2';

DROP TABLE IF EXISTS `order_item_3`;
CREATE TABLE `order_item_3` LIKE `order_item_0`;
ALTER TABLE `order_item_3` COMMENT='订单明细表_3';

DROP TABLE IF EXISTS `order_item_4`;
CREATE TABLE `order_item_4` LIKE `order_item_0`;
ALTER TABLE `order_item_4` COMMENT='订单明细表_4';

DROP TABLE IF EXISTS `order_item_5`;
CREATE TABLE `order_item_5` LIKE `order_item_0`;
ALTER TABLE `order_item_5` COMMENT='订单明细表_5';

DROP TABLE IF EXISTS `order_item_6`;
CREATE TABLE `order_item_6` LIKE `order_item_0`;
ALTER TABLE `order_item_6` COMMENT='订单明细表_6';

DROP TABLE IF EXISTS `order_item_7`;
CREATE TABLE `order_item_7` LIKE `order_item_0`;
ALTER TABLE `order_item_7` COMMENT='订单明细表_7';

-- 订单状态变更日志表（不分表，按订单号查询）
DROP TABLE IF EXISTS `order_status_log`;
CREATE TABLE `order_status_log` (
    `id` BIGINT UNSIGNED NOT NULL COMMENT '日志ID',
    `order_id` BIGINT UNSIGNED NOT NULL COMMENT '订单ID',
    `order_no` VARCHAR(64) NOT NULL COMMENT '订单号',
    `user_id` BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
    `before_status` TINYINT NOT NULL COMMENT '变更前状态',
    `after_status` TINYINT NOT NULL COMMENT '变更后状态',
    `operator_type` TINYINT NOT NULL COMMENT '操作类型：1=用户操作，2=系统操作，3=管理员操作',
    `operator_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '操作人ID',
    `remark` VARCHAR(512) DEFAULT NULL COMMENT '备注',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_order_id` (`order_id`),
    KEY `idx_order_no` (`order_no`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='订单状态变更日志表';

-- 退款单表（不分表）
DROP TABLE IF EXISTS `order_refund`;
CREATE TABLE `order_refund` (
    `id` BIGINT UNSIGNED NOT NULL COMMENT '退款单ID',
    `refund_no` VARCHAR(64) NOT NULL COMMENT '退款单号',
    `order_id` BIGINT UNSIGNED NOT NULL COMMENT '订单ID',
    `order_no` VARCHAR(64) NOT NULL COMMENT '订单号',
    `user_id` BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
    `refund_type` TINYINT NOT NULL COMMENT '退款类型：1=仅退款，2=退货退款',
    `refund_reason` TINYINT NOT NULL COMMENT '退款原因：1=不想要了，2=质量问题，3=商品描述不符，4=其他',
    `refund_desc` VARCHAR(512) DEFAULT NULL COMMENT '退款说明',
    `refund_amount` DECIMAL(12,2) NOT NULL COMMENT '退款金额',
    `refund_images` TEXT DEFAULT NULL COMMENT '退款凭证图片（JSON）',
    `status` TINYINT DEFAULT 0 COMMENT '状态：0=待审核，1=审核通过，2=审核拒绝，10=退款中，20=退款成功，-10=已取消',
    `audit_time` DATETIME DEFAULT NULL COMMENT '审核时间',
    `audit_remark` VARCHAR(512) DEFAULT NULL COMMENT '审核备注',
    `refund_time` DATETIME DEFAULT NULL COMMENT '退款完成时间',
    `express_company` VARCHAR(64) DEFAULT NULL COMMENT '快递公司',
    `express_no` VARCHAR(64) DEFAULT NULL COMMENT '快递单号',
    `receive_time` DATETIME DEFAULT NULL COMMENT '收货时间（退货退款）',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `is_deleted` TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_refund_no` (`refund_no`),
    KEY `idx_order_id` (`order_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_status` (`status`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='退款单表';

-- 物流信息表（不分表）
DROP TABLE IF EXISTS `order_logistics`;
CREATE TABLE `order_logistics` (
    `id` BIGINT UNSIGNED NOT NULL COMMENT '物流ID',
    `order_id` BIGINT UNSIGNED NOT NULL COMMENT '订单ID',
    `order_no` VARCHAR(64) NOT NULL COMMENT '订单号',
    `logistics_company` VARCHAR(64) NOT NULL COMMENT '物流公司',
    `logistics_company_code` VARCHAR(32) NOT NULL COMMENT '物流公司编码',
    `logistics_no` VARCHAR(64) NOT NULL COMMENT '物流单号',
    `sender_name` VARCHAR(64) DEFAULT NULL COMMENT '发件人姓名',
    `sender_phone` VARCHAR(20) DEFAULT NULL COMMENT '发件人手机',
    `receiver_name` VARCHAR(64) NOT NULL COMMENT '收件人姓名',
    `receiver_phone` VARCHAR(20) NOT NULL COMMENT '收件人手机',
    `receiver_address` VARCHAR(512) NOT NULL COMMENT '收件地址',
    `last_trace` VARCHAR(512) DEFAULT NULL COMMENT '最新物流轨迹',
    `last_trace_time` DATETIME DEFAULT NULL COMMENT '最新轨迹时间',
    `status` TINYINT DEFAULT 0 COMMENT '状态：0=待揽件，1=运输中，2=派送中，3=已签收，-1=异常',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_id` (`order_id`),
    KEY `idx_order_no` (`order_no`),
    KEY `idx_logistics_no` (`logistics_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='物流信息表';

-- 购物车表（不分表，可放Redis，此处作为持久化备份）
DROP TABLE IF EXISTS `shopping_cart`;
CREATE TABLE `shopping_cart` (
    `id` BIGINT UNSIGNED NOT NULL COMMENT '购物车ID',
    `user_id` BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
    `sku_id` BIGINT UNSIGNED NOT NULL COMMENT 'SKU ID',
    `spu_id` BIGINT UNSIGNED NOT NULL COMMENT 'SPU ID',
    `quantity` INT UNSIGNED NOT NULL DEFAULT 1 COMMENT '数量',
    `selected` TINYINT DEFAULT 1 COMMENT '是否选中：0=否，1=是',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_sku` (`user_id`, `sku_id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='购物车表';

-- =============================================================
-- 在 stratos_order_1 库中执行相同的DDL
-- =============================================================
USE `stratos_order_1`;

-- 订单主表（分表：order_info_0 ~ order_info_7）
DROP TABLE IF EXISTS `order_info_0`;
CREATE TABLE `order_info_0` (
    `id` BIGINT UNSIGNED NOT NULL COMMENT '订单ID',
    `order_no` VARCHAR(64) NOT NULL COMMENT '订单号（唯一）',
    `user_id` BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '订单状态：0=待支付，10=已支付，20=待发货，30=已发货，40=已收货，50=已完成，-10=已取消，-20=退款中，-30=已退款',
    `total_amount` DECIMAL(12,2) NOT NULL COMMENT '订单总金额',
    `pay_amount` DECIMAL(12,2) NOT NULL COMMENT '实付金额',
    `freight_amount` DECIMAL(12,2) DEFAULT 0.00 COMMENT '运费',
    `discount_amount` DECIMAL(12,2) DEFAULT 0.00 COMMENT '优惠金额',
    `coupon_amount` DECIMAL(12,2) DEFAULT 0.00 COMMENT '优惠券金额',
    `points_amount` DECIMAL(12,2) DEFAULT 0.00 COMMENT '积分抵扣金额',
    `use_points` INT UNSIGNED DEFAULT 0 COMMENT '使用积分',
    `gain_points` INT UNSIGNED DEFAULT 0 COMMENT '获得积分',
    `coupon_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '使用的优惠券ID',
    `payment_type` TINYINT DEFAULT NULL COMMENT '支付方式：1=支付宝，2=微信，3=银联，4=余额',
    `payment_time` DATETIME DEFAULT NULL COMMENT '支付时间',
    `delivery_type` TINYINT DEFAULT 1 COMMENT '配送方式：1=快递，2=自提',
    `receiver_name` VARCHAR(64) NOT NULL COMMENT '收货人姓名',
    `receiver_phone` VARCHAR(20) NOT NULL COMMENT '收货人手机',
    `receiver_province` VARCHAR(32) NOT NULL COMMENT '省份',
    `receiver_city` VARCHAR(32) NOT NULL COMMENT '城市',
    `receiver_district` VARCHAR(32) NOT NULL COMMENT '区县',
    `receiver_detail_address` VARCHAR(255) NOT NULL COMMENT '详细地址',
    `receiver_postal_code` VARCHAR(10) DEFAULT NULL COMMENT '邮编',
    `auto_confirm_day` INT DEFAULT 7 COMMENT '自动确认天数',
    `buyer_remark` VARCHAR(512) DEFAULT NULL COMMENT '买家备注',
    `seller_remark` VARCHAR(512) DEFAULT NULL COMMENT '卖家备注',
    `confirm_time` DATETIME DEFAULT NULL COMMENT '确认收货时间',
    `close_time` DATETIME DEFAULT NULL COMMENT '订单关闭时间',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `is_deleted` TINYINT NOT NULL DEFAULT 0,
    `version` INT NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_no` (`order_no`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_status` (`status`),
    KEY `idx_create_time` (`create_time`),
    KEY `idx_payment_time` (`payment_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='订单主表_0';

DROP TABLE IF EXISTS `order_info_1`;
CREATE TABLE `order_info_1` LIKE `order_info_0`;
ALTER TABLE `order_info_1` COMMENT='订单主表_1';

DROP TABLE IF EXISTS `order_info_2`;
CREATE TABLE `order_info_2` LIKE `order_info_0`;
ALTER TABLE `order_info_2` COMMENT='订单主表_2';

DROP TABLE IF EXISTS `order_info_3`;
CREATE TABLE `order_info_3` LIKE `order_info_0`;
ALTER TABLE `order_info_3` COMMENT='订单主表_3';

DROP TABLE IF EXISTS `order_info_4`;
CREATE TABLE `order_info_4` LIKE `order_info_0`;
ALTER TABLE `order_info_4` COMMENT='订单主表_4';

DROP TABLE IF EXISTS `order_info_5`;
CREATE TABLE `order_info_5` LIKE `order_info_0`;
ALTER TABLE `order_info_5` COMMENT='订单主表_5';

DROP TABLE IF EXISTS `order_info_6`;
CREATE TABLE `order_info_6` LIKE `order_info_0`;
ALTER TABLE `order_info_6` COMMENT='订单主表_6';

DROP TABLE IF EXISTS `order_info_7`;
CREATE TABLE `order_info_7` LIKE `order_info_0`;
ALTER TABLE `order_info_7` COMMENT='订单主表_7';

-- 订单明细表（分表：order_item_0 ~ order_item_7）
DROP TABLE IF EXISTS `order_item_0`;
CREATE TABLE `order_item_0` (
    `id` BIGINT UNSIGNED NOT NULL COMMENT '明细ID',
    `order_id` BIGINT UNSIGNED NOT NULL COMMENT '订单ID',
    `order_no` VARCHAR(64) NOT NULL COMMENT '订单号',
    `user_id` BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
    `spu_id` BIGINT UNSIGNED NOT NULL COMMENT 'SPU ID',
    `sku_id` BIGINT UNSIGNED NOT NULL COMMENT 'SKU ID',
    `sku_name` VARCHAR(255) NOT NULL COMMENT 'SKU名称',
    `sku_image` VARCHAR(512) DEFAULT NULL COMMENT 'SKU图片',
    `sku_code` VARCHAR(64) NOT NULL COMMENT 'SKU编码',
    `spec_desc` VARCHAR(255) DEFAULT NULL COMMENT '规格描述',
    `price` DECIMAL(12,2) NOT NULL COMMENT '单价',
    `quantity` INT UNSIGNED NOT NULL COMMENT '数量',
    `total_amount` DECIMAL(12,2) NOT NULL COMMENT '小计金额',
    `discount_amount` DECIMAL(12,2) DEFAULT 0.00 COMMENT '优惠金额',
    `real_amount` DECIMAL(12,2) NOT NULL COMMENT '实付金额',
    `promotion_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '促销活动ID',
    `promotion_type` TINYINT DEFAULT NULL COMMENT '促销类型：1=秒杀，2=满减，3=拼团',
    `refund_status` TINYINT DEFAULT 0 COMMENT '退款状态：0=无退款，1=退款中，2=已退款',
    `refund_amount` DECIMAL(12,2) DEFAULT 0.00 COMMENT '退款金额',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `is_deleted` TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `idx_order_id` (`order_id`),
    KEY `idx_order_no` (`order_no`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_sku_id` (`sku_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='订单明细表_0';

DROP TABLE IF EXISTS `order_item_1`;
CREATE TABLE `order_item_1` LIKE `order_item_0`;
ALTER TABLE `order_item_1` COMMENT='订单明细表_1';

DROP TABLE IF EXISTS `order_item_2`;
CREATE TABLE `order_item_2` LIKE `order_item_0`;
ALTER TABLE `order_item_2` COMMENT='订单明细表_2';

DROP TABLE IF EXISTS `order_item_3`;
CREATE TABLE `order_item_3` LIKE `order_item_0`;
ALTER TABLE `order_item_3` COMMENT='订单明细表_3';

DROP TABLE IF EXISTS `order_item_4`;
CREATE TABLE `order_item_4` LIKE `order_item_0`;
ALTER TABLE `order_item_4` COMMENT='订单明细表_4';

DROP TABLE IF EXISTS `order_item_5`;
CREATE TABLE `order_item_5` LIKE `order_item_0`;
ALTER TABLE `order_item_5` COMMENT='订单明细表_5';

DROP TABLE IF EXISTS `order_item_6`;
CREATE TABLE `order_item_6` LIKE `order_item_0`;
ALTER TABLE `order_item_6` COMMENT='订单明细表_6';

DROP TABLE IF EXISTS `order_item_7`;
CREATE TABLE `order_item_7` LIKE `order_item_0`;
ALTER TABLE `order_item_7` COMMENT='订单明细表_7';

-- 订单状态变更日志表（不分表）
DROP TABLE IF EXISTS `order_status_log`;
CREATE TABLE `order_status_log` (
    `id` BIGINT UNSIGNED NOT NULL COMMENT '日志ID',
    `order_id` BIGINT UNSIGNED NOT NULL COMMENT '订单ID',
    `order_no` VARCHAR(64) NOT NULL COMMENT '订单号',
    `user_id` BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
    `before_status` TINYINT NOT NULL COMMENT '变更前状态',
    `after_status` TINYINT NOT NULL COMMENT '变更后状态',
    `operator_type` TINYINT NOT NULL COMMENT '操作类型：1=用户操作，2=系统操作，3=管理员操作',
    `operator_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '操作人ID',
    `remark` VARCHAR(512) DEFAULT NULL COMMENT '备注',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_order_id` (`order_id`),
    KEY `idx_order_no` (`order_no`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='订单状态变更日志表';

-- 退款单表（不分表）
DROP TABLE IF EXISTS `order_refund`;
CREATE TABLE `order_refund` (
    `id` BIGINT UNSIGNED NOT NULL COMMENT '退款单ID',
    `refund_no` VARCHAR(64) NOT NULL COMMENT '退款单号',
    `order_id` BIGINT UNSIGNED NOT NULL COMMENT '订单ID',
    `order_no` VARCHAR(64) NOT NULL COMMENT '订单号',
    `user_id` BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
    `refund_type` TINYINT NOT NULL COMMENT '退款类型：1=仅退款，2=退货退款',
    `refund_reason` TINYINT NOT NULL COMMENT '退款原因：1=不想要了，2=质量问题，3=商品描述不符，4=其他',
    `refund_desc` VARCHAR(512) DEFAULT NULL COMMENT '退款说明',
    `refund_amount` DECIMAL(12,2) NOT NULL COMMENT '退款金额',
    `refund_images` TEXT DEFAULT NULL COMMENT '退款凭证图片（JSON）',
    `status` TINYINT DEFAULT 0 COMMENT '状态：0=待审核，1=审核通过，2=审核拒绝，10=退款中，20=退款成功，-10=已取消',
    `audit_time` DATETIME DEFAULT NULL COMMENT '审核时间',
    `audit_remark` VARCHAR(512) DEFAULT NULL COMMENT '审核备注',
    `refund_time` DATETIME DEFAULT NULL COMMENT '退款完成时间',
    `express_company` VARCHAR(64) DEFAULT NULL COMMENT '快递公司',
    `express_no` VARCHAR(64) DEFAULT NULL COMMENT '快递单号',
    `receive_time` DATETIME DEFAULT NULL COMMENT '收货时间（退货退款）',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `is_deleted` TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_refund_no` (`refund_no`),
    KEY `idx_order_id` (`order_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_status` (`status`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='退款单表';

-- 物流信息表（不分表）
DROP TABLE IF EXISTS `order_logistics`;
CREATE TABLE `order_logistics` (
    `id` BIGINT UNSIGNED NOT NULL COMMENT '物流ID',
    `order_id` BIGINT UNSIGNED NOT NULL COMMENT '订单ID',
    `order_no` VARCHAR(64) NOT NULL COMMENT '订单号',
    `logistics_company` VARCHAR(64) NOT NULL COMMENT '物流公司',
    `logistics_company_code` VARCHAR(32) NOT NULL COMMENT '物流公司编码',
    `logistics_no` VARCHAR(64) NOT NULL COMMENT '物流单号',
    `sender_name` VARCHAR(64) DEFAULT NULL COMMENT '发件人姓名',
    `sender_phone` VARCHAR(20) DEFAULT NULL COMMENT '发件人手机',
    `receiver_name` VARCHAR(64) NOT NULL COMMENT '收件人姓名',
    `receiver_phone` VARCHAR(20) NOT NULL COMMENT '收件人手机',
    `receiver_address` VARCHAR(512) NOT NULL COMMENT '收件地址',
    `last_trace` VARCHAR(512) DEFAULT NULL COMMENT '最新物流轨迹',
    `last_trace_time` DATETIME DEFAULT NULL COMMENT '最新轨迹时间',
    `status` TINYINT DEFAULT 0 COMMENT '状态：0=待揽件，1=运输中，2=派送中，3=已签收，-1=异常',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_id` (`order_id`),
    KEY `idx_order_no` (`order_no`),
    KEY `idx_logistics_no` (`logistics_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='物流信息表';

-- 购物车表（不分表）
DROP TABLE IF EXISTS `shopping_cart`;
CREATE TABLE `shopping_cart` (
    `id` BIGINT UNSIGNED NOT NULL COMMENT '购物车ID',
    `user_id` BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
    `sku_id` BIGINT UNSIGNED NOT NULL COMMENT 'SKU ID',
    `spu_id` BIGINT UNSIGNED NOT NULL COMMENT 'SPU ID',
    `quantity` INT UNSIGNED NOT NULL DEFAULT 1 COMMENT '数量',
    `selected` TINYINT DEFAULT 1 COMMENT '是否选中：0=否，1=是',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_sku` (`user_id`, `sku_id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='购物车表';

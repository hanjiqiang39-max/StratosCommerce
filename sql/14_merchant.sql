-- =============================================================
-- 商家端：商家账号、店铺，以及商品/订单按店铺隔离
-- 已有库增量执行，不要重跑 00～12
-- =============================================================

USE `stratos_system`;

CREATE TABLE IF NOT EXISTS `merchant` (
    `id` BIGINT UNSIGNED NOT NULL COMMENT '商家ID',
    `username` VARCHAR(32) NOT NULL COMMENT '登录名',
    `password` VARCHAR(128) NOT NULL COMMENT '密码（Bcrypt）',
    `real_name` VARCHAR(64) DEFAULT NULL COMMENT '联系人姓名',
    `nickname` VARCHAR(64) DEFAULT NULL COMMENT '昵称',
    `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    `email` VARCHAR(128) DEFAULT NULL COMMENT '邮箱',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0=待审核，1=正常，2=禁用',
    `audit_status` TINYINT NOT NULL DEFAULT 0 COMMENT '入驻审核：0=待审核，1=通过，2=拒绝',
    `audit_remark` VARCHAR(512) DEFAULT NULL COMMENT '审核备注',
    `last_login_time` DATETIME DEFAULT NULL COMMENT '最后登录时间',
    `last_login_ip` VARCHAR(45) DEFAULT NULL COMMENT '最后登录IP',
    `remark` VARCHAR(255) DEFAULT NULL COMMENT '备注',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `create_by` VARCHAR(64) DEFAULT NULL,
    `update_by` VARCHAR(64) DEFAULT NULL,
    `is_deleted` TINYINT NOT NULL DEFAULT 0,
    `version` INT NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    KEY `idx_status` (`status`),
    KEY `idx_audit_status` (`audit_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='商家账号';

CREATE TABLE IF NOT EXISTS `merchant_shop` (
    `id` BIGINT UNSIGNED NOT NULL COMMENT '店铺ID',
    `merchant_id` BIGINT UNSIGNED NOT NULL COMMENT '商家ID',
    `shop_name` VARCHAR(64) NOT NULL COMMENT '店铺名称',
    `shop_logo` VARCHAR(512) DEFAULT NULL COMMENT '店铺Logo',
    `shop_desc` VARCHAR(512) DEFAULT NULL COMMENT '店铺简介',
    `contact_name` VARCHAR(64) DEFAULT NULL COMMENT '联系人',
    `contact_phone` VARCHAR(20) DEFAULT NULL COMMENT '联系电话',
    `province` VARCHAR(32) DEFAULT NULL,
    `city` VARCHAR(32) DEFAULT NULL,
    `district` VARCHAR(32) DEFAULT NULL,
    `detail_address` VARCHAR(255) DEFAULT NULL,
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '店铺状态：0=打烊，1=营业',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `create_by` VARCHAR(64) DEFAULT NULL,
    `update_by` VARCHAR(64) DEFAULT NULL,
    `is_deleted` TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_merchant_id` (`merchant_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='商家店铺';

INSERT INTO `merchant` (`id`, `username`, `password`, `real_name`, `nickname`, `phone`, `status`, `audit_status`, `remark`)
SELECT 1, 'merchant', '{noop-pending}', '演示商家', '官方旗舰店店主', '13800000001', 1, 1, '本地演示账号'
WHERE NOT EXISTS (SELECT 1 FROM `merchant` WHERE `id` = 1 OR `username` = 'merchant');

INSERT INTO `merchant_shop` (`id`, `merchant_id`, `shop_name`, `shop_logo`, `shop_desc`, `contact_name`, `contact_phone`, `province`, `city`, `district`, `detail_address`, `status`)
SELECT 1, 1, 'Stratos 官方旗舰店', '/placeholder.svg', '平台官方演示店铺，承接历史商品与订单', '演示商家', '13800000001', '浙江', '杭州', '西湖区', '平流层路 1 号', 1
WHERE NOT EXISTS (SELECT 1 FROM `merchant_shop` WHERE `id` = 1);

USE `stratos_product`;

-- 已有列时忽略错误，重复执行即可
ALTER TABLE `product_spu`
    ADD COLUMN `shop_id` BIGINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '所属店铺ID' AFTER `id`,
    ADD KEY `idx_shop_id` (`shop_id`);

UPDATE `product_spu` SET `shop_id` = 1 WHERE `shop_id` IS NULL OR `shop_id` = 0;

USE `stratos_order_0`;
ALTER TABLE `order_info_0` ADD COLUMN `shop_id` BIGINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '所属店铺ID' AFTER `user_id`, ADD KEY `idx_shop_id` (`shop_id`);
ALTER TABLE `order_info_1` ADD COLUMN `shop_id` BIGINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '所属店铺ID' AFTER `user_id`, ADD KEY `idx_shop_id` (`shop_id`);
ALTER TABLE `order_info_2` ADD COLUMN `shop_id` BIGINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '所属店铺ID' AFTER `user_id`, ADD KEY `idx_shop_id` (`shop_id`);
ALTER TABLE `order_info_3` ADD COLUMN `shop_id` BIGINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '所属店铺ID' AFTER `user_id`, ADD KEY `idx_shop_id` (`shop_id`);
ALTER TABLE `order_info_4` ADD COLUMN `shop_id` BIGINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '所属店铺ID' AFTER `user_id`, ADD KEY `idx_shop_id` (`shop_id`);
ALTER TABLE `order_info_5` ADD COLUMN `shop_id` BIGINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '所属店铺ID' AFTER `user_id`, ADD KEY `idx_shop_id` (`shop_id`);
ALTER TABLE `order_info_6` ADD COLUMN `shop_id` BIGINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '所属店铺ID' AFTER `user_id`, ADD KEY `idx_shop_id` (`shop_id`);
ALTER TABLE `order_info_7` ADD COLUMN `shop_id` BIGINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '所属店铺ID' AFTER `user_id`, ADD KEY `idx_shop_id` (`shop_id`);

USE `stratos_order_1`;
ALTER TABLE `order_info_0` ADD COLUMN `shop_id` BIGINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '所属店铺ID' AFTER `user_id`, ADD KEY `idx_shop_id` (`shop_id`);
ALTER TABLE `order_info_1` ADD COLUMN `shop_id` BIGINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '所属店铺ID' AFTER `user_id`, ADD KEY `idx_shop_id` (`shop_id`);
ALTER TABLE `order_info_2` ADD COLUMN `shop_id` BIGINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '所属店铺ID' AFTER `user_id`, ADD KEY `idx_shop_id` (`shop_id`);
ALTER TABLE `order_info_3` ADD COLUMN `shop_id` BIGINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '所属店铺ID' AFTER `user_id`, ADD KEY `idx_shop_id` (`shop_id`);
ALTER TABLE `order_info_4` ADD COLUMN `shop_id` BIGINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '所属店铺ID' AFTER `user_id`, ADD KEY `idx_shop_id` (`shop_id`);
ALTER TABLE `order_info_5` ADD COLUMN `shop_id` BIGINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '所属店铺ID' AFTER `user_id`, ADD KEY `idx_shop_id` (`shop_id`);
ALTER TABLE `order_info_6` ADD COLUMN `shop_id` BIGINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '所属店铺ID' AFTER `user_id`, ADD KEY `idx_shop_id` (`shop_id`);
ALTER TABLE `order_info_7` ADD COLUMN `shop_id` BIGINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '所属店铺ID' AFTER `user_id`, ADD KEY `idx_shop_id` (`shop_id`);

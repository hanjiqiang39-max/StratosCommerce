-- =============================================================
-- 库存服务数据库表结构 (stratos_inventory)
-- =============================================================

USE `stratos_inventory`;

-- 库存主表
DROP TABLE IF EXISTS `inventory_stock`;
CREATE TABLE `inventory_stock` (
    `id` BIGINT UNSIGNED NOT NULL COMMENT '库存ID',
    `sku_id` BIGINT UNSIGNED NOT NULL COMMENT 'SKU ID',
    `warehouse_id` BIGINT UNSIGNED DEFAULT 1 COMMENT '仓库ID',
    `total_stock` INT UNSIGNED DEFAULT 0 COMMENT '总库存',
    `available_stock` INT UNSIGNED DEFAULT 0 COMMENT '可用库存',
    `lock_stock` INT UNSIGNED DEFAULT 0 COMMENT '锁定库存',
    `sold_stock` INT UNSIGNED DEFAULT 0 COMMENT '已售库存',
    `in_transit_stock` INT UNSIGNED DEFAULT 0 COMMENT '在途库存',
    `low_stock_threshold` INT UNSIGNED DEFAULT 10 COMMENT '低库存预警阈值',
    `status` TINYINT DEFAULT 1 COMMENT '状态：0=禁用，1=启用',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `version` INT NOT NULL DEFAULT 0 COMMENT '乐观锁（库存操作必用）',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_sku_warehouse` (`sku_id`, `warehouse_id`),
    KEY `idx_available_stock` (`available_stock`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='库存主表';

-- 库存变更日志表
DROP TABLE IF EXISTS `inventory_log`;
CREATE TABLE `inventory_log` (
    `id` BIGINT UNSIGNED NOT NULL COMMENT '日志ID',
    `sku_id` BIGINT UNSIGNED NOT NULL COMMENT 'SKU ID',
    `warehouse_id` BIGINT UNSIGNED NOT NULL COMMENT '仓库ID',
    `change_type` TINYINT NOT NULL COMMENT '变更类型：1=入库，2=出库，3=锁定，4=解锁，5=退货，6=盘点',
    `change_quantity` INT NOT NULL COMMENT '变更数量（正数=增加，负数=减少）',
    `before_stock` INT UNSIGNED NOT NULL COMMENT '变更前库存',
    `after_stock` INT UNSIGNED NOT NULL COMMENT '变更后库存',
    `source_type` TINYINT NOT NULL COMMENT '来源类型：1=采购入库，2=订单出库，3=退货入库，4=秒杀锁定',
    `source_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '来源ID（订单ID/采购单ID）',
    `source_no` VARCHAR(64) DEFAULT NULL COMMENT '来源单号',
    `operator_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '操作人ID',
    `operator_name` VARCHAR(64) DEFAULT NULL COMMENT '操作人姓名',
    `remark` VARCHAR(512) DEFAULT NULL COMMENT '备注',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_sku_id` (`sku_id`),
    KEY `idx_source_type_id` (`source_type`, `source_id`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='库存变更日志表';

-- 库存锁定记录表（秒杀预扣专用）
DROP TABLE IF EXISTS `inventory_lock`;
CREATE TABLE `inventory_lock` (
    `id` BIGINT UNSIGNED NOT NULL COMMENT '锁定ID',
    `sku_id` BIGINT UNSIGNED NOT NULL COMMENT 'SKU ID',
    `warehouse_id` BIGINT UNSIGNED NOT NULL COMMENT '仓库ID',
    `lock_quantity` INT UNSIGNED NOT NULL COMMENT '锁定数量',
    `lock_type` TINYINT NOT NULL COMMENT '锁定类型：1=秒杀，2=订单',
    `source_id` BIGINT UNSIGNED NOT NULL COMMENT '来源ID（秒杀活动ID/订单ID）',
    `source_no` VARCHAR(64) NOT NULL COMMENT '来源单号',
    `user_id` BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
    `status` TINYINT DEFAULT 0 COMMENT '状态：0=锁定中，1=已扣减，2=已释放',
    `expire_time` DATETIME NOT NULL COMMENT '过期时间（超时自动释放）',
    `deduct_time` DATETIME DEFAULT NULL COMMENT '扣减时间',
    `release_time` DATETIME DEFAULT NULL COMMENT '释放时间',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_sku_id` (`sku_id`),
    KEY `idx_source_no` (`source_no`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_status` (`status`),
    KEY `idx_expire_time` (`expire_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='库存锁定记录表';

-- 仓库表
DROP TABLE IF EXISTS `inventory_warehouse`;
CREATE TABLE `inventory_warehouse` (
    `id` BIGINT UNSIGNED NOT NULL COMMENT '仓库ID',
    `warehouse_name` VARCHAR(128) NOT NULL COMMENT '仓库名称',
    `warehouse_code` VARCHAR(32) NOT NULL COMMENT '仓库编码',
    `warehouse_type` TINYINT DEFAULT 1 COMMENT '仓库类型：1=自营仓，2=第三方仓',
    `province` VARCHAR(32) NOT NULL COMMENT '省份',
    `city` VARCHAR(32) NOT NULL COMMENT '城市',
    `district` VARCHAR(32) NOT NULL COMMENT '区县',
    `address` VARCHAR(255) NOT NULL COMMENT '详细地址',
    `contact_name` VARCHAR(64) DEFAULT NULL COMMENT '联系人',
    `contact_phone` VARCHAR(20) DEFAULT NULL COMMENT '联系电话',
    `status` TINYINT DEFAULT 1 COMMENT '状态：0=禁用，1=启用',
    `sort_order` INT DEFAULT 0 COMMENT '排序',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `is_deleted` TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_warehouse_code` (`warehouse_code`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='仓库表';

-- 库存预警记录表
DROP TABLE IF EXISTS `inventory_alert`;
CREATE TABLE `inventory_alert` (
    `id` BIGINT UNSIGNED NOT NULL COMMENT '预警ID',
    `sku_id` BIGINT UNSIGNED NOT NULL COMMENT 'SKU ID',
    `warehouse_id` BIGINT UNSIGNED NOT NULL COMMENT '仓库ID',
    `alert_type` TINYINT NOT NULL COMMENT '预警类型：1=低库存，2=零库存，3=滞销',
    `current_stock` INT UNSIGNED NOT NULL COMMENT '当前库存',
    `threshold` INT UNSIGNED NOT NULL COMMENT '预警阈值',
    `alert_status` TINYINT DEFAULT 0 COMMENT '预警状态：0=未处理，1=已处理，2=已忽略',
    `handle_remark` VARCHAR(512) DEFAULT NULL COMMENT '处理备注',
    `handle_time` DATETIME DEFAULT NULL COMMENT '处理时间',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_sku_id` (`sku_id`),
    KEY `idx_alert_status` (`alert_status`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='库存预警记录表';

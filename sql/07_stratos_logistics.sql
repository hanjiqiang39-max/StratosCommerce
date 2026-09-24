-- =============================================================
-- 物流服务数据库表结构 (stratos_logistics)
-- =============================================================

USE `stratos_logistics`;

-- 物流公司表
DROP TABLE IF EXISTS `logistics_company`;
CREATE TABLE `logistics_company` (
    `id` BIGINT UNSIGNED NOT NULL COMMENT '物流公司ID',
    `company_name` VARCHAR(64) NOT NULL COMMENT '公司名称',
    `company_code` VARCHAR(32) NOT NULL COMMENT '公司编码',
    `company_logo` VARCHAR(255) DEFAULT NULL COMMENT '公司Logo',
    `contact_phone` VARCHAR(20) DEFAULT NULL COMMENT '联系电话',
    `website` VARCHAR(255) DEFAULT NULL COMMENT '官网',
    `api_url` VARCHAR(255) DEFAULT NULL COMMENT 'API地址',
    `api_key` VARCHAR(128) DEFAULT NULL COMMENT 'API密钥',
    `sort_order` INT DEFAULT 0 COMMENT '排序',
    `status` TINYINT DEFAULT 1 COMMENT '状态：0=禁用，1=启用',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `is_deleted` TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_company_code` (`company_code`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='物流公司表';

-- 物流轨迹表
DROP TABLE IF EXISTS `logistics_trace`;
CREATE TABLE `logistics_trace` (
    `id` BIGINT UNSIGNED NOT NULL COMMENT '轨迹ID',
    `order_id` BIGINT UNSIGNED NOT NULL COMMENT '订单ID',
    `order_no` VARCHAR(64) NOT NULL COMMENT '订单号',
    `logistics_company_code` VARCHAR(32) NOT NULL COMMENT '物流公司编码',
    `logistics_no` VARCHAR(64) NOT NULL COMMENT '物流单号',
    `trace_time` DATETIME NOT NULL COMMENT '轨迹时间',
    `trace_status` VARCHAR(32) DEFAULT NULL COMMENT '状态码',
    `trace_desc` VARCHAR(512) NOT NULL COMMENT '轨迹描述',
    `trace_location` VARCHAR(255) DEFAULT NULL COMMENT '所在地点',
    `operator` VARCHAR(64) DEFAULT NULL COMMENT '操作人',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_order_id` (`order_id`),
    KEY `idx_order_no` (`order_no`),
    KEY `idx_logistics_no` (`logistics_no`),
    KEY `idx_trace_time` (`trace_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='物流轨迹表';

-- 运费模板表
DROP TABLE IF EXISTS `logistics_freight_template`;
CREATE TABLE `logistics_freight_template` (
    `id` BIGINT UNSIGNED NOT NULL COMMENT '模板ID',
    `template_name` VARCHAR(128) NOT NULL COMMENT '模板名称',
    `charge_type` TINYINT NOT NULL COMMENT '计费方式：1=按件数，2=按重量，3=按体积',
    `is_free_shipping` TINYINT DEFAULT 0 COMMENT '是否包邮：0=否，1=是',
    `free_shipping_amount` DECIMAL(12,2) DEFAULT NULL COMMENT '包邮金额（满X元包邮）',
    `default_first_unit` DECIMAL(10,2) NOT NULL COMMENT '默认首件/首重/首体积',
    `default_first_fee` DECIMAL(12,2) NOT NULL COMMENT '默认首费',
    `default_continue_unit` DECIMAL(10,2) NOT NULL COMMENT '默认续件/续重/续体积',
    `default_continue_fee` DECIMAL(12,2) NOT NULL COMMENT '默认续费',
    `status` TINYINT DEFAULT 1 COMMENT '状态：0=禁用，1=启用',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `create_by` VARCHAR(64) DEFAULT NULL,
    `update_by` VARCHAR(64) DEFAULT NULL,
    `is_deleted` TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='运费模板表';

-- 运费模板区域规则表
DROP TABLE IF EXISTS `logistics_freight_region`;
CREATE TABLE `logistics_freight_region` (
    `id` BIGINT UNSIGNED NOT NULL COMMENT '规则ID',
    `template_id` BIGINT UNSIGNED NOT NULL COMMENT '模板ID',
    `region_type` TINYINT NOT NULL COMMENT '区域类型：1=省份，2=城市',
    `region_codes` TEXT NOT NULL COMMENT '区域编码（逗号分隔）',
    `first_unit` DECIMAL(10,2) NOT NULL COMMENT '首件/首重/首体积',
    `first_fee` DECIMAL(12,2) NOT NULL COMMENT '首费',
    `continue_unit` DECIMAL(10,2) NOT NULL COMMENT '续件/续重/续体积',
    `continue_fee` DECIMAL(12,2) NOT NULL COMMENT '续费',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_template_id` (`template_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='运费模板区域规则表';

-- 物流异常记录表
DROP TABLE IF EXISTS `logistics_exception`;
CREATE TABLE `logistics_exception` (
    `id` BIGINT UNSIGNED NOT NULL COMMENT '异常ID',
    `order_id` BIGINT UNSIGNED NOT NULL COMMENT '订单ID',
    `order_no` VARCHAR(64) NOT NULL COMMENT '订单号',
    `logistics_no` VARCHAR(64) NOT NULL COMMENT '物流单号',
    `exception_type` TINYINT NOT NULL COMMENT '异常类型：1=派件异常，2=签收异常，3=退件，4=丢件',
    `exception_desc` VARCHAR(512) NOT NULL COMMENT '异常描述',
    `exception_time` DATETIME NOT NULL COMMENT '异常时间',
    `handle_status` TINYINT DEFAULT 0 COMMENT '处理状态：0=待处理，1=处理中，2=已处理',
    `handle_result` VARCHAR(512) DEFAULT NULL COMMENT '处理结果',
    `handle_time` DATETIME DEFAULT NULL COMMENT '处理时间',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_order_id` (`order_id`),
    KEY `idx_logistics_no` (`logistics_no`),
    KEY `idx_handle_status` (`handle_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='物流异常记录表';

-- =============================================================
-- 商品服务数据库表结构 (stratos_product)
-- =============================================================

USE `stratos_product`;

-- 商品SPU表（Standard Product Unit 标准产品单元）
DROP TABLE IF EXISTS `product_spu`;
CREATE TABLE `product_spu` (
    `id` BIGINT UNSIGNED NOT NULL COMMENT 'SPU ID',
    `spu_name` VARCHAR(255) NOT NULL COMMENT 'SPU名称',
    `spu_code` VARCHAR(64) NOT NULL COMMENT 'SPU编码',
    `category_id` BIGINT UNSIGNED NOT NULL COMMENT '分类ID',
    `brand_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '品牌ID',
    `title` VARCHAR(255) NOT NULL COMMENT '商品标题',
    `sub_title` VARCHAR(255) DEFAULT NULL COMMENT '副标题',
    `main_image` VARCHAR(512) NOT NULL COMMENT '主图URL',
    `image_list` TEXT DEFAULT NULL COMMENT '图片列表（JSON数组）',
    `detail_html` TEXT DEFAULT NULL COMMENT '详情HTML（富文本）',
    `weight` DECIMAL(10,2) DEFAULT 0.00 COMMENT '重量（kg）',
    `volume` DECIMAL(10,2) DEFAULT 0.00 COMMENT '体积（立方米）',
    `selling_point` VARCHAR(512) DEFAULT NULL COMMENT '卖点',
    `tags` VARCHAR(255) DEFAULT NULL COMMENT '标签（逗号分隔）',
    `sort_order` INT DEFAULT 0 COMMENT '排序',
    `status` TINYINT DEFAULT 0 COMMENT '状态：0=下架，1=上架，2=预售',
    `publish_status` TINYINT DEFAULT 0 COMMENT '发布状态：0=未发布，1=已发布',
    `audit_status` TINYINT DEFAULT 0 COMMENT '审核状态：0=待审核，1=审核通过，2=审核拒绝',
    `sale_count` INT UNSIGNED DEFAULT 0 COMMENT '销量',
    `view_count` INT UNSIGNED DEFAULT 0 COMMENT '浏览量',
    `favorite_count` INT UNSIGNED DEFAULT 0 COMMENT '收藏量',
    `comment_count` INT UNSIGNED DEFAULT 0 COMMENT '评论数',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `create_by` VARCHAR(64) DEFAULT NULL,
    `update_by` VARCHAR(64) DEFAULT NULL,
    `is_deleted` TINYINT NOT NULL DEFAULT 0,
    `version` INT NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_spu_code` (`spu_code`),
    KEY `idx_category_id` (`category_id`),
    KEY `idx_brand_id` (`brand_id`),
    KEY `idx_status` (`status`),
    KEY `idx_sale_count` (`sale_count`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='商品SPU表';

-- 商品SKU表（Stock Keeping Unit 库存单位）
DROP TABLE IF EXISTS `product_sku`;
CREATE TABLE `product_sku` (
    `id` BIGINT UNSIGNED NOT NULL COMMENT 'SKU ID',
    `spu_id` BIGINT UNSIGNED NOT NULL COMMENT 'SPU ID',
    `sku_name` VARCHAR(255) NOT NULL COMMENT 'SKU名称',
    `sku_code` VARCHAR(64) NOT NULL COMMENT 'SKU编码',
    `bar_code` VARCHAR(64) DEFAULT NULL COMMENT '条形码',
    `sku_image` VARCHAR(512) DEFAULT NULL COMMENT 'SKU图片',
    `spec_json` VARCHAR(1024) DEFAULT NULL COMMENT '规格JSON（如：{"颜色":"红色","尺寸":"XL"}）',
    `price` DECIMAL(12,2) NOT NULL COMMENT '售价',
    `original_price` DECIMAL(12,2) DEFAULT NULL COMMENT '原价',
    `cost_price` DECIMAL(12,2) DEFAULT NULL COMMENT '成本价',
    `stock` INT UNSIGNED DEFAULT 0 COMMENT '库存数量',
    `lock_stock` INT UNSIGNED DEFAULT 0 COMMENT '锁定库存（秒杀预扣）',
    `low_stock_threshold` INT UNSIGNED DEFAULT 10 COMMENT '库存预警阈值',
    `weight` DECIMAL(10,2) DEFAULT 0.00 COMMENT '重量（kg）',
    `status` TINYINT DEFAULT 1 COMMENT '状态：0=禁用，1=启用',
    `sort_order` INT DEFAULT 0 COMMENT '排序',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `is_deleted` TINYINT NOT NULL DEFAULT 0,
    `version` INT NOT NULL DEFAULT 0 COMMENT '乐观锁（库存更新必须用）',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_sku_code` (`sku_code`),
    KEY `idx_spu_id` (`spu_id`),
    KEY `idx_price` (`price`),
    KEY `idx_stock` (`stock`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='商品SKU表';

-- 商品分类表
DROP TABLE IF EXISTS `product_category`;
CREATE TABLE `product_category` (
    `id` BIGINT UNSIGNED NOT NULL COMMENT '分类ID',
    `parent_id` BIGINT UNSIGNED DEFAULT 0 COMMENT '父分类ID（0=顶级分类）',
    `category_name` VARCHAR(64) NOT NULL COMMENT '分类名称',
    `category_code` VARCHAR(32) NOT NULL COMMENT '分类编码',
    `category_level` TINYINT NOT NULL COMMENT '层级：1=一级，2=二级，3=三级',
    `icon` VARCHAR(255) DEFAULT NULL COMMENT '图标URL',
    `image` VARCHAR(512) DEFAULT NULL COMMENT '分类图片',
    `keywords` VARCHAR(255) DEFAULT NULL COMMENT '关键词',
    `description` VARCHAR(512) DEFAULT NULL COMMENT '描述',
    `sort_order` INT DEFAULT 0 COMMENT '排序',
    `show_status` TINYINT DEFAULT 1 COMMENT '显示状态：0=隐藏，1=显示',
    `nav_status` TINYINT DEFAULT 0 COMMENT '导航显示：0=否，1=是',
    `product_count` INT UNSIGNED DEFAULT 0 COMMENT '商品数量',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `is_deleted` TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_category_code` (`category_code`),
    KEY `idx_parent_id` (`parent_id`),
    KEY `idx_level` (`category_level`),
    KEY `idx_sort_order` (`sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='商品分类表';

-- 品牌表
DROP TABLE IF EXISTS `product_brand`;
CREATE TABLE `product_brand` (
    `id` BIGINT UNSIGNED NOT NULL COMMENT '品牌ID',
    `brand_name` VARCHAR(64) NOT NULL COMMENT '品牌名称',
    `brand_code` VARCHAR(32) NOT NULL COMMENT '品牌编码',
    `brand_logo` VARCHAR(512) DEFAULT NULL COMMENT '品牌Logo',
    `brand_desc` TEXT DEFAULT NULL COMMENT '品牌描述',
    `brand_story` TEXT DEFAULT NULL COMMENT '品牌故事',
    `official_website` VARCHAR(255) DEFAULT NULL COMMENT '官方网站',
    `first_letter` CHAR(1) DEFAULT NULL COMMENT '首字母',
    `sort_order` INT DEFAULT 0 COMMENT '排序',
    `show_status` TINYINT DEFAULT 1 COMMENT '显示状态：0=隐藏，1=显示',
    `product_count` INT UNSIGNED DEFAULT 0 COMMENT '商品数量',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `is_deleted` TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_brand_code` (`brand_code`),
    KEY `idx_first_letter` (`first_letter`),
    KEY `idx_show_status` (`show_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='品牌表';

-- 商品属性表（规格属性，如：颜色、尺寸）
DROP TABLE IF EXISTS `product_attr`;
CREATE TABLE `product_attr` (
    `id` BIGINT UNSIGNED NOT NULL COMMENT '属性ID',
    `attr_name` VARCHAR(64) NOT NULL COMMENT '属性名称',
    `category_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '所属分类ID（NULL=通用属性）',
    `attr_type` TINYINT NOT NULL COMMENT '属性类型：1=规格（SKU差异），2=参数（SPU通用）',
    `input_type` TINYINT DEFAULT 1 COMMENT '录入方式：1=手工录入，2=单选，3=多选',
    `value_list` TEXT DEFAULT NULL COMMENT '可选值列表（JSON数组）',
    `unit` VARCHAR(16) DEFAULT NULL COMMENT '单位',
    `sort_order` INT DEFAULT 0 COMMENT '排序',
    `is_required` TINYINT DEFAULT 0 COMMENT '是否必填：0=否，1=是',
    `status` TINYINT DEFAULT 1 COMMENT '状态：0=禁用，1=启用',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `is_deleted` TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `idx_category_id` (`category_id`),
    KEY `idx_attr_type` (`attr_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='商品属性表';

-- 商品评价表
DROP TABLE IF EXISTS `product_comment`;
CREATE TABLE `product_comment` (
    `id` BIGINT UNSIGNED NOT NULL COMMENT '评价ID',
    `spu_id` BIGINT UNSIGNED NOT NULL COMMENT 'SPU ID',
    `sku_id` BIGINT UNSIGNED NOT NULL COMMENT 'SKU ID',
    `user_id` BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
    `order_id` BIGINT UNSIGNED NOT NULL COMMENT '订单ID',
    `nickname` VARCHAR(64) DEFAULT NULL COMMENT '昵称（脱敏）',
    `star_rating` TINYINT NOT NULL COMMENT '星级评分：1~5星',
    `content` TEXT NOT NULL COMMENT '评价内容',
    `images` TEXT DEFAULT NULL COMMENT '晒图（JSON数组）',
    `spec_desc` VARCHAR(255) DEFAULT NULL COMMENT '规格描述（如：红色 XL）',
    `reply_content` TEXT DEFAULT NULL COMMENT '商家回复内容',
    `reply_time` DATETIME DEFAULT NULL COMMENT '回复时间',
    `likes_count` INT UNSIGNED DEFAULT 0 COMMENT '点赞数',
    `is_anonymous` TINYINT DEFAULT 0 COMMENT '是否匿名：0=否，1=是',
    `audit_status` TINYINT DEFAULT 0 COMMENT '审核状态：0=待审核，1=通过，2=拒绝',
    `show_status` TINYINT DEFAULT 1 COMMENT '显示状态：0=隐藏，1=显示',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `is_deleted` TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `idx_spu_id` (`spu_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_order_id` (`order_id`),
    KEY `idx_star_rating` (`star_rating`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='商品评价表';

-- 商品标签表
DROP TABLE IF EXISTS `product_tag`;
CREATE TABLE `product_tag` (
    `id` BIGINT UNSIGNED NOT NULL COMMENT '标签ID',
    `tag_name` VARCHAR(32) NOT NULL COMMENT '标签名称',
    `tag_type` TINYINT DEFAULT 1 COMMENT '标签类型：1=系统标签，2=自定义标签',
    `tag_color` VARCHAR(16) DEFAULT '#38BDF8' COMMENT '标签颜色',
    `sort_order` INT DEFAULT 0 COMMENT '排序',
    `status` TINYINT DEFAULT 1 COMMENT '状态：0=禁用，1=启用',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `is_deleted` TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_tag_name` (`tag_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='商品标签表';

-- 商品标签关联表
DROP TABLE IF EXISTS `product_tag_relation`;
CREATE TABLE `product_tag_relation` (
    `id` BIGINT UNSIGNED NOT NULL COMMENT '关联ID',
    `spu_id` BIGINT UNSIGNED NOT NULL COMMENT 'SPU ID',
    `tag_id` BIGINT UNSIGNED NOT NULL COMMENT '标签ID',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_spu_tag` (`spu_id`, `tag_id`),
    KEY `idx_tag_id` (`tag_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='商品标签关联表';

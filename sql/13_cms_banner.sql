USE `stratos_system`;

CREATE TABLE IF NOT EXISTS `cms_banner` (
    `id` BIGINT UNSIGNED NOT NULL COMMENT '轮播ID',
    `title` VARCHAR(128) NOT NULL COMMENT '标题',
    `image_url` VARCHAR(512) NOT NULL COMMENT '图片地址',
    `link_url` VARCHAR(512) DEFAULT NULL COMMENT '跳转地址',
    `position` TINYINT NOT NULL DEFAULT 1 COMMENT '位置：1=首页',
    `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0=下线，1=上线',
    `start_time` DATETIME DEFAULT NULL COMMENT '生效时间',
    `end_time` DATETIME DEFAULT NULL COMMENT '失效时间',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `create_by` VARCHAR(64) DEFAULT NULL,
    `update_by` VARCHAR(64) DEFAULT NULL,
    `is_deleted` TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `idx_status_position` (`status`, `position`, `sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='首页轮播表';

INSERT INTO `cms_banner` (`id`, `title`, `image_url`, `link_url`, `position`, `sort_order`, `status`)
SELECT 1, 'Stratos 精选', '/banner.svg', '/search?keyword=phone', 1, 1, 1
WHERE NOT EXISTS (SELECT 1 FROM `cms_banner` WHERE `id` = 1);

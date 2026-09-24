-- 满减 / 拼团演示活动（已有库可单独执行）
USE `stratos_promotion`;

INSERT IGNORE INTO `promotion_full_discount` (`id`, `activity_name`, `activity_code`, `full_amount`, `discount_amount`, `use_type`, `start_time`, `end_time`, `status`, `sort_order`, `used_count`) VALUES
(1, '演示满减99减10', 'FULL_DEMO', 99.00, 10.00, 1, NOW() - INTERVAL 1 DAY, NOW() + INTERVAL 30 DAY, 1, 1, 0);

INSERT IGNORE INTO `promotion_group_buying` (`id`, `activity_name`, `activity_code`, `spu_id`, `sku_id`, `original_price`, `group_price`, `require_num`, `limit_hours`, `limit_per_user`, `start_time`, `end_time`, `status`) VALUES
(1, '演示拼团2人成团', 'GROUP_DEMO', 10001, 1000101, 129.00, 79.00, 2, 24, 1, NOW() - INTERVAL 1 HOUR, NOW() + INTERVAL 7 DAY, 1);

USE `stratos_message`;

INSERT INTO `message_notice` (
    `id`, `title`, `content`, `notice_type`, `target_scope`,
    `publish_status`, `publish_time`, `top_flag`, `sort_order`, `read_count`, `version`, `is_deleted`
)
SELECT 1, '欢迎来到 StratosCommerce', '商城已开放注册、下单与支付。', 1, 0, 1, NOW(), 1, 1, 0, 0, 0
WHERE NOT EXISTS (SELECT 1 FROM `message_notice` WHERE `id` = 1);

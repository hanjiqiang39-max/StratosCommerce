-- =============================================================
-- StratosCommerce 数据库初始化脚本
-- 说明：每个微服务独立数据库（Database per Service）
-- 字符集：utf8mb4 / 排序规则：utf8mb4_0900_ai_ci (MySQL 8.0)
-- 执行顺序：00 -> 01 ~ 09 -> 99
-- =============================================================

-- 用户服务
CREATE DATABASE IF NOT EXISTS `stratos_user`
    DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;

-- 商品服务
CREATE DATABASE IF NOT EXISTS `stratos_product`
    DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;

-- 订单服务（分库分表：stratos_order_0 / stratos_order_1）
CREATE DATABASE IF NOT EXISTS `stratos_order_0`
    DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
CREATE DATABASE IF NOT EXISTS `stratos_order_1`
    DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;

-- 支付服务
CREATE DATABASE IF NOT EXISTS `stratos_payment`
    DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;

-- 营销服务
CREATE DATABASE IF NOT EXISTS `stratos_promotion`
    DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;

-- 库存服务
CREATE DATABASE IF NOT EXISTS `stratos_inventory`
    DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;

-- 物流服务
CREATE DATABASE IF NOT EXISTS `stratos_logistics`
    DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;

-- 消息服务
CREATE DATABASE IF NOT EXISTS `stratos_message`
    DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;

-- 系统管理（管理后台 RBAC）
CREATE DATABASE IF NOT EXISTS `stratos_system`
    DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;

-- 通知服务
CREATE DATABASE IF NOT EXISTS `stratos_notification`
    DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;

-- =============================================================
-- 应用账号（生产环境请替换为强密码，并按库最小化授权）
-- =============================================================
-- CREATE USER 'stratos_app'@'%' IDENTIFIED BY 'CHANGE_ME_StrongPassw0rd!';
-- GRANT SELECT, INSERT, UPDATE, DELETE ON `stratos_user`.*      TO 'stratos_app'@'%';
-- GRANT SELECT, INSERT, UPDATE, DELETE ON `stratos_product`.*   TO 'stratos_app'@'%';
-- GRANT SELECT, INSERT, UPDATE, DELETE ON `stratos_order_0`.*   TO 'stratos_app'@'%';
-- GRANT SELECT, INSERT, UPDATE, DELETE ON `stratos_order_1`.*   TO 'stratos_app'@'%';
-- GRANT SELECT, INSERT, UPDATE, DELETE ON `stratos_payment`.*   TO 'stratos_app'@'%';
-- GRANT SELECT, INSERT, UPDATE, DELETE ON `stratos_promotion`.* TO 'stratos_app'@'%';
-- GRANT SELECT, INSERT, UPDATE, DELETE ON `stratos_inventory`.* TO 'stratos_app'@'%';
-- GRANT SELECT, INSERT, UPDATE, DELETE ON `stratos_logistics`.* TO 'stratos_app'@'%';
-- GRANT SELECT, INSERT, UPDATE, DELETE ON `stratos_message`.*      TO 'stratos_app'@'%';
-- GRANT SELECT, INSERT, UPDATE, DELETE ON `stratos_system`.*       TO 'stratos_app'@'%';
-- GRANT SELECT, INSERT, UPDATE, DELETE ON `stratos_notification`.* TO 'stratos_app'@'%';
-- FLUSH PRIVILEGES;

-- =============================================================
-- 设计约定
-- =============================================================
-- 1. 主键 id 为 BIGINT UNSIGNED，由应用层雪花算法生成，不使用 AUTO_INCREMENT
-- 2. 金额统一使用 DECIMAL(12,2)，禁止使用 FLOAT/DOUBLE
-- 3. 逻辑删除字段 is_deleted：0=未删除，1=已删除
-- 4. 乐观锁字段 version，MyBatis-Plus @Version 管理
-- 5. 不使用物理外键，约束由应用层保证（分库分表兼容性）
-- 6. 敏感字段（手机号/身份证/银行卡）密文存储，附加 _hash 列用于等值查询

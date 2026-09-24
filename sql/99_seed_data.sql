-- =============================================================
-- StratosCommerce 初始化种子数据
-- 说明：包含系统必需的初始数据和测试数据
-- 执行顺序：在所有DDL执行完成后再执行
-- =============================================================

-- =============================================================
-- 用户服务初始数据
-- =============================================================
USE `stratos_user`;

-- 会员等级数据
INSERT INTO `user_level` (`id`, `level_name`, `level_code`, `growth_value_min`, `growth_value_max`, `discount_rate`, `free_shipping_threshold`, `sort_order`, `status`) VALUES
(1, '普通会员', 'NORMAL', 0, 999, 100.00, NULL, 1, 1),
(2, '铜牌会员', 'BRONZE', 1000, 4999, 98.00, 99.00, 2, 1),
(3, '银牌会员', 'SILVER', 5000, 14999, 95.00, 79.00, 3, 1),
(4, '金牌会员', 'GOLD', 15000, 49999, 92.00, 59.00, 4, 1),
(5, '钻石会员', 'DIAMOND', 50000, 999999, 88.00, 0.00, 5, 1);

-- =============================================================
-- 商品服务初始数据
-- =============================================================
USE `stratos_product`;

-- 商品分类数据（三级分类示例）
INSERT INTO `product_category` (`id`, `parent_id`, `category_name`, `category_code`, `category_level`, `sort_order`, `show_status`, `nav_status`) VALUES
-- 一级分类
(1, 0, '数码电器', 'digital', 1, 1, 1, 1),
(2, 0, '服装鞋包', 'clothing', 1, 2, 1, 1),
(3, 0, '食品生鲜', 'food', 1, 3, 1, 1),
(4, 0, '家居家装', 'home', 1, 4, 1, 1),
-- 二级分类
(101, 1, '手机通讯', 'phone', 2, 1, 1, 1),
(102, 1, '电脑办公', 'computer', 2, 2, 1, 1),
(103, 1, '数码配件', 'accessory', 2, 3, 1, 1),
(201, 2, '男装', 'men_clothing', 2, 1, 1, 1),
(202, 2, '女装', 'women_clothing', 2, 2, 1, 1),
(203, 2, '运动户外', 'sports', 2, 3, 1, 1),
-- 三级分类
(10101, 101, '智能手机', 'smartphone', 3, 1, 1, 0),
(10102, 101, '5G手机', '5g_phone', 3, 2, 1, 0),
(10201, 102, '笔记本电脑', 'laptop', 3, 1, 1, 0),
(10202, 102, '台式机', 'desktop', 3, 2, 1, 0);

-- 品牌数据
INSERT INTO `product_brand` (`id`, `brand_name`, `brand_code`, `first_letter`, `sort_order`, `show_status`) VALUES
(1, 'Apple', 'apple', 'A', 1, 1),
(2, 'Huawei', 'huawei', 'H', 2, 1),
(3, 'Xiaomi', 'xiaomi', 'X', 3, 1),
(4, 'Samsung', 'samsung', 'S', 4, 1),
(5, 'OPPO', 'oppo', 'O', 5, 1),
(6, 'Vivo', 'vivo', 'V', 6, 1);

-- 商品标签数据
INSERT INTO `product_tag` (`id`, `tag_name`, `tag_type`, `tag_color`, `status`) VALUES
(1, '热销', 1, '#E9A568', 1),
(2, '新品', 1, '#38BDF8', 1),
(3, '爆款', 1, '#6EE7B7', 1),
(4, '折扣', 1, '#3B6DFF', 1),
(5, '包邮', 1, '#F472B6', 1);

-- 联调用演示商品（上架）
INSERT INTO `product_spu` (`id`, `spu_name`, `spu_code`, `category_id`, `brand_id`, `title`, `sub_title`, `main_image`, `status`, `publish_status`, `audit_status`, `sale_count`, `view_count`) VALUES
(10001, '演示手机', 'SPU10001', 10101, 1, 'Stratos 演示智能手机', '用于下单联调', '/placeholder.svg', 1, 1, 1, 0, 0);

INSERT INTO `product_sku` (`id`, `spu_id`, `sku_name`, `sku_code`, `spec_json`, `price`, `original_price`, `stock`, `lock_stock`, `status`, `sort_order`, `version`) VALUES
(1000101, 10001, '演示手机 黑色 128G', 'SKU1000101', '{"颜色":"黑色","内存":"128G"}', 99.00, 129.00, 100, 0, 1, 1, 0);

-- =============================================================
-- 库存服务初始数据
-- =============================================================
USE `stratos_inventory`;

-- 仓库数据
INSERT INTO `inventory_warehouse` (`id`, `warehouse_name`, `warehouse_code`, `warehouse_type`, `province`, `city`, `district`, `address`, `status`) VALUES
(1, '北京中心仓', 'WH_BJ_01', 1, '北京市', '朝阳区', '朝阳区', '朝阳路888号', 1),
(2, '上海中心仓', 'WH_SH_01', 1, '上海市', '浦东新区', '浦东新区', '张江高科技园区', 1),
(3, '广州中心仓', 'WH_GZ_01', 1, '广东省', '广州市', '天河区', '天河软件园', 1),
(4, '深圳中心仓', 'WH_SZ_01', 1, '广东省', '深圳市', '南山区', '科技园', 1);

INSERT INTO `inventory_stock` (`id`, `sku_id`, `warehouse_id`, `total_stock`, `available_stock`, `lock_stock`, `sold_stock`, `in_transit_stock`, `low_stock_threshold`, `status`, `version`) VALUES
(1000101, 1000101, 1, 100, 100, 0, 0, 0, 10, 1, 0);

-- =============================================================
-- 营销服务初始数据
-- =============================================================
USE `stratos_promotion`;

INSERT INTO `promotion_seckill` (`id`, `activity_name`, `activity_code`, `spu_id`, `sku_id`, `original_price`, `seckill_price`, `seckill_stock`, `limit_per_user`, `start_time`, `end_time`, `status`, `sold_count`) VALUES
(1, '演示秒杀', 'SECKILL_DEMO', 10001, 1000101, 129.00, 9.90, 50, 1, NOW() - INTERVAL 1 HOUR, NOW() + INTERVAL 7 DAY, 1, 0);

INSERT INTO `promotion_coupon` (`id`, `coupon_name`, `coupon_code`, `coupon_type`, `discount_type`, `discount_value`, `min_amount`, `publish_count`, `received_count`, `used_count`, `limit_per_user`, `receive_type`, `use_type`, `valid_days`, `start_time`, `end_time`, `status`) VALUES
(1, '新人满50减10', 'COUPON_NEW10', 1, 1, 10.00, 50.00, 1000, 0, 0, 1, 1, 1, 7, NOW() - INTERVAL 1 DAY, NOW() + INTERVAL 30 DAY, 1);

INSERT INTO `promotion_full_discount` (`id`, `activity_name`, `activity_code`, `full_amount`, `discount_amount`, `use_type`, `start_time`, `end_time`, `status`, `sort_order`, `used_count`) VALUES
(1, '演示满减99减10', 'FULL_DEMO', 99.00, 10.00, 1, NOW() - INTERVAL 1 DAY, NOW() + INTERVAL 30 DAY, 1, 1, 0);

INSERT INTO `promotion_group_buying` (`id`, `activity_name`, `activity_code`, `spu_id`, `sku_id`, `original_price`, `group_price`, `require_num`, `limit_hours`, `limit_per_user`, `start_time`, `end_time`, `status`) VALUES
(1, '演示拼团2人成团', 'GROUP_DEMO', 10001, 1000101, 129.00, 79.00, 2, 24, 1, NOW() - INTERVAL 1 HOUR, NOW() + INTERVAL 7 DAY, 1);

-- =============================================================
-- 物流服务初始数据
-- =============================================================
USE `stratos_logistics`;

-- 物流公司数据
INSERT INTO `logistics_company` (`id`, `company_name`, `company_code`, `status`) VALUES
(1, '顺丰速运', 'SF', 1),
(2, '中通快递', 'ZTO', 1),
(3, '圆通速递', 'YTO', 1),
(4, '申通快递', 'STO', 1),
(5, '韵达快递', 'YD', 1),
(6, '京东物流', 'JD', 1),
(7, '邮政EMS', 'EMS', 1);

-- 运费模板数据（默认包邮模板）
INSERT INTO `logistics_freight_template` (`id`, `template_name`, `charge_type`, `is_free_shipping`, `free_shipping_amount`, `default_first_unit`, `default_first_fee`, `default_continue_unit`, `default_continue_fee`, `status`) VALUES
(1, '全国包邮', 1, 1, 0.00, 1, 0.00, 1, 0.00, 1),
(2, '标准快递（按件）', 1, 0, NULL, 1, 10.00, 1, 5.00, 1),
(3, '标准快递（按重）', 2, 0, NULL, 1.00, 10.00, 1.00, 5.00, 1);

-- =============================================================
-- 系统管理初始数据
-- =============================================================
USE `stratos_system`;

-- 超级管理员（密码：admin123，实际环境请修改）
INSERT INTO `sys_admin` (`id`, `username`, `password`, `real_name`, `nickname`, `status`, `is_super_admin`) VALUES
(1, 'admin', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE/TU5XbDQL1EO', '超级管理员', 'Admin', 1, 1);

-- 部门数据
INSERT INTO `sys_dept` (`id`, `parent_id`, `dept_name`, `dept_code`, `sort_order`, `status`) VALUES
(1, 0, 'StratosCommerce', 'ROOT', 0, 1),
(2, 1, '技术部', 'TECH', 1, 1),
(3, 1, '运营部', 'OPS', 2, 1),
(4, 1, '客服部', 'CS', 3, 1);

-- 角色数据
INSERT INTO `sys_role` (`id`, `role_name`, `role_code`, `data_scope`, `sort_order`, `status`) VALUES
(1, '超级管理员', 'SUPER_ADMIN', 1, 1, 1),
(2, '系统管理员', 'SYSTEM_ADMIN', 2, 2, 1),
(3, '运营人员', 'OPERATOR', 3, 3, 1),
(4, '客服人员', 'CS_STAFF', 5, 4, 1);

-- 管理员角色关联
INSERT INTO `sys_admin_role` (`id`, `admin_id`, `role_id`) VALUES
(1, 1, 1);

-- 菜单数据（示例）
INSERT INTO `sys_menu` (`id`, `parent_id`, `menu_name`, `menu_type`, `path`, `component`, `permission`, `icon`, `sort_order`, `visible`, `status`) VALUES
-- 一级菜单
(1, 0, '系统管理', 1, '/system', NULL, NULL, 'system', 1, 1, 1),
(2, 0, '商品管理', 1, '/product', NULL, NULL, 'product', 2, 1, 1),
(3, 0, '订单管理', 1, '/order', NULL, NULL, 'order', 3, 1, 1),
(4, 0, '用户管理', 1, '/user', NULL, NULL, 'user', 4, 1, 1),
(5, 0, '营销管理', 1, '/promotion', NULL, NULL, 'promotion', 5, 1, 1),
-- 二级菜单（系统管理）
(101, 1, '管理员管理', 2, 'admin', 'system/admin/index', 'system:admin:list', 'user', 1, 1, 1),
(102, 1, '角色管理', 2, 'role', 'system/role/index', 'system:role:list', 'peoples', 2, 1, 1),
(103, 1, '菜单管理', 2, 'menu', 'system/menu/index', 'system:menu:list', 'tree-table', 3, 1, 1),
(104, 1, '部门管理', 2, 'dept', 'system/dept/index', 'system:dept:list', 'tree', 4, 1, 1),
(105, 1, '字典管理', 2, 'dict', 'system/dict/index', 'system:dict:list', 'dict', 5, 1, 1),
-- 三级菜单（按钮）
(10101, 101, '管理员新增', 3, NULL, NULL, 'system:admin:add', '', 1, 0, 1),
(10102, 101, '管理员编辑', 3, NULL, NULL, 'system:admin:edit', '', 2, 0, 1),
(10103, 101, '管理员删除', 3, NULL, NULL, 'system:admin:delete', '', 3, 0, 1),
(10104, 101, '重置密码', 3, NULL, NULL, 'system:admin:resetPwd', '', 4, 0, 1);

-- 角色菜单关联（超级管理员拥有所有菜单）
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`) VALUES
(1, 1, 1),
(2, 1, 2),
(3, 1, 3),
(4, 1, 4),
(5, 1, 5),
(6, 1, 101),
(7, 1, 102),
(8, 1, 103),
(9, 1, 104),
(10, 1, 105),
(11, 1, 10101),
(12, 1, 10102),
(13, 1, 10103),
(14, 1, 10104);

-- 字典类型数据
INSERT INTO `sys_dict_type` (`id`, `dict_name`, `dict_type`, `status`) VALUES
(1, '用户性别', 'sys_user_gender', 1),
(2, '订单状态', 'order_status', 1),
(3, '支付方式', 'pay_channel', 1),
(4, '物流公司', 'logistics_company', 1);

-- 字典数据
INSERT INTO `sys_dict_data` (`id`, `dict_type`, `dict_label`, `dict_value`, `dict_sort`, `is_default`, `status`) VALUES
-- 用户性别
(1, 'sys_user_gender', '未知', '0', 1, 1, 1),
(2, 'sys_user_gender', '男', '1', 2, 0, 1),
(3, 'sys_user_gender', '女', '2', 3, 0, 1),
-- 订单状态
(11, 'order_status', '待支付', '0', 1, 0, 1),
(12, 'order_status', '已支付', '10', 2, 0, 1),
(13, 'order_status', '待发货', '20', 3, 0, 1),
(14, 'order_status', '已发货', '30', 4, 0, 1),
(15, 'order_status', '已收货', '40', 5, 0, 1),
(16, 'order_status', '已完成', '50', 6, 0, 1),
-- 支付方式
(21, 'pay_channel', '支付宝', '1', 1, 0, 1),
(22, 'pay_channel', '微信支付', '2', 2, 1, 1),
(23, 'pay_channel', '银联支付', '3', 3, 0, 1),
(24, 'pay_channel', '余额支付', '4', 4, 0, 1);

-- 系统配置数据
INSERT INTO `sys_config` (`id`, `config_name`, `config_key`, `config_value`, `config_type`) VALUES
(1, '主框架页-默认皮肤样式名称', 'sys.index.skinName', 'skin-blue', 0),
(2, '用户管理-账号初始密码', 'sys.user.initPassword', '123456', 0),
(3, '主框架页-侧边栏主题', 'sys.index.sideTheme', 'theme-dark', 0),
(4, '订单自动确认收货天数', 'order.auto.confirm.days', '7', 1),
(5, '订单超时未支付自动取消分钟数', 'order.timeout.cancel.minutes', '30', 1);

-- =============================================================
-- 说明
-- =============================================================
-- 1. 超级管理员密码：admin123（BCrypt加密）
-- 2. 所有雪花ID已预置，实际使用时由应用层生成
-- 3. 生产环境请务必修改管理员密码和敏感配置
-- 4. 测试商品数据需根据实际业务自行添加

-- =============================================================
-- 系统管理服务数据库表结构 (stratos_system)
-- 职责：管理后台RBAC权限、菜单、操作日志、字典配置
-- =============================================================

USE `stratos_system`;

-- 管理员表
DROP TABLE IF EXISTS `sys_admin`;
CREATE TABLE `sys_admin` (
    `id` BIGINT UNSIGNED NOT NULL COMMENT '管理员ID（雪花算法）',
    `username` VARCHAR(32) NOT NULL COMMENT '用户名',
    `password` VARCHAR(128) NOT NULL COMMENT '密码（Bcrypt）',
    `real_name` VARCHAR(64) NOT NULL COMMENT '真实姓名',
    `nickname` VARCHAR(64) DEFAULT NULL COMMENT '昵称',
    `avatar` VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
    `phone_encrypted` VARCHAR(128) DEFAULT NULL COMMENT '手机号（加密）',
    `phone_hash` CHAR(64) DEFAULT NULL COMMENT '手机号哈希（用于查询）',
    `email` VARCHAR(128) DEFAULT NULL COMMENT '邮箱',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0=禁用，1=正常',
    `is_super_admin` TINYINT NOT NULL DEFAULT 0 COMMENT '是否超级管理员：0=否，1=是',
    `dept_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '部门ID',
    `last_login_time` DATETIME DEFAULT NULL COMMENT '最后登录时间',
    `last_login_ip` VARCHAR(45) DEFAULT NULL COMMENT '最后登录IP',
    `remark` VARCHAR(255) DEFAULT NULL COMMENT '备注',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by` VARCHAR(64) DEFAULT NULL COMMENT '创建人',
    `update_by` VARCHAR(64) DEFAULT NULL COMMENT '更新人',
    `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0=未删除，1=已删除',
    `version` INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    UNIQUE KEY `uk_phone_hash` (`phone_hash`),
    KEY `idx_status` (`status`),
    KEY `idx_dept_id` (`dept_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='管理员表';

-- 角色表
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role` (
    `id` BIGINT UNSIGNED NOT NULL COMMENT '角色ID（雪花算法）',
    `role_name` VARCHAR(64) NOT NULL COMMENT '角色名称',
    `role_code` VARCHAR(32) NOT NULL COMMENT '角色编码',
    `data_scope` TINYINT NOT NULL DEFAULT 1 COMMENT '数据权限范围：1=全部，2=自定义，3=本部门，4=本部门及以下，5=仅本人',
    `dept_ids` VARCHAR(512) DEFAULT NULL COMMENT '自定义部门权限（逗号分隔）',
    `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0=禁用，1=正常',
    `remark` VARCHAR(255) DEFAULT NULL COMMENT '备注',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by` VARCHAR(64) DEFAULT NULL COMMENT '创建人',
    `update_by` VARCHAR(64) DEFAULT NULL COMMENT '更新人',
    `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0=未删除，1=已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_role_code` (`role_code`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='角色表';

-- 管理员角色关联表
DROP TABLE IF EXISTS `sys_admin_role`;
CREATE TABLE `sys_admin_role` (
    `id` BIGINT UNSIGNED NOT NULL COMMENT '关联ID（雪花算法）',
    `admin_id` BIGINT UNSIGNED NOT NULL COMMENT '管理员ID',
    `role_id` BIGINT UNSIGNED NOT NULL COMMENT '角色ID',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_admin_role` (`admin_id`, `role_id`),
    KEY `idx_role_id` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='管理员角色关联表';

-- 菜单表（权限资源）
DROP TABLE IF EXISTS `sys_menu`;
CREATE TABLE `sys_menu` (
    `id` BIGINT UNSIGNED NOT NULL COMMENT '菜单ID（雪花算法）',
    `parent_id` BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '父菜单ID（0=顶级）',
    `menu_name` VARCHAR(64) NOT NULL COMMENT '菜单名称',
    `menu_type` TINYINT NOT NULL COMMENT '菜单类型：1=目录，2=菜单，3=按钮',
    `path` VARCHAR(255) DEFAULT NULL COMMENT '路由路径',
    `component` VARCHAR(255) DEFAULT NULL COMMENT '组件路径',
    `permission` VARCHAR(128) DEFAULT NULL COMMENT '权限标识（如：user:add）',
    `icon` VARCHAR(64) DEFAULT NULL COMMENT '图标',
    `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序',
    `visible` TINYINT NOT NULL DEFAULT 1 COMMENT '是否可见：0=隐藏，1=显示',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0=禁用，1=启用',
    `remark` VARCHAR(255) DEFAULT NULL COMMENT '备注',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by` VARCHAR(64) DEFAULT NULL COMMENT '创建人',
    `update_by` VARCHAR(64) DEFAULT NULL COMMENT '更新人',
    `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0=未删除，1=已删除',
    PRIMARY KEY (`id`),
    KEY `idx_parent_id` (`parent_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='菜单表';

-- 角色菜单关联表
DROP TABLE IF EXISTS `sys_role_menu`;
CREATE TABLE `sys_role_menu` (
    `id` BIGINT UNSIGNED NOT NULL COMMENT '关联ID（雪花算法）',
    `role_id` BIGINT UNSIGNED NOT NULL COMMENT '角色ID',
    `menu_id` BIGINT UNSIGNED NOT NULL COMMENT '菜单ID',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_role_menu` (`role_id`, `menu_id`),
    KEY `idx_menu_id` (`menu_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='角色菜单关联表';

-- 部门表
DROP TABLE IF EXISTS `sys_dept`;
CREATE TABLE `sys_dept` (
    `id` BIGINT UNSIGNED NOT NULL COMMENT '部门ID（雪花算法）',
    `parent_id` BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '父部门ID（0=顶级）',
    `ancestors` VARCHAR(512) DEFAULT NULL COMMENT '祖级列表（1,2,3）',
    `dept_name` VARCHAR(64) NOT NULL COMMENT '部门名称',
    `dept_code` VARCHAR(32) NOT NULL COMMENT '部门编码',
    `leader` VARCHAR(64) DEFAULT NULL COMMENT '负责人',
    `phone` VARCHAR(20) DEFAULT NULL COMMENT '联系电话',
    `email` VARCHAR(128) DEFAULT NULL COMMENT '邮箱',
    `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0=禁用，1=启用',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by` VARCHAR(64) DEFAULT NULL COMMENT '创建人',
    `update_by` VARCHAR(64) DEFAULT NULL COMMENT '更新人',
    `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0=未删除，1=已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_dept_code` (`dept_code`),
    KEY `idx_parent_id` (`parent_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='部门表';

-- 操作日志表
DROP TABLE IF EXISTS `sys_operation_log`;
CREATE TABLE `sys_operation_log` (
    `id` BIGINT UNSIGNED NOT NULL COMMENT '日志ID（雪花算法）',
    `module` VARCHAR(64) NOT NULL COMMENT '操作模块',
    `business_type` TINYINT NOT NULL COMMENT '业务类型：1=新增，2=修改，3=删除，4=查询，5=导出，6=导入，7=其他',
    `method` VARCHAR(255) NOT NULL COMMENT '方法名（类名.方法名）',
    `request_method` VARCHAR(16) NOT NULL COMMENT '请求方式（GET/POST）',
    `operator_type` TINYINT NOT NULL DEFAULT 1 COMMENT '操作类型：1=后台用户，2=前台用户',
    `operator_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '操作人ID',
    `operator_name` VARCHAR(64) DEFAULT NULL COMMENT '操作人姓名',
    `dept_name` VARCHAR(64) DEFAULT NULL COMMENT '部门名称',
    `request_url` VARCHAR(512) DEFAULT NULL COMMENT '请求URL',
    `request_ip` VARCHAR(45) DEFAULT NULL COMMENT '请求IP',
    `request_location` VARCHAR(255) DEFAULT NULL COMMENT '请求地点（IP解析）',
    `request_param` TEXT DEFAULT NULL COMMENT '请求参数（JSON，脱敏）',
    `response_result` TEXT DEFAULT NULL COMMENT '返回结果（JSON，截断）',
    `status` TINYINT NOT NULL COMMENT '状态：1=成功，0=失败',
    `error_msg` TEXT DEFAULT NULL COMMENT '错误消息',
    `cost_time` INT UNSIGNED DEFAULT 0 COMMENT '耗时（毫秒）',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_operator_id` (`operator_id`),
    KEY `idx_business_type` (`business_type`),
    KEY `idx_status` (`status`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='操作日志表';

-- 登录日志表
DROP TABLE IF EXISTS `sys_login_log`;
CREATE TABLE `sys_login_log` (
    `id` BIGINT UNSIGNED NOT NULL COMMENT '日志ID（雪花算法）',
    `username` VARCHAR(64) NOT NULL COMMENT '用户名',
    `login_ip` VARCHAR(45) NOT NULL COMMENT '登录IP',
    `login_location` VARCHAR(255) DEFAULT NULL COMMENT '登录地点',
    `browser` VARCHAR(64) DEFAULT NULL COMMENT '浏览器类型',
    `os` VARCHAR(64) DEFAULT NULL COMMENT '操作系统',
    `status` TINYINT NOT NULL COMMENT '登录状态：1=成功，0=失败',
    `msg` VARCHAR(255) DEFAULT NULL COMMENT '提示信息',
    `login_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '登录时间',
    PRIMARY KEY (`id`),
    KEY `idx_username` (`username`),
    KEY `idx_status` (`status`),
    KEY `idx_login_time` (`login_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='登录日志表';

-- 字典类型表
DROP TABLE IF EXISTS `sys_dict_type`;
CREATE TABLE `sys_dict_type` (
    `id` BIGINT UNSIGNED NOT NULL COMMENT '字典类型ID（雪花算法）',
    `dict_name` VARCHAR(64) NOT NULL COMMENT '字典名称',
    `dict_type` VARCHAR(64) NOT NULL COMMENT '字典类型编码',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0=禁用，1=启用',
    `remark` VARCHAR(255) DEFAULT NULL COMMENT '备注',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by` VARCHAR(64) DEFAULT NULL COMMENT '创建人',
    `update_by` VARCHAR(64) DEFAULT NULL COMMENT '更新人',
    `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0=未删除，1=已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_dict_type` (`dict_type`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='字典类型表';

-- 字典数据表
DROP TABLE IF EXISTS `sys_dict_data`;
CREATE TABLE `sys_dict_data` (
    `id` BIGINT UNSIGNED NOT NULL COMMENT '字典数据ID（雪花算法）',
    `dict_type` VARCHAR(64) NOT NULL COMMENT '字典类型编码',
    `dict_label` VARCHAR(128) NOT NULL COMMENT '字典标签',
    `dict_value` VARCHAR(128) NOT NULL COMMENT '字典键值',
    `dict_sort` INT NOT NULL DEFAULT 0 COMMENT '排序',
    `css_class` VARCHAR(64) DEFAULT NULL COMMENT '样式属性',
    `list_class` VARCHAR(64) DEFAULT 'default' COMMENT '列表样式',
    `is_default` TINYINT NOT NULL DEFAULT 0 COMMENT '是否默认：0=否，1=是',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0=禁用，1=启用',
    `remark` VARCHAR(255) DEFAULT NULL COMMENT '备注',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by` VARCHAR(64) DEFAULT NULL COMMENT '创建人',
    `update_by` VARCHAR(64) DEFAULT NULL COMMENT '更新人',
    `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0=未删除，1=已删除',
    PRIMARY KEY (`id`),
    KEY `idx_dict_type` (`dict_type`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='字典数据表';

-- 系统配置表
DROP TABLE IF EXISTS `sys_config`;
CREATE TABLE `sys_config` (
    `id` BIGINT UNSIGNED NOT NULL COMMENT '配置ID（雪花算法）',
    `config_name` VARCHAR(128) NOT NULL COMMENT '配置名称',
    `config_key` VARCHAR(128) NOT NULL COMMENT '配置键',
    `config_value` TEXT NOT NULL COMMENT '配置值',
    `config_type` TINYINT NOT NULL DEFAULT 0 COMMENT '配置类型：0=系统内置，1=业务配置',
    `remark` VARCHAR(255) DEFAULT NULL COMMENT '备注',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by` VARCHAR(64) DEFAULT NULL COMMENT '创建人',
    `update_by` VARCHAR(64) DEFAULT NULL COMMENT '更新人',
    `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0=未删除，1=已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_config_key` (`config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统配置表';

-- 首页轮播
DROP TABLE IF EXISTS `cms_banner`;
CREATE TABLE `cms_banner` (
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

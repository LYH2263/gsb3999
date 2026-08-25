-- Database: `agri_trace`
-- 适用于 Navicat 导入，或者作为 docker-compose mysql 容器的 init script
-- 满足用户要求的 utf8mb4 字符集支持中文和表情符号
-- 北京时间通常在应用层面或 MySQL server timezone 中控制
-- 这里为各时间戳设置为 MySQL 级别的 DEFAULT 和 ON UPDATE 控制
-- 包含：用户（多角色）、商品信息、物流信息、溯源码、热门产品核心数据

CREATE DATABASE IF NOT EXISTS `agri_trace` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `agri_trace`;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;


-- ----------------------------
-- 1. Table structure for sys_user : 系统用户表
-- 包含所有的四种角色：普通用户、农户、物流管理员、系统管理员
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `username` VARCHAR(50) NOT NULL COMMENT '登录账号(唯一)',
  `password` VARCHAR(255) NOT NULL COMMENT '登录密码(BCrypt加密)',
  `role` VARCHAR(20) NOT NULL COMMENT '角色标识: USER(普通用户), FARMER(农户), LOGS_ADMIN(物流管理), SYS_ADMIN(系统管理)',
  `enabled` TINYINT NOT NULL DEFAULT 1 COMMENT '账号状态: 1-启用, 0-禁用',
  `real_name` VARCHAR(50) DEFAULT NULL COMMENT '真实姓名 / 企业名称',
  `phone` VARCHAR(20) DEFAULT NULL COMMENT '联系电话号码',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_username`(`username`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统用户表(含多角色)';


-- ----------------------------
-- 2. Table structure for product : 农产品信息表
-- ----------------------------
DROP TABLE IF EXISTS `product`;
CREATE TABLE `product` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `farmer_id` BIGINT NOT NULL COMMENT '农户ID (关联sys_user)',
  `product_name` VARCHAR(100) NOT NULL COMMENT '农产品名称',
  `category` VARCHAR(50) NOT NULL COMMENT '类别(如: 水果, 蔬菜, 粮食)',
  `origin` VARCHAR(255) NOT NULL COMMENT '出产地详细地址',
  `description` TEXT COMMENT '产品描述和介绍',
  `harvest_date` DATE COMMENT '采摘日期/生产日期',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '添加时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最新修改时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_farmer_id`(`farmer_id`) USING BTREE,
  CONSTRAINT `fk_product_farmer` FOREIGN KEY (`farmer_id`) REFERENCES `sys_user` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='农产品基础信息表';


-- ----------------------------
-- 3. Table structure for tracing_code : 溯源码记录表
-- 这实现了追踪系统“一物一码”或“一批一码”的核心业务
-- ----------------------------
DROP TABLE IF EXISTS `tracing_code`;
CREATE TABLE `tracing_code` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `product_id` BIGINT NOT NULL COMMENT '关联商品ID',
  `trace_code` VARCHAR(64) NOT NULL COMMENT '生成的系统唯一溯源码(32/64位)',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 1-正常有效, 0-作废撤回',
  `generated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '溯源码生成时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_trace_code`(`trace_code`) USING BTREE,
  INDEX `idx_tracing_product`(`product_id`) USING BTREE,
  CONSTRAINT `fk_tracing_product` FOREIGN KEY (`product_id`) REFERENCES `product` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品唯一溯源码映射表';


-- ----------------------------
-- 4. Table structure for logistics : 物流节点信息表
-- 包含物流管理员登记的一条溯源码的多次位置打卡记录
-- ----------------------------
DROP TABLE IF EXISTS `logistics`;
CREATE TABLE `logistics` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `trace_code_id` BIGINT NOT NULL COMMENT '溯源码ID (外键)',
  `logistics_admin_id` BIGINT NOT NULL COMMENT '操作这条物流状态的物流管理员ID (外键)',
  `location` VARCHAR(255) NOT NULL COMMENT '当前物流物理位置',
  `status_desc` VARCHAR(100) NOT NULL COMMENT '物理状态节点(揽收、运输中、派件中、已签收)',
  `recorded_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '录入发生时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_logistics_code`(`trace_code_id`) USING BTREE,
  INDEX `idx_logistics_admin`(`logistics_admin_id`) USING BTREE,
  CONSTRAINT `fk_logistics_code` FOREIGN KEY (`trace_code_id`) REFERENCES `tracing_code` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_logistics_admin` FOREIGN KEY (`logistics_admin_id`) REFERENCES `sys_user` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='溯源物流流转信息日志表';


-- ----------------------------
-- 5. Table structure for hot_product : 热门溯源产品表
-- 提取查询维度以便首页渲染提速 (满足第三范式的补充表 / 或做一定反范式容忍来统计)
-- ----------------------------
DROP TABLE IF EXISTS `hot_product`;
CREATE TABLE `hot_product` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `product_id` BIGINT NOT NULL COMMENT '农产品ID (外键)',
  `search_count` INT NOT NULL DEFAULT 0 COMMENT '页面被溯源查询的统计次数',
  `is_display` TINYINT NOT NULL DEFAULT 1 COMMENT '是否强制上榜首页: 1-展示, 0-下架',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '计数最新更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_hot_product_id`(`product_id`) USING BTREE,
  CONSTRAINT `fk_hot_product` FOREIGN KEY (`product_id`) REFERENCES `product` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='热门溯源农用产品推荐表';


-- ----------------------------
-- Records Seeding: 初始测试数据 (符合 "0 Mock 数据, 拒绝空库交付" 的规范)
-- 密码均为 123456 的 BCrypt 哈希值: $2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2
-- ----------------------------

-- 用户
INSERT INTO `sys_user` (`id`, `username`, `password`, `role`, `enabled`, `real_name`, `phone`) VALUES 
(1, 'admin', '$2a$10$1vXJcoktze.A7roChmLs0eaSukwNhT7B0w6hQY.EYTc6NVn0sIRx2', 'SYS_ADMIN', 1, '系统超级权限员', '13800000000'),
(2, 'farmer_wang', '$2a$10$1vXJcoktze.A7roChmLs0eaSukwNhT7B0w6hQY.EYTc6NVn0sIRx2', 'FARMER', 1, '王大拿农场', '13800000001'),
(3, 'sf_admin', '$2a$10$1vXJcoktze.A7roChmLs0eaSukwNhT7B0w6hQY.EYTc6NVn0sIRx2', 'LOGS_ADMIN', 1, '顺丰华南分拨中心操作员', '13800000002'),
(4, 'consumer1', '$2a$10$1vXJcoktze.A7roChmLs0eaSukwNhT7B0w6hQY.EYTc6NVn0sIRx2', 'USER', 1, '购买用户张三', '13800000003');

-- 假装农户发布了产品
INSERT INTO `product` (`id`, `farmer_id`, `product_name`, `category`, `origin`, `description`, `harvest_date`) VALUES
(1, 2, '阿克苏冰糖心苹果', '水果', '新疆阿克苏地区红旗坡农场', '高原雪水灌溉，果核透明，甜度极高', '2023-10-15'),
(2, 2, '东北五常有机稻花香', '粮食', '黑龙江省哈尔滨市五常市', '手工插秧，人工除草，零化肥无残留', '2023-09-20');

-- 生成了溯源码
INSERT INTO `tracing_code` (`id`, `product_id`, `trace_code`, `status`) VALUES
(1, 1, 'TRC-AKAPPLE-231015-0001A', 1),
(2, 2, 'TRC-WCRICE-230920-0099Z', 1);

-- 物流流转信息录入
INSERT INTO `logistics` (`id`, `trace_code_id`, `logistics_admin_id`, `location`, `status_desc`, `recorded_at`) VALUES
(1, 1, 3, '新疆阿克苏红旗坡揽收点', '已揽收', '2023-11-01 10:00:00'),
(2, 1, 3, '西安陆运分拨中心', '长途运输中', '2023-11-03 14:30:00'),
(3, 2, 3, '哈尔滨集散中心', '已揽收', '2023-10-15 08:00:00');

-- 成为热门推荐
INSERT INTO `hot_product` (`id`, `product_id`, `search_count`, `is_display`) VALUES
(1, 1, 15302, 1),
(2, 2, 5840, 1);

SET FOREIGN_KEY_CHECKS = 1;

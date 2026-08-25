-- ----------------------------
-- 增量升级脚本：新增「农事档案」模块所需的 farming_record 表
-- 适用于已存在 agri_trace 库、无需重建的环境
-- 执行：mysql -uroot -p agri_trace < backend/sql/upgrade_20260825_farming_record.sql
-- ----------------------------
USE `agri_trace`;
SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS `farming_record` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `product_id` BIGINT NOT NULL COMMENT '关联农产品ID (外键)',
  `farmer_id` BIGINT NOT NULL COMMENT '登记该记录的农户ID (外键)',
  `operation_type` VARCHAR(20) NOT NULL COMMENT '操作类型: SOWING(播种), FERTILIZING(施肥), PESTICIDE(用药)',
  `operation_date` DATE NOT NULL COMMENT '操作日期',
  `drug_name` VARCHAR(100) DEFAULT NULL COMMENT '用药名称(仅用药记录)',
  `safety_interval_days` INT DEFAULT NULL COMMENT '安全间隔天数(仅用药记录)',
  `remark` VARCHAR(255) DEFAULT NULL COMMENT '备注说明',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '登记时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_farming_product`(`product_id`) USING BTREE,
  INDEX `idx_farming_farmer`(`farmer_id`) USING BTREE,
  CONSTRAINT `fk_farming_product` FOREIGN KEY (`product_id`) REFERENCES `product` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_farming_farmer` FOREIGN KEY (`farmer_id`) REFERENCES `sys_user` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='农事档案记录表(播种/施肥/用药)';

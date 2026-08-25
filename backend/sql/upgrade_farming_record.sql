-- 农事档案模块升级脚本
-- 适用于已存在 agri_trace 数据库的环境

USE `agri_trace`;

-- ----------------------------
-- Table structure for farming_record : 农事档案记录表
-- 记录农户对其产品的播种、施肥、用药等农事操作
-- ----------------------------
DROP TABLE IF EXISTS `farming_record`;
CREATE TABLE `farming_record` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `product_id` BIGINT NOT NULL COMMENT '关联农产品ID (外键 product.id)',
  `farmer_id` BIGINT NOT NULL COMMENT '农户ID (外键 sys_user.id, 冗余便于权限校验)',
  `operation_type` VARCHAR(20) NOT NULL COMMENT '操作类型: SOWING-播种, FERTILIZING-施肥, PESTICIDE-用药',
  `operation_date` DATE NOT NULL COMMENT '操作日期',
  `pesticide_name` VARCHAR(100) DEFAULT NULL COMMENT '用药名称 (operation_type=PESTICIDE 时必填)',
  `safety_interval_days` INT DEFAULT NULL COMMENT '安全间隔天数 (operation_type=PESTICIDE 时必填)',
  `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_farming_product`(`product_id`) USING BTREE,
  INDEX `idx_farming_farmer`(`farmer_id`) USING BTREE,
  INDEX `idx_farming_type`(`operation_type`) USING BTREE,
  CONSTRAINT `fk_farming_product` FOREIGN KEY (`product_id`) REFERENCES `product` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_farming_farmer` FOREIGN KEY (`farmer_id`) REFERENCES `sys_user` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='农事档案记录表(播种/施肥/用药)';

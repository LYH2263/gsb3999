USE `agri_trace`;

-- 农事档案记录表：播种/施肥/用药；用药安全间隔天数用于阻断间隔期内生成溯源码
CREATE TABLE IF NOT EXISTS `farming_record` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `product_id` BIGINT NOT NULL COMMENT '关联农产品ID',
  `farmer_id` BIGINT NOT NULL COMMENT '登记农户ID (关联sys_user)',
  `operation_type` VARCHAR(20) NOT NULL COMMENT '操作类型: SOWING(播种), FERTILIZING(施肥), PESTICIDE(用药)',
  `operation_date` DATE NOT NULL COMMENT '操作日期',
  `drug_name` VARCHAR(100) DEFAULT NULL COMMENT '用药名称(仅用药记录)',
  `safety_interval_days` INT DEFAULT NULL COMMENT '安全间隔天数(仅用药记录)',
  `remark` VARCHAR(255) DEFAULT NULL COMMENT '备注',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '登记时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_farming_product`(`product_id`) USING BTREE,
  INDEX `idx_farming_farmer`(`farmer_id`) USING BTREE,
  CONSTRAINT `fk_farming_product` FOREIGN KEY (`product_id`) REFERENCES `product` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_farming_farmer` FOREIGN KEY (`farmer_id`) REFERENCES `sys_user` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='农事档案记录表(播种/施肥/用药)';

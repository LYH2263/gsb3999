USE `agri_trace`;

-- 农事档案模块：农事档案记录表
-- 农户为其农产品登记播种、施肥、用药记录；消费者溯源时只读查看
CREATE TABLE IF NOT EXISTS `farming_record` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `product_id` BIGINT NOT NULL COMMENT '关联农产品ID (外键 product.id)',
  `farmer_id` BIGINT NOT NULL COMMENT '录入农户ID (外键 sys_user.id)',
  `operation_type` VARCHAR(20) NOT NULL COMMENT '操作类型: SOWING-播种, FERTILIZING-施肥, PESTICIDE-用药',
  `operation_date` DATE NOT NULL COMMENT '操作日期',
  `material_name` VARCHAR(100) DEFAULT NULL COMMENT '用药/肥料名称(播种为空)',
  `safety_interval_days` INT DEFAULT NULL COMMENT '安全间隔天数(仅用药记录填写)',
  `remark` VARCHAR(255) DEFAULT NULL COMMENT '备注',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '录入时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_farming_product`(`product_id`) USING BTREE,
  INDEX `idx_farming_farmer`(`farmer_id`) USING BTREE,
  CONSTRAINT `fk_farming_product` FOREIGN KEY (`product_id`) REFERENCES `product` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_farming_farmer` FOREIGN KEY (`farmer_id`) REFERENCES `sys_user` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='农事档案记录表';

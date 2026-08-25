USE `agri_trace`;

ALTER TABLE `sys_user`
ADD COLUMN IF NOT EXISTS `enabled` TINYINT NOT NULL DEFAULT 1 COMMENT '账号状态: 1-启用, 0-禁用' AFTER `role`;

UPDATE `sys_user`
SET `enabled` = 1
WHERE `enabled` IS NULL;

package com.agritrace.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class DatabaseCompatibilityInitializer implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(DatabaseCompatibilityInitializer.class);

    private final JdbcTemplate jdbcTemplate;

    public DatabaseCompatibilityInitializer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!tableExists("sys_user")) {
            log.warn("Table `sys_user` not found, skip compatibility migration.");
            return;
        }

        if (!columnExists("sys_user", "enabled")) {
            jdbcTemplate.execute(
                    "ALTER TABLE `sys_user` " +
                    "ADD COLUMN `enabled` TINYINT NOT NULL DEFAULT 1 COMMENT '账号状态: 1-启用, 0-禁用' AFTER `role`"
            );
            log.info("Added missing column `sys_user.enabled` for backward compatibility.");
        }

        int updatedRows = jdbcTemplate.update(
                "UPDATE `sys_user` SET `enabled` = 1 WHERE `enabled` IS NULL"
        );
        if (updatedRows > 0) {
            log.info("Initialized {} rows with NULL `sys_user.enabled` to 1.", updatedRows);
        }
    }

    private boolean tableExists(String tableName) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.TABLES WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ?",
                Integer.class,
                tableName
        );
        return count != null && count > 0;
    }

    private boolean columnExists(String tableName, String columnName) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.COLUMNS " +
                "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ? AND COLUMN_NAME = ?",
                Integer.class,
                tableName,
                columnName
        );
        return count != null && count > 0;
    }
}

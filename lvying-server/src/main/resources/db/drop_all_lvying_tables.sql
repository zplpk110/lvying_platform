-- =============================================================================
-- 仅开发/迁移时使用：删除旅盈全部业务表（按外键依赖逆序），数据会丢失，请先备份。
-- 执行后重启应用：在 spring.sql.init.mode=always 下会再次运行 schema.sql 创建 UUID(CHAR(36)) 表。
-- 在 MySQL 客户端中：SOURCE .../drop_all_lvying_tables.sql; 或粘贴执行。
-- =============================================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS collection_reminders;
DROP TABLE IF EXISTS expenses;
DROP TABLE IF EXISTS incomes;
DROP TABLE IF EXISTS tour_guests;
DROP TABLE IF EXISTS tours;
DROP TABLE IF EXISTS users;

SET FOREIGN_KEY_CHECKS = 1;

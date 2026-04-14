-- =============================================================================
-- 旅盈数据库：主键与外键 ID 统一为 UUID
-- -----------------------------------------------------------------------------
-- 存储类型：CHAR(32)，32 位十六进制、无连字符（与 Java UUID.toString() 去掉 '-' 一致）。
-- 应用侧：java.util.UUID；落库与 JSON、JWT subject 均为紧凑格式。
-- 读取时仍兼容旧数据中带连字符的 CHAR(36)（见 UuidCharTypeHandler / UuidStrings）。
-- 若需从 CHAR(36) 迁到 CHAR(32)：备份后执行 db/drop_all_lvying_tables.sql 再启动重建，或自行 ALTER。
-- =============================================================================

CREATE TABLE IF NOT EXISTS users (
  id CHAR(32) NOT NULL PRIMARY KEY COMMENT 'UUID 主键 32hex',
  phone VARCHAR(32) NOT NULL UNIQUE,
  password_hash VARCHAR(120) NOT NULL,
  name VARCHAR(64) NOT NULL,
  role VARCHAR(32) NOT NULL,
  bank_name VARCHAR(64) NULL,
  bank_account_last4 VARCHAR(8) NULL,
  created_at DATETIME(3) NOT NULL,
  updated_at DATETIME(3) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS tours (
  id CHAR(32) NOT NULL PRIMARY KEY COMMENT 'UUID 主键 32hex',
  tour_code VARCHAR(32) NOT NULL UNIQUE,
  name VARCHAR(128) NOT NULL,
  status VARCHAR(32) NOT NULL,
  departure_date DATE NOT NULL,
  guest_count INT NOT NULL DEFAULT 0,
  price_per_guest DECIMAL(14,2) NOT NULL DEFAULT 0,
  budget_redline DECIMAL(14,2) NOT NULL DEFAULT 0,
  gross_margin_pct DECIMAL(6,2) NOT NULL DEFAULT 25,
  sales_user_id CHAR(32) NULL COMMENT 'UUID 外键 -> users.id',
  commission_rate DECIMAL(6,2) NOT NULL DEFAULT 15,
  created_at DATETIME(3) NOT NULL,
  updated_at DATETIME(3) NOT NULL,
  CONSTRAINT fk_tour_sales FOREIGN KEY (sales_user_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS tour_guests (
  id CHAR(32) NOT NULL PRIMARY KEY COMMENT 'UUID 主键 32hex',
  tour_id CHAR(32) NOT NULL COMMENT 'UUID 外键 -> tours.id',
  name VARCHAR(64) NOT NULL,
  phone_masked VARCHAR(32) NOT NULL,
  phone_raw VARCHAR(32) NULL,
  balance_due DECIMAL(14,2) NOT NULL DEFAULT 0,
  created_at DATETIME(3) NOT NULL,
  CONSTRAINT fk_guest_tour FOREIGN KEY (tour_id) REFERENCES tours (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS incomes (
  id CHAR(32) NOT NULL PRIMARY KEY COMMENT 'UUID 主键 32hex',
  tour_id CHAR(32) NOT NULL COMMENT 'UUID 外键 -> tours.id',
  amount DECIMAL(14,2) NOT NULL,
  type VARCHAR(16) NOT NULL,
  note VARCHAR(255) NULL,
  received_at DATETIME(3) NOT NULL,
  created_at DATETIME(3) NOT NULL,
  CONSTRAINT fk_income_tour FOREIGN KEY (tour_id) REFERENCES tours (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS expenses (
  id CHAR(32) NOT NULL PRIMARY KEY COMMENT 'UUID 主键 32hex',
  tour_id CHAR(32) NOT NULL COMMENT 'UUID 外键 -> tours.id',
  amount DECIMAL(14,2) NOT NULL,
  category VARCHAR(32) NOT NULL,
  payment_method VARCHAR(32) NOT NULL,
  staff_user_id CHAR(32) NULL COMMENT 'UUID 外键 -> users.id',
  receipt_image_url VARCHAR(512) NULL,
  note VARCHAR(512) NULL,
  approval_status VARCHAR(32) NOT NULL,
  pay_status VARCHAR(16) NOT NULL,
  approved_by_id CHAR(32) NULL COMMENT 'UUID 外键 -> users.id',
  approved_at DATETIME(3) NULL,
  batch_pay_ref VARCHAR(64) NULL,
  created_at DATETIME(3) NOT NULL,
  updated_at DATETIME(3) NOT NULL,
  CONSTRAINT fk_expense_tour FOREIGN KEY (tour_id) REFERENCES tours (id) ON DELETE CASCADE,
  CONSTRAINT fk_expense_staff FOREIGN KEY (staff_user_id) REFERENCES users (id),
  CONSTRAINT fk_expense_approver FOREIGN KEY (approved_by_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS collection_reminders (
  id CHAR(32) NOT NULL PRIMARY KEY COMMENT 'UUID 主键 32hex',
  tour_id CHAR(32) NOT NULL COMMENT 'UUID 外键 -> tours.id',
  channel VARCHAR(32) NOT NULL,
  payload VARCHAR(1024) NOT NULL,
  sent_at DATETIME(3) NOT NULL,
  CONSTRAINT fk_remind_tour FOREIGN KEY (tour_id) REFERENCES tours (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

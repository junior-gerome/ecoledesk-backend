CREATE TABLE IF NOT EXISTS users (
  id BIGINT NOT NULL AUTO_INCREMENT,
  username VARCHAR(50) NOT NULL,
  password VARCHAR(255) NOT NULL,
  last_login DATETIME(6) NULL,
  reset_token VARCHAR(255) NULL,
  actif BOOLEAN NOT NULL DEFAULT TRUE,
  created_at DATETIME(6) NULL,
  updated_at DATETIME(6) NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_users_username (username)
);

SET @column_exists = (
  SELECT COUNT(*)
  FROM information_schema.columns
  WHERE table_schema = DATABASE()
    AND table_name = 'users'
    AND column_name = 'reset_token_expires_at'
);
SET @ddl = IF(@column_exists = 0,
  'ALTER TABLE users ADD COLUMN reset_token_expires_at DATETIME(6) NULL',
  'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

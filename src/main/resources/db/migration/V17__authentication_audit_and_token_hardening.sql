CREATE TABLE IF NOT EXISTS authentication_audit_events (
  id BIGINT NOT NULL AUTO_INCREMENT,
  user_id BIGINT NULL,
  username VARCHAR(100) NULL,
  event_type VARCHAR(50) NOT NULL,
  successful BOOLEAN NOT NULL,
  reason VARCHAR(255) NULL,
  client_ip VARCHAR(45) NULL,
  created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  PRIMARY KEY (id),
  KEY idx_auth_audit_username (username),
  KEY idx_auth_audit_user_id (user_id),
  KEY idx_auth_audit_event_type (event_type),
  KEY idx_auth_audit_created_at (created_at)
);

SET @column_exists = (
  SELECT COUNT(*)
  FROM information_schema.columns
  WHERE table_schema = DATABASE()
    AND table_name = 'users'
    AND column_name = 'reset_token'
);
SET @ddl = IF(@column_exists = 1,
  'ALTER TABLE users MODIFY reset_token VARCHAR(64) NULL',
  'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @index_exists = (
  SELECT COUNT(*)
  FROM information_schema.statistics
  WHERE table_schema = DATABASE()
    AND table_name = 'refresh_tokens'
    AND index_name = 'idx_refresh_tokens_user_active'
);
SET @ddl = IF(@index_exists = 0,
  'CREATE INDEX idx_refresh_tokens_user_active ON refresh_tokens (user_id, revoked_at, expires_at)',
  'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

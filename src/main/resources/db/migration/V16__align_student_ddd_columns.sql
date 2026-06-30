SET @column_exists = (
  SELECT COUNT(*)
  FROM information_schema.columns
  WHERE table_schema = DATABASE()
    AND table_name = 'students'
    AND column_name = 'current_level'
);
SET @ddl = IF(@column_exists = 0,
  'ALTER TABLE students ADD COLUMN current_level VARCHAR(50) NULL',
  'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @column_exists = (
  SELECT COUNT(*)
  FROM information_schema.columns
  WHERE table_schema = DATABASE()
    AND table_name = 'students'
    AND column_name = 'ecole_precedente'
);
SET @ddl = IF(@column_exists = 0,
  'ALTER TABLE students ADD COLUMN ecole_precedente VARCHAR(100) NULL',
  'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

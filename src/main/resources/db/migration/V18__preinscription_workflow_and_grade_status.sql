SET @column_exists = (
  SELECT COUNT(*)
  FROM information_schema.columns
  WHERE table_schema = DATABASE()
    AND table_name = 'inscription_student'
    AND column_name = 'preinscription_decision_reason'
);
SET @ddl = IF(@column_exists = 0,
  'ALTER TABLE inscription_student ADD COLUMN preinscription_decision_reason VARCHAR(500) NULL',
  'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @column_exists = (
  SELECT COUNT(*)
  FROM information_schema.columns
  WHERE table_schema = DATABASE()
    AND table_name = 'inscription_student'
    AND column_name = 'preinscription_decision_by'
);
SET @ddl = IF(@column_exists = 0,
  'ALTER TABLE inscription_student ADD COLUMN preinscription_decision_by BIGINT NULL',
  'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @column_exists = (
  SELECT COUNT(*)
  FROM information_schema.columns
  WHERE table_schema = DATABASE()
    AND table_name = 'inscription_student'
    AND column_name = 'preinscription_decision_at'
);
SET @ddl = IF(@column_exists = 0,
  'ALTER TABLE inscription_student ADD COLUMN preinscription_decision_at DATETIME(6) NULL',
  'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

UPDATE inscription_student
SET statut_preinscription = 'VALIDEE'
WHERE statut_preinscription = 'INSCRITE';

SET @column_exists = (
  SELECT COUNT(*)
  FROM information_schema.columns
  WHERE table_schema = DATABASE()
    AND table_name = 'academic_grade'
    AND column_name = 'status'
);
SET @ddl = IF(@column_exists = 0,
  'ALTER TABLE academic_grade ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT ''DRAFT''',
  'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @index_exists = (
  SELECT COUNT(*)
  FROM information_schema.statistics
  WHERE table_schema = DATABASE()
    AND table_name = 'academic_grade'
    AND index_name = 'idx_grade_status'
);
SET @ddl = IF(@index_exists = 0,
  'CREATE INDEX idx_grade_status ON academic_grade (status)',
  'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

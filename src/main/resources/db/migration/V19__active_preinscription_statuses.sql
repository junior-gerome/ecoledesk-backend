SET @index_exists = (
  SELECT COUNT(*)
  FROM information_schema.statistics
  WHERE table_schema = DATABASE()
    AND table_name = 'inscription_student'
    AND index_name = 'uk_enrollment_student_year_active'
);
SET @ddl = IF(@index_exists > 0,
  'ALTER TABLE inscription_student DROP INDEX uk_enrollment_student_year_active',
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
    AND column_name = 'active_enrollment_key'
);
SET @ddl = IF(@column_exists > 0,
  'ALTER TABLE inscription_student DROP COLUMN active_enrollment_key',
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
    AND column_name = 'statut_preinscription'
);
SET @ddl = IF(@column_exists > 0,
  'ALTER TABLE inscription_student MODIFY COLUMN statut_preinscription VARCHAR(30) NOT NULL DEFAULT ''VALIDEE''',
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
    AND column_name = 'active_enrollment_key'
);
SET @ddl = IF(@column_exists = 0,
  'ALTER TABLE inscription_student ADD COLUMN active_enrollment_key TINYINT GENERATED ALWAYS AS (CASE WHEN statut_preinscription IN (''BROUILLON'', ''EN_ATTENTE'', ''VALIDEE'', ''INSCRITE'') THEN 1 ELSE NULL END) STORED',
  'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @index_exists = (
  SELECT COUNT(*)
  FROM information_schema.statistics
  WHERE table_schema = DATABASE()
    AND table_name = 'inscription_student'
    AND index_name = 'uk_enrollment_student_year_active'
);
SET @ddl = IF(@index_exists = 0,
  'CREATE UNIQUE INDEX uk_enrollment_student_year_active ON inscription_student (student_id, anneescolaire_id, active_enrollment_key)',
  'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
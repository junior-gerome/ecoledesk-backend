SET @column_exists = (
  SELECT COUNT(*)
  FROM information_schema.columns
  WHERE table_schema = DATABASE()
    AND table_name = 'paiements'
    AND column_name = 'cancelled_at'
);
SET @ddl = IF(@column_exists = 0,
  'ALTER TABLE paiements ADD COLUMN cancelled_at DATETIME NULL',
  'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @column_exists = (
  SELECT COUNT(*)
  FROM information_schema.columns
  WHERE table_schema = DATABASE()
    AND table_name = 'paiements'
    AND column_name = 'cancellation_reason'
);
SET @ddl = IF(@column_exists = 0,
  'ALTER TABLE paiements ADD COLUMN cancellation_reason VARCHAR(500) NULL',
  'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @column_exists = (
  SELECT COUNT(*)
  FROM information_schema.columns
  WHERE table_schema = DATABASE()
    AND table_name = 'annee_scolaire'
    AND column_name = 'active_key'
);
SET @ddl = IF(@column_exists = 0,
  'ALTER TABLE annee_scolaire ADD COLUMN active_key TINYINT GENERATED ALWAYS AS (CASE WHEN statut_code = 1 THEN 1 ELSE NULL END) STORED',
  'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @index_exists = (
  SELECT COUNT(*)
  FROM information_schema.statistics
  WHERE table_schema = DATABASE()
    AND table_name = 'annee_scolaire'
    AND index_name = 'uk_one_active_school_year'
);
SET @ddl = IF(@index_exists = 0,
  'CREATE UNIQUE INDEX uk_one_active_school_year ON annee_scolaire (active_key)',
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
  'ALTER TABLE inscription_student ADD COLUMN active_enrollment_key TINYINT GENERATED ALWAYS AS (CASE WHEN statut_preinscription <> ''ANNULEE'' THEN 1 ELSE NULL END) STORED',
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

SET @index_exists = (
  SELECT COUNT(*)
  FROM information_schema.statistics
  WHERE table_schema = DATABASE()
    AND table_name = 'affectation'
    AND index_name = 'idx_affectation_teacher_year'
);
SET @ddl = IF(@index_exists = 0,
  'CREATE INDEX idx_affectation_teacher_year ON affectation (teacher_id, annee_scolaire_id)',
  'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @index_exists = (
  SELECT COUNT(*)
  FROM information_schema.statistics
  WHERE table_schema = DATABASE()
    AND table_name = 'affectation'
    AND index_name = 'idx_affectation_class_year'
);
SET @ddl = IF(@index_exists = 0,
  'CREATE INDEX idx_affectation_class_year ON affectation (classe_id, annee_scolaire_id)',
  'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
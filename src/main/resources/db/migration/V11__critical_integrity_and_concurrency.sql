SET @column_exists = (
  SELECT COUNT(*)
  FROM information_schema.columns
  WHERE table_schema = DATABASE()
    AND table_name = 'paiements'
    AND column_name = 'version'
);
SET @ddl = IF(@column_exists = 0,
  'ALTER TABLE paiements ADD COLUMN version BIGINT NOT NULL DEFAULT 0',
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
    AND column_name = 'version'
);
SET @ddl = IF(@column_exists = 0,
  'ALTER TABLE inscription_student ADD COLUMN version BIGINT NOT NULL DEFAULT 0',
  'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @index_exists = (
  SELECT COUNT(*)
  FROM information_schema.statistics
  WHERE table_schema = DATABASE()
    AND table_name = 'paiements'
    AND index_name = 'uk_paiements_receipt_number'
);
SET @ddl = IF(@index_exists = 0,
  'CREATE UNIQUE INDEX uk_paiements_receipt_number ON paiements (receipt_number)',
  'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @index_exists = (
  SELECT COUNT(*)
  FROM information_schema.statistics
  WHERE table_schema = DATABASE()
    AND table_name = 'montant'
    AND index_name = 'uk_montant_class_type'
);
SET @ddl = IF(@index_exists = 0,
  'CREATE UNIQUE INDEX uk_montant_class_type ON montant (classe_room_id, type_paiement)',
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
    AND index_name = 'uk_affectation_teacher_class_subject_year'
);
SET @ddl = IF(@index_exists = 0,
  'CREATE UNIQUE INDEX uk_affectation_teacher_class_subject_year ON affectation (teacher_id, classe_id, subject_id, annee_scolaire_id)',
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
    AND index_name = 'uk_grade_student_subject_sequence_class'
);
SET @ddl = IF(@index_exists = 0,
  'CREATE UNIQUE INDEX uk_grade_student_subject_sequence_class ON academic_grade (student_id, subject_id, sequence_id, classe_id)',
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
    AND index_name = 'idx_grade_class_period'
);
SET @ddl = IF(@index_exists = 0,
  'CREATE INDEX idx_grade_class_period ON academic_grade (classe_id, period(191))',
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
    AND index_name = 'idx_grade_student_period'
);
SET @ddl = IF(@index_exists = 0,
  'CREATE INDEX idx_grade_student_period ON academic_grade (student_id, period(191))',
  'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @index_exists = (
  SELECT COUNT(*)
  FROM information_schema.statistics
  WHERE table_schema = DATABASE()
    AND table_name = 'paiements'
    AND index_name = 'idx_paiements_student_date'
);
SET @ddl = IF(@index_exists = 0,
  'CREATE INDEX idx_paiements_student_date ON paiements (student_id, date_paiement)',
  'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @index_exists = (
  SELECT COUNT(*)
  FROM information_schema.statistics
  WHERE table_schema = DATABASE()
    AND table_name = 'paiements'
    AND index_name = 'idx_paiements_due_remaining'
);
SET @ddl = IF(@index_exists = 0,
  'CREATE INDEX idx_paiements_due_remaining ON paiements (due_date, montant_restant)',
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
    AND index_name = 'idx_inscription_class_year'
);
SET @ddl = IF(@index_exists = 0,
  'CREATE INDEX idx_inscription_class_year ON inscription_student (classe_room_id, anneescolaire_id)',
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
    AND index_name = 'idx_inscription_student_year'
);
SET @ddl = IF(@index_exists = 0,
  'CREATE INDEX idx_inscription_student_year ON inscription_student (student_id, anneescolaire_id)',
  'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;


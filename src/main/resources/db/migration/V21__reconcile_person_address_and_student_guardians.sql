-- Reconcile the canonical JPA mapping with the physical schema without removing data.

SET @column_exists = (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'persons' AND column_name = 'region');
SET @ddl = IF(@column_exists = 0, 'ALTER TABLE persons ADD COLUMN region VARCHAR(100) NULL AFTER city', 'SELECT 1');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @column_exists = (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'persons' AND column_name = 'address_complement');
SET @ddl = IF(@column_exists = 0, 'ALTER TABLE persons ADD COLUMN address_complement VARCHAR(255) NULL AFTER country', 'SELECT 1');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

CREATE TABLE IF NOT EXISTS student_guardians (
  id BIGINT NOT NULL AUTO_INCREMENT,
  active BOOLEAN DEFAULT TRUE,
  created_at DATETIME(6) NULL,
  updated_at DATETIME(6) NULL,
  student_id BIGINT NOT NULL,
  guardian_person_id BIGINT NOT NULL,
  relationship_type VARCHAR(20) NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_student_guardians_student_person (student_id, guardian_person_id),
  KEY idx_student_guardians_guardian_person (guardian_person_id),
  CONSTRAINT fk_student_guardians_student FOREIGN KEY (student_id) REFERENCES students (id),
  CONSTRAINT fk_student_guardians_person FOREIGN KEY (guardian_person_id) REFERENCES persons (id)
);

INSERT IGNORE INTO student_guardians (active, created_at, updated_at, student_id, guardian_person_id, relationship_type)
SELECT sp.active, sp.created_at, sp.updated_at, sp.student_id, p.person_id, sp.relationship_type
FROM student_parents sp
JOIN parents p ON p.id = sp.parent_id
WHERE p.person_id IS NOT NULL;
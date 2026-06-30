CREATE TABLE IF NOT EXISTS persons (
  id BIGINT NOT NULL AUTO_INCREMENT,
  active BOOLEAN DEFAULT TRUE,
  created_at DATETIME(6) NULL,
  updated_at DATETIME(6) NULL,
  first_name VARCHAR(100) NOT NULL,
  last_name VARCHAR(100) NOT NULL,
  email VARCHAR(150) NULL,
  phone VARCHAR(20) NULL,
  birth_date DATE NULL,
  address VARCHAR(255) NULL,
  city VARCHAR(100) NULL,
  country VARCHAR(100) NULL,
  gender VARCHAR(255) NULL,
  photo_url VARCHAR(500) NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_persons_email (email)
);

CREATE TABLE IF NOT EXISTS user_accounts (
  id BIGINT NOT NULL AUTO_INCREMENT,
  active BOOLEAN DEFAULT TRUE,
  created_at DATETIME(6) NULL,
  updated_at DATETIME(6) NULL,
  person_id BIGINT NOT NULL,
  username VARCHAR(50) NOT NULL,
  password VARCHAR(255) NOT NULL,
  enabled BOOLEAN DEFAULT TRUE,
  email_verified BOOLEAN DEFAULT FALSE,
  last_login DATETIME(6) NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_user_accounts_person (person_id),
  UNIQUE KEY uk_user_accounts_username (username),
  CONSTRAINT fk_user_accounts_person
    FOREIGN KEY (person_id) REFERENCES persons (id)
);

CREATE TABLE IF NOT EXISTS user_roles (
  user_id BIGINT NOT NULL,
  role_id BIGINT NOT NULL,
  PRIMARY KEY (user_id, role_id),
  CONSTRAINT fk_user_roles_account
    FOREIGN KEY (user_id) REFERENCES user_accounts (id),
  CONSTRAINT fk_user_roles_role
    FOREIGN KEY (role_id) REFERENCES roles (id)
);

CREATE TABLE IF NOT EXISTS positions (
  id BIGINT NOT NULL AUTO_INCREMENT,
  active BOOLEAN DEFAULT TRUE,
  created_at DATETIME(6) NULL,
  updated_at DATETIME(6) NULL,
  code VARCHAR(50) NOT NULL,
  label VARCHAR(100) NOT NULL,
  description VARCHAR(255) NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_positions_code (code)
);

CREATE TABLE IF NOT EXISTS employees (
  id BIGINT NOT NULL AUTO_INCREMENT,
  active BOOLEAN DEFAULT TRUE,
  created_at DATETIME(6) NULL,
  updated_at DATETIME(6) NULL,
  person_id BIGINT NOT NULL,
  employee_number VARCHAR(50) NOT NULL,
  hire_date DATE NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_employees_person (person_id),
  UNIQUE KEY uk_employees_employee_number (employee_number),
  CONSTRAINT fk_employees_person
    FOREIGN KEY (person_id) REFERENCES persons (id)
);

CREATE TABLE IF NOT EXISTS employee_positions (
  id BIGINT NOT NULL AUTO_INCREMENT,
  active BOOLEAN DEFAULT TRUE,
  created_at DATETIME(6) NULL,
  updated_at DATETIME(6) NULL,
  employee_id BIGINT NOT NULL,
  position_id BIGINT NOT NULL,
  start_date DATE NOT NULL,
  end_date DATE NULL,
  PRIMARY KEY (id),
  KEY idx_employee_positions_employee (employee_id),
  KEY idx_employee_positions_position (position_id),
  CONSTRAINT fk_employee_positions_employee
    FOREIGN KEY (employee_id) REFERENCES employees (id),
  CONSTRAINT fk_employee_positions_position
    FOREIGN KEY (position_id) REFERENCES positions (id)
);

CREATE TABLE IF NOT EXISTS student_parents (
  id BIGINT NOT NULL AUTO_INCREMENT,
  active BOOLEAN DEFAULT TRUE,
  created_at DATETIME(6) NULL,
  updated_at DATETIME(6) NULL,
  student_id BIGINT NOT NULL,
  parent_id BIGINT NOT NULL,
  relationship_type VARCHAR(20) NOT NULL,
  PRIMARY KEY (id),
  KEY idx_student_parents_student (student_id),
  KEY idx_student_parents_parent (parent_id)
);

SET @column_exists = (
  SELECT COUNT(*)
  FROM information_schema.columns
  WHERE table_schema = DATABASE()
    AND table_name = 'students'
    AND column_name = 'active'
);
SET @ddl = IF(@column_exists = 0,
  'ALTER TABLE students ADD COLUMN active BOOLEAN DEFAULT TRUE',
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
    AND column_name = 'person_id'
);
SET @ddl = IF(@column_exists = 0,
  'ALTER TABLE students ADD COLUMN person_id BIGINT NULL',
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
    AND column_name = 'student_number'
);
SET @ddl = IF(@column_exists = 0,
  'ALTER TABLE students ADD COLUMN student_number VARCHAR(50) NULL',
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
    AND column_name = 'admission_date'
);
SET @ddl = IF(@column_exists = 0,
  'ALTER TABLE students ADD COLUMN admission_date DATE NULL',
  'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @index_exists = (
  SELECT COUNT(*)
  FROM information_schema.statistics
  WHERE table_schema = DATABASE()
    AND table_name = 'students'
    AND index_name = 'uk_students_person'
);
SET @ddl = IF(@index_exists = 0,
  'CREATE UNIQUE INDEX uk_students_person ON students (person_id)',
  'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @index_exists = (
  SELECT COUNT(*)
  FROM information_schema.statistics
  WHERE table_schema = DATABASE()
    AND table_name = 'students'
    AND index_name = 'uk_students_student_number'
);
SET @ddl = IF(@index_exists = 0,
  'CREATE UNIQUE INDEX uk_students_student_number ON students (student_number)',
  'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @column_exists = (
  SELECT COUNT(*)
  FROM information_schema.columns
  WHERE table_schema = DATABASE()
    AND table_name = 'parents'
    AND column_name = 'active'
);
SET @ddl = IF(@column_exists = 0,
  'ALTER TABLE parents ADD COLUMN active BOOLEAN DEFAULT TRUE',
  'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @column_exists = (
  SELECT COUNT(*)
  FROM information_schema.columns
  WHERE table_schema = DATABASE()
    AND table_name = 'parents'
    AND column_name = 'person_id'
);
SET @ddl = IF(@column_exists = 0,
  'ALTER TABLE parents ADD COLUMN person_id BIGINT NULL',
  'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @column_exists = (
  SELECT COUNT(*)
  FROM information_schema.columns
  WHERE table_schema = DATABASE()
    AND table_name = 'parents'
    AND column_name = 'occupation'
);
SET @ddl = IF(@column_exists = 0,
  'ALTER TABLE parents ADD COLUMN occupation VARCHAR(100) NULL',
  'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @index_exists = (
  SELECT COUNT(*)
  FROM information_schema.statistics
  WHERE table_schema = DATABASE()
    AND table_name = 'parents'
    AND index_name = 'uk_parents_person'
);
SET @ddl = IF(@index_exists = 0,
  'CREATE UNIQUE INDEX uk_parents_person ON parents (person_id)',
  'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

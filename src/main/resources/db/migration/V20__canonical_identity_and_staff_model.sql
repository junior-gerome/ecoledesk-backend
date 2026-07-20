-- Canonical identity/staff model: Person -> UserAccount and Person -> StaffMember.
-- Legacy tables are intentionally retained so production data can be migrated safely.

CREATE TABLE IF NOT EXISTS staff_members (
  id BIGINT NOT NULL AUTO_INCREMENT,
  active BOOLEAN DEFAULT TRUE,
  created_at DATETIME(6) NULL,
  updated_at DATETIME(6) NULL,
  person_id BIGINT NOT NULL,
  employee_number VARCHAR(50) NOT NULL,
  employment_date DATE NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_staff_members_person (person_id),
  UNIQUE KEY uk_staff_members_employee_number (employee_number),
  CONSTRAINT fk_staff_members_person FOREIGN KEY (person_id) REFERENCES persons (id)
);

CREATE TABLE IF NOT EXISTS staff_assignments (
  id BIGINT NOT NULL AUTO_INCREMENT,
  active BOOLEAN DEFAULT TRUE,
  created_at DATETIME(6) NULL,
  updated_at DATETIME(6) NULL,
  staff_member_id BIGINT NOT NULL,
  professional_function VARCHAR(50) NOT NULL,
  start_date DATE NOT NULL,
  end_date DATE NULL,
  PRIMARY KEY (id),
  KEY idx_staff_assignments_member (staff_member_id),
  CONSTRAINT fk_staff_assignments_member FOREIGN KEY (staff_member_id) REFERENCES staff_members (id)
);

INSERT INTO staff_members (active, created_at, updated_at, person_id, employee_number, employment_date)
SELECT e.active, e.created_at, e.updated_at, e.person_id, e.employee_number, e.hire_date
FROM employees e
WHERE NOT EXISTS (SELECT 1 FROM staff_members sm WHERE sm.person_id = e.person_id);

INSERT INTO staff_assignments (active, created_at, updated_at, staff_member_id, professional_function, start_date, end_date)
SELECT ep.active, ep.created_at, ep.updated_at, sm.id,
       CASE UPPER(p.code)
         WHEN 'TEACHER' THEN 'TEACHER' WHEN 'ENSEIGNANT' THEN 'TEACHER'
         WHEN 'SECRETARY' THEN 'SECRETARY' WHEN 'SECRETAIRE' THEN 'SECRETARY'
         WHEN 'ACCOUNTANT' THEN 'ACCOUNTANT' WHEN 'COMPTABLE' THEN 'ACCOUNTANT'
         WHEN 'DIRECTOR' THEN 'DIRECTOR' WHEN 'DIRECTEUR' THEN 'DIRECTOR'
         WHEN 'CLEANER' THEN 'CLEANER' WHEN 'SECURITY_GUARD' THEN 'SECURITY_GUARD'
         ELSE 'SECRETARY'
       END,
       ep.start_date, ep.end_date
FROM employee_positions ep
JOIN employees e ON e.id = ep.employee_id
JOIN staff_members sm ON sm.person_id = e.person_id
JOIN positions p ON p.id = ep.position_id
WHERE NOT EXISTS (
  SELECT 1 FROM staff_assignments sa
  WHERE sa.staff_member_id = sm.id AND sa.start_date = ep.start_date AND sa.professional_function =
    CASE UPPER(p.code)
      WHEN 'TEACHER' THEN 'TEACHER' WHEN 'ENSEIGNANT' THEN 'TEACHER'
      WHEN 'SECRETARY' THEN 'SECRETARY' WHEN 'SECRETAIRE' THEN 'SECRETARY'
      WHEN 'ACCOUNTANT' THEN 'ACCOUNTANT' WHEN 'COMPTABLE' THEN 'ACCOUNTANT'
      WHEN 'DIRECTOR' THEN 'DIRECTOR' WHEN 'DIRECTEUR' THEN 'DIRECTOR'
      WHEN 'CLEANER' THEN 'CLEANER' WHEN 'SECURITY_GUARD' THEN 'SECURITY_GUARD'
      ELSE 'SECRETARY'
    END
);

SET @column_exists = (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'user_accounts' AND column_name = 'reset_token');
SET @ddl = IF(@column_exists = 0, 'ALTER TABLE user_accounts ADD COLUMN reset_token VARCHAR(64) NULL', 'SELECT 1');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @column_exists = (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'user_accounts' AND column_name = 'reset_token_expires_at');
SET @ddl = IF(@column_exists = 0, 'ALTER TABLE user_accounts ADD COLUMN reset_token_expires_at DATETIME(6) NULL', 'SELECT 1');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- Migrate the legacy Users/UsersProfil aggregate into Person + UserAccount when present.
INSERT INTO persons (active, created_at, updated_at, first_name, last_name, email)
SELECT u.actif, u.created_at, u.updated_at, up.first_name, up.last_name, up.email_user
FROM users u
JOIN profil_utilisateur up ON up.user_id = u.id
WHERE NOT EXISTS (SELECT 1 FROM persons p WHERE p.email = up.email_user);

INSERT INTO user_accounts (active, created_at, updated_at, person_id, username, password, enabled, email_verified, last_login, reset_token, reset_token_expires_at)
SELECT u.actif, u.created_at, u.updated_at, p.id, u.username, u.password, u.actif, FALSE, u.last_login, u.reset_token, u.reset_token_expires_at
FROM users u
JOIN profil_utilisateur up ON up.user_id = u.id
JOIN persons p ON p.email = up.email_user
WHERE NOT EXISTS (SELECT 1 FROM user_accounts ua WHERE ua.username = u.username);

INSERT IGNORE INTO user_roles (user_id, role_id)
SELECT ua.id, COALESCE(up.role_id, r.id)
FROM users u
JOIN profil_utilisateur up ON up.user_id = u.id
JOIN user_accounts ua ON ua.username = u.username
LEFT JOIN roles r ON r.code = up.type_user
WHERE COALESCE(up.role_id, r.id) IS NOT NULL;

-- Refresh tokens are short-lived credentials. Revoke them during the aggregate migration,
-- then point the foreign key at the canonical user account table.
DELETE FROM refresh_tokens;
SET @fk_exists = (SELECT COUNT(*) FROM information_schema.table_constraints WHERE table_schema = DATABASE() AND table_name = 'refresh_tokens' AND constraint_name = 'fk_refresh_tokens_user' AND constraint_type = 'FOREIGN KEY');
SET @ddl = IF(@fk_exists = 1, 'ALTER TABLE refresh_tokens DROP FOREIGN KEY fk_refresh_tokens_user', 'SELECT 1');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;
ALTER TABLE refresh_tokens ADD CONSTRAINT fk_refresh_tokens_user_account FOREIGN KEY (user_id) REFERENCES user_accounts (id);
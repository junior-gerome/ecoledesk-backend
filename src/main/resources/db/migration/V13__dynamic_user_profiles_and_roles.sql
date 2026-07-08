CREATE TABLE IF NOT EXISTS permissions (
  id BIGINT NOT NULL AUTO_INCREMENT,
  active BOOLEAN DEFAULT TRUE,
  created_at DATETIME(6) NULL,
  updated_at DATETIME(6) NULL,
  code VARCHAR(50) NOT NULL,
  description VARCHAR(255) NULL,
  resource VARCHAR(50) NULL,
  action VARCHAR(50) NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_permissions_code (code)
);

CREATE TABLE IF NOT EXISTS roles (
  id BIGINT NOT NULL AUTO_INCREMENT,
  active BOOLEAN DEFAULT TRUE,
  created_at DATETIME(6) NULL,
  updated_at DATETIME(6) NULL,
  code VARCHAR(50) NOT NULL,
  label VARCHAR(100) NOT NULL,
  description VARCHAR(255) NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_roles_code (code)
);

CREATE TABLE IF NOT EXISTS role_permissions (
  role_id BIGINT NOT NULL,
  permission_id BIGINT NOT NULL,
  PRIMARY KEY (role_id, permission_id),
  CONSTRAINT fk_role_permissions_role
    FOREIGN KEY (role_id) REFERENCES roles (id),
  CONSTRAINT fk_role_permissions_permission
    FOREIGN KEY (permission_id) REFERENCES permissions (id)
);

CREATE TABLE IF NOT EXISTS profil_utilisateur (
  id BIGINT NOT NULL AUTO_INCREMENT,
  email_user VARCHAR(100) NOT NULL,
  last_name VARCHAR(50) NOT NULL,
  first_name VARCHAR(50) NOT NULL,
  type_user VARCHAR(50) NOT NULL,
  user_id BIGINT NOT NULL,
  reference_id BIGINT NULL,
  created_at DATETIME(6) NULL,
  updated_at DATETIME(6) NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_profil_utilisateur_email_user (email_user),
  KEY idx_profil_utilisateur_user_id (user_id)
);
SET @column_exists = (
  SELECT COUNT(*)
  FROM information_schema.columns
  WHERE table_schema = DATABASE()
    AND table_name = 'profil_utilisateur'
    AND column_name = 'role_id'
);
SET @ddl = IF(@column_exists = 0,
  'ALTER TABLE profil_utilisateur ADD COLUMN role_id BIGINT NULL',
  'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @index_exists = (
  SELECT COUNT(*)
  FROM information_schema.statistics
  WHERE table_schema = DATABASE()
    AND table_name = 'profil_utilisateur'
    AND index_name = 'idx_profil_utilisateur_role_id'
);
SET @ddl = IF(@index_exists = 0,
  'CREATE INDEX idx_profil_utilisateur_role_id ON profil_utilisateur (role_id)',
  'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
INSERT INTO permissions (code, description, resource, action, active, created_at, updated_at)
SELECT 'ROLE_ADMIN', 'Acces complet administrateur', 'legacy-role', 'ADMIN', TRUE, NOW(6), NOW(6)
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE code = 'ROLE_ADMIN');

INSERT INTO permissions (code, description, resource, action, active, created_at, updated_at)
SELECT 'ROLE_AGENT', 'Acces agent administratif', 'legacy-role', 'AGENT', TRUE, NOW(6), NOW(6)
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE code = 'ROLE_AGENT');

INSERT INTO permissions (code, description, resource, action, active, created_at, updated_at)
SELECT 'ROLE_ENSEIGNANT', 'Acces enseignant', 'legacy-role', 'ENSEIGNANT', TRUE, NOW(6), NOW(6)
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE code = 'ROLE_ENSEIGNANT');

INSERT INTO permissions (code, description, resource, action, active, created_at, updated_at)
SELECT 'ROLE_PARENT', 'Acces parent', 'legacy-role', 'PARENT', TRUE, NOW(6), NOW(6)
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE code = 'ROLE_PARENT');

INSERT INTO permissions (code, description, resource, action, active, created_at, updated_at)
SELECT 'ROLE_ELEVE', 'Acces eleve', 'legacy-role', 'ELEVE', TRUE, NOW(6), NOW(6)
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE code = 'ROLE_ELEVE');

INSERT INTO roles (code, label, description, active, created_at, updated_at)
SELECT 'ADMIN', 'Administrateur', 'Profil systeme administrateur', TRUE, NOW(6), NOW(6)
WHERE NOT EXISTS (SELECT 1 FROM roles WHERE code = 'ADMIN');

INSERT INTO roles (code, label, description, active, created_at, updated_at)
SELECT 'AGENT', 'Agent', 'Profil systeme agent administratif', TRUE, NOW(6), NOW(6)
WHERE NOT EXISTS (SELECT 1 FROM roles WHERE code = 'AGENT');

INSERT INTO roles (code, label, description, active, created_at, updated_at)
SELECT 'ENSEIGNANT', 'Enseignant', 'Profil systeme enseignant', TRUE, NOW(6), NOW(6)
WHERE NOT EXISTS (SELECT 1 FROM roles WHERE code = 'ENSEIGNANT');

INSERT INTO roles (code, label, description, active, created_at, updated_at)
SELECT 'PARENT', 'Parent', 'Profil systeme parent', TRUE, NOW(6), NOW(6)
WHERE NOT EXISTS (SELECT 1 FROM roles WHERE code = 'PARENT');

INSERT INTO roles (code, label, description, active, created_at, updated_at)
SELECT 'ELEVE', 'Eleve', 'Profil systeme eleve', TRUE, NOW(6), NOW(6)
WHERE NOT EXISTS (SELECT 1 FROM roles WHERE code = 'ELEVE');

INSERT IGNORE INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
JOIN permissions p ON p.code = CONCAT('ROLE_', r.code)
WHERE r.code IN ('ADMIN', 'AGENT', 'ENSEIGNANT', 'PARENT', 'ELEVE');
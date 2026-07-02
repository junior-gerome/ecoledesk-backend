CREATE TABLE IF NOT EXISTS sections (
  id BIGINT NOT NULL AUTO_INCREMENT,
  libelle VARCHAR(255) NOT NULL,
  description VARCHAR(255) NULL,
  PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS statut_annee (
  code VARCHAR(20) NOT NULL,
  libelle VARCHAR(100) NOT NULL,
  PRIMARY KEY (code)
);

CREATE TABLE IF NOT EXISTS annee_scolaire (
  id BIGINT NOT NULL AUTO_INCREMENT,
  libelle_annee_scolaire VARCHAR(50) NOT NULL,
  date_debut DATE NOT NULL,
  date_fin DATE NOT NULL,
  statut_code BOOLEAN NOT NULL,
  created_at DATETIME(6) NULL,
  updated_at DATETIME(6) NULL,
  PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS trimestre (
  id BIGINT NOT NULL AUTO_INCREMENT,
  libelle_trimestre VARCHAR(50) NOT NULL,
  annee_scolaire_id BIGINT NULL,
  created_at DATETIME(6) NULL,
  updated_at DATETIME(6) NULL,
  PRIMARY KEY (id),
  KEY idx_trimestre_annee_scolaire (annee_scolaire_id)
);

CREATE TABLE IF NOT EXISTS sequence (
  id BIGINT NOT NULL AUTO_INCREMENT,
  libelle_sequence VARCHAR(50) NOT NULL,
  trimestre_id BIGINT NULL,
  created_at DATETIME(6) NULL,
  updated_at DATETIME(6) NULL,
  PRIMARY KEY (id),
  KEY idx_sequence_trimestre (trimestre_id)
);

CREATE TABLE IF NOT EXISTS enseignant (
  id BIGINT NOT NULL AUTO_INCREMENT,
  lastname_teacher VARCHAR(100) NOT NULL,
  firstname_teacher VARCHAR(100) NOT NULL,
  email VARCHAR(100) NOT NULL,
  gender ENUM('MASCULIN', 'FEMININ') NOT NULL,
  phone_number VARCHAR(20) NOT NULL,
  speciality VARCHAR(100) NULL,
  niveau VARCHAR(50) NULL,
  date_embauche DATE NULL,
  adress VARCHAR(255) NULL,
  created_at DATETIME(6) NULL,
  updated_at DATETIME(6) NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_enseignant_email (email),
  UNIQUE KEY uk_enseignant_phone_number (phone_number)
);

CREATE TABLE IF NOT EXISTS subject (
  id BIGINT NOT NULL AUTO_INCREMENT,
  name_subject VARCHAR(100) NOT NULL,
  code VARCHAR(20) NOT NULL,
  coefficient INT NULL,
  description VARCHAR(500) NULL,
  actif BOOLEAN DEFAULT TRUE,
  created_at DATETIME(6) NULL,
  updated_at DATETIME(6) NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_subject_code (code)
);

CREATE TABLE IF NOT EXISTS classes (
  id BIGINT NOT NULL AUTO_INCREMENT,
  name_classe VARCHAR(100) NOT NULL,
  niveau VARCHAR(50) NOT NULL,
  section_id BIGINT NULL,
  capacite INT NULL,
  teacher_id BIGINT NULL,
  anneescolaire_id BIGINT NULL,
  description VARCHAR(500) NULL,
  actif BOOLEAN DEFAULT TRUE,
  created_at DATETIME(6) NULL,
  updated_at DATETIME(6) NULL,
  PRIMARY KEY (id),
  KEY idx_classes_section (section_id),
  KEY idx_classes_teacher (teacher_id),
  KEY idx_classes_annee_scolaire (anneescolaire_id)
);

CREATE TABLE IF NOT EXISTS type_affectation (
  code VARCHAR(20) NOT NULL,
  libelle VARCHAR(100) NOT NULL,
  PRIMARY KEY (code)
);

CREATE TABLE IF NOT EXISTS affectation (
  id BIGINT NOT NULL AUTO_INCREMENT,
  type_code VARCHAR(20) NOT NULL,
  date_debut DATE NULL,
  date_fin DATE NULL,
  teacher_id BIGINT NOT NULL,
  classe_id BIGINT NOT NULL,
  subject_id BIGINT NOT NULL,
  annee_scolaire_id BIGINT NOT NULL,
  created_at DATETIME(6) NULL,
  updated_at DATETIME(6) NULL,
  PRIMARY KEY (id),
  KEY idx_affectation_teacher (teacher_id),
  KEY idx_affectation_classe (classe_id),
  KEY idx_affectation_subject (subject_id),
  KEY idx_affectation_annee_scolaire (annee_scolaire_id)
);

CREATE TABLE IF NOT EXISTS students (
  id BIGINT NOT NULL AUTO_INCREMENT,
  active BOOLEAN DEFAULT TRUE,
  created_at DATETIME(6) NULL,
  updated_at DATETIME(6) NULL,
  PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS parents (
  id BIGINT NOT NULL AUTO_INCREMENT,
  active BOOLEAN DEFAULT TRUE,
  created_at DATETIME(6) NULL,
  updated_at DATETIME(6) NULL,
  PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS document (
  id BIGINT NOT NULL AUTO_INCREMENT,
  student_id BIGINT NULL,
  type_document VARCHAR(50) NOT NULL,
  chemin_fichier VARCHAR(255) NOT NULL,
  date_ajout DATETIME(6) NULL,
  PRIMARY KEY (id),
  KEY idx_document_student (student_id)
);

CREATE TABLE IF NOT EXISTS montant (
  id BIGINT NOT NULL AUTO_INCREMENT,
  `count` DECIMAL(15,2) NOT NULL,
  classe_room_id BIGINT NOT NULL,
  type_paiement VARCHAR(50) NOT NULL,
  PRIMARY KEY (id),
  KEY idx_montant_classe_room (classe_room_id)
);

CREATE TABLE IF NOT EXISTS inscription_student (
  id BIGINT NOT NULL AUTO_INCREMENT,
  student_id BIGINT NOT NULL,
  classe_room_id BIGINT NOT NULL,
  montant_id BIGINT NOT NULL,
  anneescolaire_id BIGINT NOT NULL,
  date_inscription DATE NOT NULL,
  PRIMARY KEY (id),
  KEY idx_inscription_student (student_id),
  KEY idx_inscription_classe_room (classe_room_id),
  KEY idx_inscription_montant (montant_id),
  KEY idx_inscription_annee_scolaire (anneescolaire_id)
);

CREATE TABLE IF NOT EXISTS paiements (
  id BIGINT NOT NULL AUTO_INCREMENT,
  student_id BIGINT NOT NULL,
  date_paiement DATE NULL,
  montant_paye DECIMAL(15,2) NULL,
  montant_restant DECIMAL(15,2) NULL,
  remise DECIMAL(15,2) NULL,
  description VARCHAR(255) NULL,
  inscription_student_id BIGINT NOT NULL,
  montant_id BIGINT NOT NULL,
  type_paiement VARCHAR(50) NOT NULL,
  PRIMARY KEY (id),
  KEY idx_paiements_student (student_id),
  KEY idx_paiements_inscription (inscription_student_id),
  KEY idx_paiements_montant (montant_id)
);

CREATE TABLE IF NOT EXISTS academic_grade (
  id BIGINT NOT NULL AUTO_INCREMENT,
  student_id BIGINT NOT NULL,
  subject_id BIGINT NOT NULL,
  classe_id BIGINT NOT NULL,
  sequence_id BIGINT NOT NULL,
  trimestre_id BIGINT NULL,
  grade DECIMAL(4,2) NOT NULL,
  coefficient DECIMAL(4,2) NOT NULL DEFAULT 1.00,
  pedagogical_comment VARCHAR(1000) NULL,
  assessment_date DATETIME(6) NOT NULL,
  created_at DATETIME(6) NULL,
  updated_at DATETIME(6) NULL,
  version INT NULL,
  period VARCHAR(255) NULL,
  PRIMARY KEY (id),
  KEY idx_grade_student (student_id),
  KEY idx_grade_subject (subject_id),
  KEY idx_grade_classe (classe_id),
  KEY idx_grade_sequence (sequence_id)
);

CREATE TABLE IF NOT EXISTS student_progress_report (
  id BIGINT NOT NULL AUTO_INCREMENT,
  student_id BIGINT NOT NULL,
  period VARCHAR(20) NOT NULL,
  average_grade DOUBLE NULL,
  attendance_rate DOUBLE NULL,
  comments VARCHAR(1000) NULL,
  created_at DATETIME(6) NULL,
  updated_at DATETIME(6) NULL,
  PRIMARY KEY (id),
  KEY idx_student_progress_report_student (student_id)
);

CREATE TABLE IF NOT EXISTS notifications (
  id BIGINT NOT NULL AUTO_INCREMENT,
  user_id BIGINT NULL,
  message VARCHAR(255) NOT NULL,
  created_at DATETIME(6) NULL,
  is_read BOOLEAN NOT NULL DEFAULT FALSE,
  PRIMARY KEY (id),
  KEY idx_notifications_user (user_id)
);

CREATE TABLE IF NOT EXISTS log_activite (
  id BIGINT NOT NULL AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  action VARCHAR(255) NOT NULL,
  date_action DATETIME(6) NULL,
  ip_adresse VARCHAR(45) NULL,
  table_cible VARCHAR(100) NULL,
  reference_id BIGINT NULL,
  PRIMARY KEY (id),
  KEY idx_log_activite_user (user_id)
);

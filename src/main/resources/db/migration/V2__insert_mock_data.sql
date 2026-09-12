-- =============================================================================
-- V2__insert_mock_data.sql
-- Donnees mock completes pour le schema bd_gsbp.
-- STRATEGIE : REMISE A ZERO DETERMINISTE puis INSERT. Le script efface les
-- tables de donnees mock (ordre respectueux des FK) et re-insere un jeu de
-- donnees coherent et reproductible, utilisable pour tester tous les workflows.
-- Idempotent : peut etre re-execute sans erreur.
-- ATTENTION : detruit les donnees presente dans les tables couvertes ci-dessous.
-- =============================================================================

-- ---------------------------------------------------------------------------
-- 0. RESET : suppression de toutes les donnees mock (enfants avant parents)
-- ---------------------------------------------------------------------------
DELETE FROM authentication_audit_events;
DELETE FROM refresh_tokens;
DELETE FROM log_activite;
DELETE FROM notifications;
DELETE FROM user_roles;
DELETE FROM role_permissions;
DELETE FROM payment_installments;
DELETE FROM tuition_payment_plans;
DELETE FROM paiements;
DELETE FROM academic_grade;
DELETE FROM absences;
DELETE FROM student_progress_report;
DELETE FROM document;
DELETE FROM enrollments;
DELETE FROM pre_enrollment_fee_payments;
DELETE FROM pre_enrollment_documents;
DELETE FROM pre_enrollment_guardians;
DELETE FROM student_guardians;
DELETE FROM pre_enrollments;
DELETE FROM affectation;
DELETE FROM staff_assignments;
DELETE FROM montant;
DELETE FROM classes;
DELETE FROM sequence;
DELETE FROM trimestre;
DELETE FROM students;
DELETE FROM guardians;
DELETE FROM staff_members;
DELETE FROM user_accounts;
DELETE FROM subject;
DELETE FROM sections;
DELETE FROM academic_year;
DELETE FROM persons;
DELETE FROM statut_year;
DELETE FROM type_affectation;
DELETE FROM permissions;
DELETE FROM roles;

-- ---------------------------------------------------------------------------
-- 1. ROLES (tous, y compris ADMIN re-insere apres la purge)
-- ---------------------------------------------------------------------------
INSERT IGNORE INTO roles (code, label, description, active, created_at, updated_at) VALUES
('ADMIN', 'Administrateur', 'Acces total a la plateforme', true, NOW(), NOW()),
('DIRECTEUR', 'Directeur', 'Directeur de l''etablissement', true, NOW(), NOW()),
('ENSEIGNANT', 'Enseignant', 'Enseignant / Professeur', true, NOW(), NOW()),
('SECRETAIRE', 'Secretaire', 'Secretaire de l''ecole', true, NOW(), NOW()),
('COMPTABLE', 'Comptable', 'Comptable / Gestionnaire financier', true, NOW(), NOW()),
('PARENT', 'Parent', 'Parent d''eleve', true, NOW(), NOW())
ON DUPLICATE KEY UPDATE label = VALUES(label);

-- ---------------------------------------------------------------------------
-- 2. PERMISSIONS
-- ---------------------------------------------------------------------------
INSERT IGNORE INTO permissions (code, action, resource, description, active, created_at, updated_at) VALUES
('STUDENTS_VIEW', 'VIEW', 'STUDENTS', 'Consulter la liste des eleves', true, NOW(), NOW()),
('STUDENTS_CREATE', 'CREATE', 'STUDENTS', 'Creer un nouvel eleve', true, NOW(), NOW()),
('STUDENTS_UPDATE', 'UPDATE', 'STUDENTS', 'Modifier les informations d''un eleve', true, NOW(), NOW()),
('STUDENTS_DELETE', 'DELETE', 'STUDENTS', 'Supprimer un eleve', true, NOW(), NOW()),
('GRADES_VIEW', 'VIEW', 'GRADES', 'Consulter les notes', true, NOW(), NOW()),
('GRADES_CREATE', 'CREATE', 'GRADES', 'Saisir des notes', true, NOW(), NOW()),
('GRADES_UPDATE', 'UPDATE', 'GRADES', 'Modifier des notes', true, NOW(), NOW()),
('GRADES_LOCK', 'LOCK', 'GRADES', 'Verrouiller les notes d''une sequence', true, NOW(), NOW()),
('GRADES_VALIDATE', 'VALIDATE', 'GRADES', 'Valider les notes d''une sequence', true, NOW(), NOW()),
('ATTENDANCE_VIEW', 'VIEW', 'ATTENDANCE', 'Consulter les presences', true, NOW(), NOW()),
('ATTENDANCE_CREATE', 'CREATE', 'ATTENDANCE', 'Enregistrer les presences', true, NOW(), NOW()),
('ENROLLMENT_VIEW', 'VIEW', 'ENROLLMENTS', 'Consulter les inscriptions', true, NOW(), NOW()),
('ENROLLMENT_CREATE', 'CREATE', 'ENROLLMENTS', 'Creer une inscription', true, NOW(), NOW()),
('ENROLLMENT_UPDATE', 'UPDATE', 'ENROLLMENTS', 'Modifier une inscription', true, NOW(), NOW()),
('PRE_ENROLLMENT_VIEW', 'VIEW', 'PRE_ENROLLMENTS', 'Consulter les pre-inscriptions', true, NOW(), NOW()),
('PRE_ENROLLMENT_REVIEW', 'REVIEW', 'PRE_ENROLLMENTS', 'Examiner les dossiers de pre-inscription', true, NOW(), NOW()),
('PRE_ENROLLMENT_APPROVE', 'APPROVE', 'PRE_ENROLLMENTS', 'Approuver les pre-inscriptions', true, NOW(), NOW()),
('BILLING_VIEW', 'VIEW', 'BILLING', 'Consulter les paiements', true, NOW(), NOW()),
('BILLING_CREATE', 'CREATE', 'BILLING', 'Enregistrer un paiement', true, NOW(), NOW()),
('BILLING_REFUND', 'REFUND', 'BILLING', 'Annuler / rembourser un paiement', true, NOW(), NOW()),
('REPORTS_VIEW', 'VIEW', 'REPORTS', 'Consulter les rapports', true, NOW(), NOW()),
('SETTINGS_VIEW', 'VIEW', 'SETTINGS', 'Consulter les parametres', true, NOW(), NOW()),
('SETTINGS_UPDATE', 'UPDATE', 'SETTINGS', 'Modifier les parametres', true, NOW(), NOW()),
('STAFF_VIEW', 'VIEW', 'STAFF', 'Consulter les membres du personnel', true, NOW(), NOW()),
('STAFF_CREATE', 'CREATE', 'STAFF', 'Ajouter un membre du personnel', true, NOW(), NOW()),
('STAFF_UPDATE', 'UPDATE', 'STAFF', 'Modifier les informations du personnel', true, NOW(), NOW()),
('CLASSES_VIEW', 'VIEW', 'CLASSES', 'Consulter les classes', true, NOW(), NOW()),
('CLASSES_CREATE', 'CREATE', 'CLASSES', 'Creer une classe', true, NOW(), NOW()),
('DOCUMENTS_VIEW', 'VIEW', 'DOCUMENTS', 'Consulter les documents', true, NOW(), NOW()),
('DOCUMENTS_UPLOAD', 'UPLOAD', 'DOCUMENTS', 'Deposer un document', true, NOW(), NOW()),
('NOTIFICATIONS_VIEW', 'VIEW', 'NOTIFICATIONS', 'Consulter les notifications', true, NOW(), NOW())
ON DUPLICATE KEY UPDATE description = VALUES(description);

-- ---------------------------------------------------------------------------
-- 3. ROLE_PERMISSIONS (associer les permissions aux roles)
-- ---------------------------------------------------------------------------
-- ADMIN : toutes les permissions
INSERT IGNORE INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p WHERE r.code = 'ADMIN'
ON DUPLICATE KEY UPDATE role_id = VALUES(role_id);

-- DIRECTEUR : presque tout sauf SETTINGS_UPDATE
INSERT IGNORE INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p
WHERE r.code = 'DIRECTEUR' AND p.code NOT IN ('SETTINGS_UPDATE')
ON DUPLICATE KEY UPDATE role_id = VALUES(role_id);

-- ENSEIGNANT : notes, presences, classes, documents
INSERT IGNORE INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p
WHERE r.code = 'ENSEIGNANT' AND p.code IN (
    'STUDENTS_VIEW','GRADES_VIEW','GRADES_CREATE','GRADES_UPDATE','GRADES_LOCK',
    'ATTENDANCE_VIEW','ATTENDANCE_CREATE','CLASSES_VIEW','DOCUMENTS_VIEW','DOCUMENTS_UPLOAD',
    'REPORTS_VIEW','NOTIFICATIONS_VIEW'
)
ON DUPLICATE KEY UPDATE role_id = VALUES(role_id);

-- SECRETAIRE : eleves, inscriptions, pre-inscriptions, documents
INSERT IGNORE INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p
WHERE r.code = 'SECRETAIRE' AND p.code IN (
    'STUDENTS_VIEW','STUDENTS_CREATE','STUDENTS_UPDATE',
    'ENROLLMENT_VIEW','ENROLLMENT_CREATE',
    'PRE_ENROLLMENT_VIEW','PRE_ENROLLMENT_REVIEW',
    'DOCUMENTS_VIEW','DOCUMENTS_UPLOAD','CLASSES_VIEW','NOTIFICATIONS_VIEW'
)
ON DUPLICATE KEY UPDATE role_id = VALUES(role_id);

-- COMPTABLE : billing, rapports
INSERT IGNORE INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p
WHERE r.code = 'COMPTABLE' AND p.code IN (
    'BILLING_VIEW','BILLING_CREATE','BILLING_REFUND','REPORTS_VIEW',
    'STUDENTS_VIEW','ENROLLMENT_VIEW','NOTIFICATIONS_VIEW'
)
ON DUPLICATE KEY UPDATE role_id = VALUES(role_id);

-- PARENT : lecture seule eleves/factures
INSERT IGNORE INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p
WHERE r.code = 'PARENT' AND p.code IN (
    'STUDENTS_VIEW','BILLING_VIEW','GRADES_VIEW','REPORTS_VIEW','NOTIFICATIONS_VIEW'
)
ON DUPLICATE KEY UPDATE role_id = VALUES(role_id);

-- ---------------------------------------------------------------------------
-- 4. SECTIONS
-- ---------------------------------------------------------------------------
INSERT IGNORE INTO sections (id, libelle, description, active, created_at, updated_at) VALUES
(1, 'Maternelle', 'Section Maternelle', true, NOW(), NOW()),
(2, 'Primaire', 'Section Primaire', true, NOW(), NOW()),
(3, 'Secondaire', 'Section Secondaire', true, NOW(), NOW())
ON DUPLICATE KEY UPDATE libelle = VALUES(libelle);

-- ---------------------------------------------------------------------------
-- 5. ANNEES SCOLAIRES
-- ---------------------------------------------------------------------------
INSERT IGNORE INTO academic_year (id, libelle_academic_year, date_debut, date_fin, statut_code, active, created_at, updated_at) VALUES
(1, '2026-2027', '2026-09-01', '2027-08-31', true, true, NOW(), NOW()),
(2, '2025-2026', '2025-09-01', '2026-08-31', false, false, NOW(), NOW())
ON DUPLICATE KEY UPDATE libelle_academic_year = VALUES(libelle_academic_year);

-- ---------------------------------------------------------------------------
-- 6. STATUT_YEAR et TYPE_AFFECTATION (donnees de reference)
-- ---------------------------------------------------------------------------
INSERT IGNORE INTO statut_year (code, libelle) VALUES
('EN_COURS', 'En cours'),
('TERMINEE', 'Terminee'),
('A_VENIR', 'A venir')
ON DUPLICATE KEY UPDATE libelle = VALUES(libelle);

INSERT IGNORE INTO type_affectation (code, libelle) VALUES
('TITULAIRE', 'Titulaire'),
('VACATAIRE', 'Vacataire'),
('STAGIAIRE', 'Stagiaire')
ON DUPLICATE KEY UPDATE libelle = VALUES(libelle);

-- ---------------------------------------------------------------------------
-- 7. MATIERES (Sujets)
-- ---------------------------------------------------------------------------
INSERT IGNORE INTO subject (id, code, name_subject, coefficient, description, active, created_at, updated_at) VALUES
(1, 'MATHS', 'Mathematiques', 4, 'Algebre, geometrie, arithmetique', true, NOW(), NOW()),
(2, 'FRAN', 'Francais', 3, 'Grammaire, conjugaison, litterature', true, NOW(), NOW()),
(3, 'ANGL', 'Anglais', 2, 'Langue anglaise', true, NOW(), NOW()),
(4, 'HIST', 'Histoire-Geographie', 2, 'Histoire et geographie', true, NOW(), NOW()),
(5, 'SCIE', 'Sciences', 3, 'Sciences de la vie et de la terre, physique', true, NOW(), NOW()),
(6, 'EPS', 'Education Physique', 1, 'Sport et education physique', true, NOW(), NOW()),
(7, 'DESS', 'Dessin', 1, 'Arts plastiques et dessin', true, NOW(), NOW()),
(8, 'INFI', 'Informatique', 1, 'Initiation a l''informatique', true, NOW(), NOW()),
(9, 'ARAB', 'Arabe', 2, 'Langue arabe', true, NOW(), NOW()),
(10, 'MORA', 'Education Morale', 1, 'Enseignement moral et civique', true, NOW(), NOW())
ON DUPLICATE KEY UPDATE name_subject = VALUES(name_subject);

-- ---------------------------------------------------------------------------
-- 8. PERSONNES (base pour tous les acteurs)
--    IDs 1-14 : Staff / Admin    IDs 15-26 : Eleves    IDs 27-38 : Tuteurs
-- ---------------------------------------------------------------------------

-- === Staff / Admin (personnes 1-14) ===
INSERT IGNORE INTO persons (id, first_name, last_name, gender, birth_date, phone, email, city, country, region, address, active, created_at, updated_at) VALUES
(1,  'Ibrahima',  'Sow',      'MASCULIN', '1975-03-15', '+221771000001', 'ibrahima.sow@ecoledesk.sn',      'Dakar',    'Senegal', 'Dakar',      'Quartier Plateau, Rue 12', true, NOW(), NOW()),
(2,  'Fatou',     'Diop',     'FEMININ',  '1980-07-22', '+221771000002', 'fatou.diop@ecoledesk.sn',         'Dakar',    'Senegal', 'Dakar',      'Cite Keur Damel, Bloc B', true, NOW(), NOW()),
(3,  'Moussa',     'Fall',     'MASCULIN', '1985-11-08', '+221771000003', 'moussa.fall@ecoledesk.sn',        'Thies',    'Senegal', 'Thies',      'Avenue Bourguiba, 45', true, NOW(), NOW()),
(4,  'Aminata',    'Ndiaye',   'FEMININ',  '1990-02-14', '+221771000004', 'aminata.ndiaye@ecoledesk.sn',     'Saint-Louis', 'Senegal', 'Saint-Louis', 'Rue Faidherbe, 18', true, NOW(), NOW()),
(5,  'Ousmane',    'Ba',       'MASCULIN', '1982-06-30', '+221771000005', 'ousmane.ba@ecoledesk.sn',         'Dakar',    'Senegal', 'Dakar',      'HLM Grand Yoff, Villa 23', true, NOW(), NOW()),
(6,  'Khady',      'Sy',       'FEMININ',  '1988-09-17', '+221771000006', 'khady.sy@ecoledesk.sn',           'Kaolack',  'Senegal', 'Kaolack',    'Quartier Medina, Rue 5', true, NOW(), NOW()),
(7,  'Cheikh',     'Gueye',    'MASCULIN', '1978-12-01', '+221771000007', 'cheikh.gueye@ecoledesk.sn',       'Dakar',    'Senegal', 'Dakar',      'Fann Residence, 10', true, NOW(), NOW()),
(8,  'Mariama',    'Cisse',    'FEMININ',  '1992-04-25', '+221771000008', 'mariama.cisse@ecoledesk.sn',      'Ziguinhor','Senegal', 'Ziguinhor',  'Boulevard du Senegal, 7', true, NOW(), NOW()),
(9,  'Abdoulaye',  'Mbaye',    'MASCULIN', '1987-08-19', '+221771000009', 'abdoulaye.mbaye@ecoledesk.sn',    'Dakar',    'Senegal', 'Dakar',      'Merkoz, Rue 8', true, NOW(), NOW()),
(10, 'Ndeye',      'Fall',     'FEMININ',  '1991-01-11', '+221771000010', 'ndeye.fall@ecoledesk.sn',         'Rufisque', 'Senegal', 'Dakar',      'Rufisque Centre, 22', true, NOW(), NOW()),
(11, 'Babacar',    'Diallo',   'MASCULIN', '1983-05-28', '+221771000011', 'babacar.diallo@ecoledesk.sn',     'Dakar',    'Senegal', 'Dakar',      'Parcelles Assainies, U12', true, NOW(), NOW()),
(12, 'Awa',        'Sarr',     'FEMININ',  '1989-10-03', '+221771000012', 'awa.sarr@ecoledesk.sn',           'Mbour',    'Senegal', 'Mbour',      'Route de Ngor, 31', true, NOW(), NOW()),
(13, 'El Hadj',    'Kane',     'MASCULIN', '1976-07-07', '+221771000013', 'elhadj.kane@ecoledesk.sn',        'Dakar',    'Senegal', 'Dakar',      'Almadies, Villa 15', true, NOW(), NOW()),
(14, 'Astou',      'Ba',       'FEMININ',  '1993-12-20', '+221771000014', 'astou.ba@ecoledesk.sn',           'Dakar',    'Senegal', 'Dakar',      'Point E, Rue 10', true, NOW(), NOW());

-- === Eleves (personnes 15-26) ===
INSERT IGNORE INTO persons (id, first_name, last_name, gender, birth_date, phone, email, city, country, region, address, active, created_at, updated_at) VALUES
(15, 'Awa',        'Diop',     'FEMININ',  '2016-03-12', '+221771234501', 'awa.diop@example.com',            'Dakar',    'Senegal', 'Dakar',      'Quartier Medina', true, NOW(), NOW()),
(16, 'Mamadou',    'Ndiaye',   'MASCULIN', '2015-07-25', '+221771234502', 'mamadou.ndiaye@example.com',      'Dakar',    'Senegal', 'Dakar',      'HLM Grand Yoff', true, NOW(), NOW()),
(17, 'Fatou',      'Sarr',     'FEMININ',  '2013-01-09', '+221771234503', 'fatou.sarr@example.com',          'Thies',    'Senegal', 'Thies',      'Cite Soweto', true, NOW(), NOW()),
(18, 'Ibrahima',   'Fall',     'MASCULIN', '2011-11-03', '+221771234504', 'ibrahima.fall@example.com',       'Dakar',    'Senegal', 'Dakar',      'Plateau', true, NOW(), NOW()),
(19, 'Aminata',    'Gueye',    'FEMININ',  '2009-05-19', '+221771234505', 'aminata.gueye@example.com',       'Saint-Louis', 'Senegal', 'Saint-Louis', 'Quartier Guet Ndar', true, NOW(), NOW()),
(20, 'Babacar',    'Ba',       'MASCULIN', '2007-02-28', '+221771234506', 'babacar.ba@example.com',          'Dakar',    'Senegal', 'Dakar',      'Fann', true, NOW(), NOW()),
(21, 'Amadou',     'Kane',     'MASCULIN', '2016-08-14', '+221771234511', 'amadou.kane@example.com',         'Dakar',    'Senegal', 'Dakar',      'Almadies', true, NOW(), NOW()),
(22, 'Salimata',   'Sy',       'FEMININ',  '2015-05-03', '+221771234512', 'salimata.sy@example.com',         'Kaolack',  'Senegal', 'Kaolack',    'Medina Baye', true, NOW(), NOW()),
(23, 'Oumar',      'Cisse',    'MASCULIN', '2013-12-20', '+221771234513', 'oumar.cisse@example.com',         'Ziguinhor','Senegal', 'Ziguinhor',  'Centre', true, NOW(), NOW()),
(24, 'Aissatou',   'Mbaye',    'FEMININ',  '2008-04-17', '+221771234514', 'aissatou.mbaye@example.com',      'Mbour',    'Senegal', 'Mbour',      'Saly Portudal', true, NOW(), NOW()),
(25, 'Moussa',     'Diallo',   'MASCULIN', '2014-09-05', '+221771234515', 'moussa.diallo@example.com',       'Dakar',    'Senegal', 'Dakar',      'Parcelles Assainies', true, NOW(), NOW()),
(26, 'Ndeye',      'Thiam',    'FEMININ',  '2012-06-18', '+221771234516', 'ndeye.thiam@example.com',         'Rufisque', 'Senegal', 'Dakar',      'Rufisque Centre', true, NOW(), NOW());

-- === Tuteurs / Parents (personnes 27-38) ===
INSERT IGNORE INTO persons (id, first_name, last_name, gender, birth_date, phone, email, city, country, region, address, active, created_at, updated_at) VALUES
(27, 'Mariama',    'Diop',     'FEMININ',  '1985-03-20', '+221771234501', 'mariama.diop@example.com',        'Dakar',    'Senegal', 'Dakar',      'Quartier Medina', true, NOW(), NOW()),
(28, 'Ousmane',    'Ndiaye',   'MASCULIN', '1982-11-15', '+221771234502', 'ousmane.ndiaye@example.com',      'Dakar',    'Senegal', 'Dakar',      'HLM Grand Yoff', true, NOW(), NOW()),
(29, 'Aly',        'Sarr',     'MASCULIN', '1979-07-08', '+221771234503', 'aly.sarr@example.com',            'Thies',    'Senegal', 'Thies',      'Cite Soweto', true, NOW(), NOW()),
(30, 'Khady',      'Fall',     'FEMININ',  '1988-01-22', '+221771234504', 'khady.fall@example.com',          'Dakar',    'Senegal', 'Dakar',      'Plateau', true, NOW(), NOW()),
(31, 'Cheikh',     'Gueye',    'MASCULIN', '1975-09-10', '+221771234505', 'cheikh.gueye@example.com',        'Saint-Louis', 'Senegal', 'Saint-Louis', 'Guet Ndar', true, NOW(), NOW()),
(32, 'Ndeye',      'Ba',       'FEMININ',  '1980-04-17', '+221771234506', 'ndeye.ba@example.com',            'Dakar',    'Senegal', 'Dakar',      'Fann Residence', true, NOW(), NOW()),
(33, 'Astou',      'Kane',     'FEMININ',  '1987-06-14', '+221771234507', 'astou.kane@example.com',          'Dakar',    'Senegal', 'Dakar',      'Almadies', true, NOW(), NOW()),
(34, 'Moussa',     'Sy',       'MASCULIN', '1983-12-01', '+221771234508', 'moussa.sy@example.com',           'Kaolack',  'Senegal', 'Kaolack',    'Medina Baye', true, NOW(), NOW()),
(35, 'Bineta',     'Cisse',    'FEMININ',  '1986-08-25', '+221771234509', 'bineta.cisse@example.com',        'Ziguinhor','Senegal', 'Ziguinhor',  'Centre', true, NOW(), NOW()),
(36, 'El Hadj',    'Mbaye',    'MASCULIN', '1978-02-11', '+221771234510', 'elhadj.mbaye@example.com',        'Mbour',    'Senegal', 'Mbour',      'Saly Portudal', true, NOW(), NOW()),
(37, 'Isabelle',   'Diallo',   'FEMININ',  '1984-10-30', '+221771234517', 'isabelle.diallo@example.com',     'Dakar',    'Senegal', 'Dakar',      'Parcelles Assainies', true, NOW(), NOW()),
(38, 'Serigne',    'Thiam',    'MASCULIN', '1981-05-09', '+221771234518', 'serigne.thiam@example.com',       'Rufisque', 'Senegal', 'Dakar',      'Rufisque Centre', true, NOW(), NOW());

-- ---------------------------------------------------------------------------
-- 9. USER_ACCOUNTS (comptes de connexion)
-- ---------------------------------------------------------------------------
INSERT IGNORE INTO user_accounts (id, username, password, person_id, email_verified, enabled, active, created_at, updated_at) VALUES
(1,  'admin',        '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 1,  true, true, true, NOW(), NOW()),
(2,  'directeur',    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 2,  true, true, true, NOW(), NOW()),
(3,  'mfall',        '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 3,  true, true, true, NOW(), NOW()),
(4,  'anddiaye',     '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 4,  true, true, true, NOW(), NOW()),
(5,  'oba',          '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 5,  true, true, true, NOW(), NOW()),
(6,  'ksy',          '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 6,  true, true, true, NOW(), NOW()),
(7,  'comptable',    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 11, true, true, true, NOW(), NOW()),
(8,  'secretaire',   '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 12, true, true, true, NOW(), NOW()),
(9,  'parent1',      '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 27, true, true, true, NOW(), NOW()),
(10, 'parent2',      '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 28, true, true, true, NOW(), NOW()),
(11, 'prof_maths',   '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 7,  true, true, true, NOW(), NOW()),
(12, 'prof_francais','$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 8,  true, true, true, NOW(), NOW()),
(13, 'prof_sciences','$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 9,  true, true, true, NOW(), NOW()),
(14, 'prof_anglais', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 10, true, true, true, NOW(), NOW()),
(15, 'user_mod',     '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 13, true, true, true, NOW(), NOW()),
(16, 'assistant',    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 14, true, true, true, NOW(), NOW());
-- Mots de passe : hash bcrypt de "Password123!"

-- ---------------------------------------------------------------------------
-- 10. USER_ROLES
-- ---------------------------------------------------------------------------
INSERT IGNORE INTO user_roles (user_id, role_id) VALUES
(1,  (SELECT id FROM roles WHERE code = 'ADMIN')),
(2,  (SELECT id FROM roles WHERE code = 'DIRECTEUR')),
(3,  (SELECT id FROM roles WHERE code = 'ENSEIGNANT')),
(4,  (SELECT id FROM roles WHERE code = 'ENSEIGNANT')),
(5,  (SELECT id FROM roles WHERE code = 'ENSEIGNANT')),
(6,  (SELECT id FROM roles WHERE code = 'ENSEIGNANT')),
(7,  (SELECT id FROM roles WHERE code = 'COMPTABLE')),
(8,  (SELECT id FROM roles WHERE code = 'SECRETAIRE')),
(9,  (SELECT id FROM roles WHERE code = 'PARENT')),
(10, (SELECT id FROM roles WHERE code = 'PARENT')),
(11, (SELECT id FROM roles WHERE code = 'ENSEIGNANT')),
(12, (SELECT id FROM roles WHERE code = 'ENSEIGNANT')),
(13, (SELECT id FROM roles WHERE code = 'ENSEIGNANT')),
(14, (SELECT id FROM roles WHERE code = 'ENSEIGNANT')),
(15, (SELECT id FROM roles WHERE code = 'ADMIN')),
(16, (SELECT id FROM roles WHERE code = 'SECRETAIRE'));

-- ---------------------------------------------------------------------------
-- 11. STAFF_MEMBERS
-- ---------------------------------------------------------------------------
INSERT IGNORE INTO staff_members (id, person_id, employee_number, employment_date, cni_number, level, speciality, active, created_at, updated_at) VALUES
(1,  1,  'EMP-001', '2015-09-01', 'CNI-001', 'Licence', 'Direction Generale',        true, NOW(), NOW()),
(2,  2,  'EMP-002', '2018-09-01', 'CNI-002', 'Master',  'Administration scolaire',  true, NOW(), NOW()),
(3,  3,  'EMP-003', '2020-09-01', 'CNI-003', 'Licence', 'Mathematiques',             true, NOW(), NOW()),
(4,  4,  'EMP-004', '2021-09-01', 'CNI-004', 'Licence', 'Francais',                  true, NOW(), NOW()),
(5,  5,  'EMP-005', '2019-09-01', 'CNI-005', 'Licence', 'Sciences Physiques',        true, NOW(), NOW()),
(6,  6,  'EMP-006', '2022-09-01', 'CNI-006', 'Licence', 'Anglais',                   true, NOW(), NOW()),
(7,  7,  'EMP-007', '2016-09-01', 'CNI-007', 'Master',  'Mathematiques',             true, NOW(), NOW()),
(8,  8,  'EMP-008', '2020-09-01', 'CNI-008', 'Licence', 'Francais',                  true, NOW(), NOW()),
(9,  9,  'EMP-009', '2018-09-01', 'CNI-009', 'Licence', 'SVT',                       true, NOW(), NOW()),
(10, 10, 'EMP-010', '2021-09-01', 'CNI-010', 'Licence', 'Anglais',                   true, NOW(), NOW()),
(11, 11, 'EMP-011', '2020-03-01', 'CNI-011', 'BTS',     'Comptabilite',              true, NOW(), NOW()),
(12, 12, 'EMP-012', '2022-06-01', 'CNI-012', 'BTS',     'Secretariat',               true, NOW(), NOW()),
(13, 13, 'EMP-013', '2014-09-01', 'CNI-013', 'Licence', 'Informatique',              true, NOW(), NOW()),
(14, 14, 'EMP-014', '2023-01-01', 'CNI-014', 'BTS',     'Accueil et secretariat',    true, NOW(), NOW());

-- ---------------------------------------------------------------------------
-- 12. STAFF_ASSIGNMENTS
-- ---------------------------------------------------------------------------
INSERT IGNORE INTO staff_assignments (id, staff_member_id, professional_function, start_date, end_date, active, created_at, updated_at) VALUES
(1,  1,  'DIRECTOR',      '2015-09-01', NULL, true, NOW(), NOW()),
(2,  2,  'SECRETARY',     '2018-09-01', NULL, true, NOW(), NOW()),
(3,  3,  'TEACHER',       '2020-09-01', NULL, true, NOW(), NOW()),
(4,  4,  'TEACHER',       '2021-09-01', NULL, true, NOW(), NOW()),
(5,  5,  'TEACHER',       '2019-09-01', NULL, true, NOW(), NOW()),
(6,  6,  'TEACHER',       '2022-09-01', NULL, true, NOW(), NOW()),
(7,  7,  'TEACHER',       '2016-09-01', NULL, true, NOW(), NOW()),
(8,  8,  'TEACHER',       '2020-09-01', NULL, true, NOW(), NOW()),
(9,  9,  'TEACHER',       '2018-09-01', NULL, true, NOW(), NOW()),
(10, 10, 'TEACHER',       '2021-09-01', NULL, true, NOW(), NOW()),
(11, 11, 'ACCOUNTANT',    '2020-03-01', NULL, true, NOW(), NOW()),
(12, 12, 'SECRETARY',     '2022-06-01', NULL, true, NOW(), NOW()),
(13, 13, 'TEACHER',       '2014-09-01', NULL, true, NOW(), NOW()),
(14, 14, 'SECRETARY',     '2023-01-01', NULL, true, NOW(), NOW());

-- ---------------------------------------------------------------------------
-- 13. CLASSES (liees a l'annee scolaire active 2026-2027)
-- ---------------------------------------------------------------------------
INSERT IGNORE INTO classes (id, name_classe, niveau, capacite, section_id, academic_year_id, staff_member_id, description, active, created_at, updated_at) VALUES
-- Maternelle
(1,  'PS',  'Petite Section',       25, 1, 1, 8,  'PS - Petite Section',                true, NOW(), NOW()),
(2,  'MS',  'Moyenne Section',      25, 1, 1, 8,  'MS - Moyenne Section',               true, NOW(), NOW()),
(3,  'GS',  'Grande Section',       25, 1, 1, 8,  'GS - Grande Section',                true, NOW(), NOW()),
-- Primaire
(4,  'CP',  'Cours Preparatoire',   30, 2, 1, 4,  'CP - Cours Preparatoire',            true, NOW(), NOW()),
(5,  'CE1', 'Cours Elementaire 1',  30, 2, 1, 4,  'CE1 - Cours Elementaire 1',          true, NOW(), NOW()),
(6,  'CE2', 'Cours Elementaire 2',  30, 2, 1, 8,  'CE2 - Cours Elementaire 2',          true, NOW(), NOW()),
(7,  'CM1', 'Cours Moyen 1',        30, 2, 1, 4,  'CM1 - Cours Moyen 1',                true, NOW(), NOW()),
(8,  'CM2', 'Cours Moyen 2',        30, 2, 1, 8,  'CM2 - Cours Moyen 2',                true, NOW(), NOW()),
-- Secondaire
(9,  '6eme','Sixieme',              35, 3, 1, 3,  '6eme - Sixieme',                     true, NOW(), NOW()),
(10, '5eme','Cinquieme',            35, 3, 1, 5,  '5eme - Cinquieme',                   true, NOW(), NOW()),
(11, '4eme','Quatrieme',            35, 3, 1, 3,  '4eme - Quatrieme',                   true, NOW(), NOW()),
(12, '3eme','Troisieme',            35, 3, 1, 5,  '3eme - Troisieme',                   true, NOW(), NOW()),
(13, '2nde','Seconde',              35, 3, 1, 6,  '2nde - Seconde',                     true, NOW(), NOW()),
(14, '1re', 'Premiere',             35, 3, 1, 7,  '1ere - Premiere',                    true, NOW(), NOW()),
(15, 'Tle', 'Terminale',            35, 3, 1, 7,  'Tle - Terminale',                    true, NOW(), NOW());

-- ---------------------------------------------------------------------------
-- 14. TRIMESTRES et SEQUENCES (annee 2026-2027)
-- ---------------------------------------------------------------------------
INSERT IGNORE INTO trimestre (id, libelle_trimestre, academic_year_id, active, created_at, updated_at) VALUES
(1, 'Trimestre 1', 1, true, NOW(), NOW()),
(2, 'Trimestre 2', 1, true, NOW(), NOW()),
(3, 'Trimestre 3', 1, true, NOW(), NOW());

INSERT IGNORE INTO sequence (id, libelle_sequence, trimestre_id, active, created_at, updated_at) VALUES
(1, 'Sequence 1', 1, true, NOW(), NOW()),
(2, 'Sequence 2', 1, true, NOW(), NOW()),
(3, 'Sequence 3', 2, true, NOW(), NOW()),
(4, 'Sequence 4', 2, true, NOW(), NOW()),
(5, 'Sequence 5', 3, true, NOW(), NOW()),
(6, 'Sequence 6', 3, true, NOW(), NOW());

-- ---------------------------------------------------------------------------
-- 15. AFFECTATIONS (enseignants -> classes / matieres)
-- ---------------------------------------------------------------------------
INSERT IGNORE INTO affectation (id, academic_year_id, classe_id, subject_id, staff_member_id, type_code, date_debut, date_fin, created_at, updated_at) VALUES
(1,  1, 9,  1, 3, 'TITULAIRE', '2026-09-01', NULL, NOW(), NOW()),
(2,  1, 10, 1, 3, 'TITULAIRE', '2026-09-01', NULL, NOW(), NOW()),
(3,  1, 4,  2, 4, 'TITULAIRE', '2026-09-01', NULL, NOW(), NOW()),
(4,  1, 5,  2, 4, 'TITULAIRE', '2026-09-01', NULL, NOW(), NOW()),
(5,  1, 7,  2, 4, 'TITULAIRE', '2026-09-01', NULL, NOW(), NOW()),
(6,  1, 11, 5, 5, 'TITULAIRE', '2026-09-01', NULL, NOW(), NOW()),
(7,  1, 12, 5, 5, 'TITULAIRE', '2026-09-01', NULL, NOW(), NOW()),
(8,  1, 13, 3, 6, 'TITULAIRE', '2026-09-01', NULL, NOW(), NOW()),
(9,  1, 14, 3, 6, 'TITULAIRE', '2026-09-01', NULL, NOW(), NOW()),
(10, 1, 14, 1, 7, 'TITULAIRE', '2026-09-01', NULL, NOW(), NOW()),
(11, 1, 15, 1, 7, 'TITULAIRE', '2026-09-01', NULL, NOW(), NOW()),
(12, 1, 6,  2, 8, 'TITULAIRE', '2026-09-01', NULL, NOW(), NOW()),
(13, 1, 8,  2, 8, 'TITULAIRE', '2026-09-01', NULL, NOW(), NOW()),
(14, 1, 9,  5, 9, 'VACATAIRE', '2026-09-01', NULL, NOW(), NOW()),
(15, 1, 10, 5, 9, 'VACATAIRE', '2026-09-01', NULL, NOW(), NOW()),
(16, 1, 11, 5, 9, 'VACATAIRE', '2026-09-01', NULL, NOW(), NOW()),
(17, 1, 9,  3, 10, 'VACATAIRE', '2026-09-01', NULL, NOW(), NOW()),
(18, 1, 10, 3, 10, 'VACATAIRE', '2026-09-01', NULL, NOW(), NOW()),
(19, 1, 13, 8, 13, 'TITULAIRE', '2026-09-01', NULL, NOW(), NOW()),
(20, 1, 14, 8, 13, 'TITULAIRE', '2026-09-01', NULL, NOW(), NOW());

-- ---------------------------------------------------------------------------
-- 16. ELEVES (table students)
-- ---------------------------------------------------------------------------
INSERT IGNORE INTO students (id, person_id, student_number, admission_date, current_level, ecole_precedente, active, created_at, updated_at) VALUES
(1,  15, 'STU-2026-001', '2026-09-01', 'CP',    'Ecole publique de Dakar',         true, NOW(), NOW()),
(2,  16, 'STU-2026-002', '2026-09-01', 'CE1',   'Ecole publique de Dakar',         true, NOW(), NOW()),
(3,  17, 'STU-2026-003', '2026-09-01', '6eme',  'Ecole La Concorde Thies',         true, NOW(), NOW()),
(4,  18, 'STU-2026-004', '2026-09-01', '4eme',  'Ecole Normale Dakar',             true, NOW(), NOW()),
(5,  19, 'STU-2026-005', '2026-09-01', '2nde',  'Lycee SNM Saint-Louis',           true, NOW(), NOW()),
(6,  20, 'STU-2026-006', '2026-09-01', '1re',   'Lycee Faidherbe Saint-Louis',     true, NOW(), NOW()),
(7,  21, 'STU-2026-007', '2026-09-01', 'CP',    'Nouvelle inscription',            true, NOW(), NOW()),
(8,  22, 'STU-2026-008', '2026-09-01', 'CE1',   'Ecole Mousse Kaolack',            true, NOW(), NOW()),
(9,  23, 'STU-2026-009', '2026-09-01', '6eme',  'Ecole independante Ziguinhor',    true, NOW(), NOW()),
(10, 24, 'STU-2026-010', '2026-09-01', 'Tle',   'Lycee de Mbour',                  true, NOW(), NOW()),
(11, 25, 'STU-2026-011', '2026-09-01', 'CE2',   'Ecole publique Parcelles',        true, NOW(), NOW()),
(12, 26, 'STU-2026-012', '2026-09-01', '5eme',  'Ecole publique Rufisque',         true, NOW(), NOW());

-- ---------------------------------------------------------------------------
-- 17. GUARDIANS
-- ---------------------------------------------------------------------------
INSERT IGNORE INTO guardians (id, person_id, occupation, active, created_at, updated_at) VALUES
(1,  27, 'Commercante',     true, NOW(), NOW()),
(2,  28, 'Enseignant',      true, NOW(), NOW()),
(3,  29, 'Ingenieur',       true, NOW(), NOW()),
(4,  30, 'Avocate',         true, NOW(), NOW()),
(5,  31, 'Medecin',         true, NOW(), NOW()),
(6,  32, 'Comptable',       true, NOW(), NOW()),
(7,  33, 'Infirmiere',      true, NOW(), NOW()),
(8,  34, 'Commercant',      true, NOW(), NOW()),
(9,  35, 'Institutrice',    true, NOW(), NOW()),
(10, 36, 'Chef d''entreprise', true, NOW(), NOW()),
(11, 37, 'Architecte',      true, NOW(), NOW()),
(12, 38, 'Avocat',          true, NOW(), NOW());

-- ---------------------------------------------------------------------------
-- 18. STUDENT_GUARDIANS (liens eleves <-> tuteurs)
-- ---------------------------------------------------------------------------
INSERT IGNORE INTO student_guardians (id, student_id, guardian_id, relationship_type, relationship_details, primary_contact, financial_responsible, emergency_contact, active, created_at, updated_at) VALUES
(1,  1,  1,  'MOTHER',  'Mere d''Awa Diop',              true, true,  true,  true, NOW(), NOW()),
(2,  2,  2,  'FATHER',  'Pere de Mamadou Ndiaye',        true, true,  true,  true, NOW(), NOW()),
(3,  3,  3,  'FATHER',  'Pere de Fatou Sarr',            true, true,  true,  true, NOW(), NOW()),
(4,  4,  4,  'MOTHER',  'Mere d''Ibrahima Fall',         true, true,  true,  true, NOW(), NOW()),
(5,  5,  5,  'FATHER',  'Pere d''Aminata Gueye',         true, true,  true,  true, NOW(), NOW()),
(6,  6,  6,  'MOTHER',  'Mere de Babacar Ba',            true, true,  true,  true, NOW(), NOW()),
(7,  7,  7,  'MOTHER',  'Mere d''Amadou Kane',           true, true,  true,  true, NOW(), NOW()),
(8,  8,  8,  'FATHER',  'Pere de Salimata Sy',           true, true,  true,  true, NOW(), NOW()),
(9,  9,  9,  'MOTHER',  'Mere d''Oumar Cisse',           true, true,  true,  true, NOW(), NOW()),
(10, 10, 10, 'FATHER',  'Pere d''Aissatou Mbaye',        true, true,  true,  true, NOW(), NOW()),
(11, 11, 11, 'MOTHER',  'Mere de Moussa Diallo',         true, true,  true,  true, NOW(), NOW()),
(12, 12, 12, 'FATHER',  'Pere de Ndeye Thiam',           true, true,  true,  true, NOW(), NOW());

-- ---------------------------------------------------------------------------
-- 19. PRE_INSCRIPTIONS (pre-inscriptions)
-- ---------------------------------------------------------------------------
INSERT IGNORE INTO pre_enrollments (id, number, first_name, last_name, gender, birth_date, birth_place, requested_level, required_fee, academic_year_id, status, fee_payment_reference, submitted_at, reviewed_by, reviewed_at, administrative_comment, rejection_reason, active, created_at, updated_at) VALUES
(1, 'PRE-2026-001', 'Amadou',     'Kane',     'MASCULIN', '2016-08-14', 'Dakar',     'CP',     25000.00, 1, 'APPROVED',          'FEE-REF-001', '2026-07-10 09:00:00.000000', 1, '2026-07-15 14:00:00.000000', 'Dossier complet et valide', NULL, true, NOW(), NOW()),
(2, 'PRE-2026-002', 'Salimata',   'Sy',       'FEMININ',  '2015-05-03', 'Kaolack',   'CE1',    25000.00, 1, 'SUBMITTED',         'FEE-REF-002', '2026-07-12 10:30:00.000000', NULL, NULL, NULL, NULL, true, NOW(), NOW()),
(3, 'PRE-2026-003', 'Oumar',      'Cisse',    'MASCULIN', '2013-12-20', 'Ziguinhor', '6eme',   35000.00, 1, 'UNDER_REVIEW',      'FEE-REF-003', '2026-07-14 08:15:00.000000', 1, '2026-07-16 11:00:00.000000', 'En cours d''examen', NULL, true, NOW(), NOW()),
(4, 'PRE-2026-004', 'Aissatou',   'Mbaye',    'FEMININ',  '2008-04-17', 'Mbour',     '2nde',   35000.00, 1, 'DRAFT',             NULL,         NULL,                       NULL, NULL, NULL, NULL, true, NOW(), NOW()),
(5, 'PRE-2026-005', 'Moussa',     'Diallo',   'MASCULIN', '2014-09-05', 'Dakar',     'CE2',    25000.00, 1, 'CANCELLED',         'FEE-REF-005', '2026-07-08 14:00:00.000000', 1, '2026-07-09 10:00:00.000000', NULL, 'Dossier incomplet', true, NOW(), NOW()),
(6, 'PRE-2026-006', 'Ndeye',      'Thiam',    'FEMININ',  '2012-06-18', 'Rufisque',  '5eme',   35000.00, 1, 'APPROVED',          'FEE-REF-006', '2026-07-05 09:30:00.000000', 1, '2026-07-11 16:00:00.000000', 'Dossier valide', NULL, true, NOW(), NOW()),
(7, 'PRE-2026-007', 'Ibrahima',   'Mendy',    'MASCULIN', '2017-01-22', 'Dakar',     'PS',     15000.00, 1, 'EXPIRED',           NULL,         '2026-04-01 10:00:00.000000', NULL, NULL, NULL, NULL, true, NOW(), NOW()),
(8, 'PRE-2026-008', 'Fatima',     'Sow',      'FEMININ',  '2010-10-10', 'Thies',     '3eme',   35000.00, 1, 'REJECTED',          'FEE-REF-008', '2026-07-01 08:00:00.000000', 1, '2026-07-06 15:00:00.000000', NULL, 'Age non conforme au niveau demande', true, NOW(), NOW()),
(9, 'PRE-2026-009', 'Awa',        'Diop',     'FEMININ',  '2016-03-12', 'Dakar',     'CP',     25000.00, 1, 'APPROVED',          'FEE-REF-009', '2026-08-20 10:00:00.000000', 1, '2026-08-25 11:00:00.000000', 'Reinscription confirmee', NULL, true, NOW(), NOW()),
(10,'PRE-2026-010', 'Mamadou',    'Ndiaye',   'MASCULIN', '2015-07-25', 'Dakar',     'CE1',    25000.00, 1, 'APPROVED',          'FEE-REF-010', '2026-08-20 10:30:00.000000', 1, '2026-08-25 11:00:00.000000', 'Reinscription confirmee', NULL, true, NOW(), NOW()),
(11,'PRE-2026-011', 'Fatou',      'Sarr',     'FEMININ',  '2013-01-09', 'Thies',     '6eme',   35000.00, 1, 'APPROVED',          'FEE-REF-011', '2026-08-21 09:00:00.000000', 1, '2026-08-25 11:00:00.000000', 'Reinscription confirmee', NULL, true, NOW(), NOW()),
(12,'PRE-2026-012', 'Ibrahima',   'Fall',     'MASCULIN', '2011-11-03', 'Dakar',     '4eme',   35000.00, 1, 'SUBMITTED',         'FEE-REF-012', '2026-08-22 14:00:00.000000', NULL, NULL, NULL, NULL, true, NOW(), NOW()),
(13,'PRE-2026-013', 'Aminata',    'Gueye',    'FEMININ',  '2009-05-19', 'Saint-Louis','2nde',   35000.00, 1, 'APPROVED',          'FEE-REF-013', '2026-08-20 11:00:00.000000', 1, '2026-08-25 11:00:00.000000', 'Reinscription confirmee', NULL, true, NOW(), NOW()),
(14,'PRE-2026-014', 'Babacar',    'Ba',       'MASCULIN', '2007-02-28', 'Dakar',     '1re',    35000.00, 1, 'APPROVED',          'FEE-REF-014', '2026-08-20 12:00:00.000000', 1, '2026-08-25 11:00:00.000000', 'Reinscription confirmee', NULL, true, NOW(), NOW()),
(15,'PRE-2026-015', 'Aissatou',   'Mbaye',    'FEMININ',  '2008-04-17', 'Mbour',     'Tle',    35000.00, 1, 'APPROVED',          'FEE-REF-015', '2026-08-21 10:00:00.000000', 1, '2026-08-25 11:00:00.000000', 'Reinscription confirmee', NULL, true, NOW(), NOW()),
(16,'PRE-2026-016', 'Moussa',     'Diallo',   'MASCULIN', '2014-09-05', 'Dakar',     'CE2',    25000.00, 1, 'SUBMITTED',         'FEE-REF-016', '2026-08-22 15:00:00.000000', NULL, NULL, NULL, NULL, true, NOW(), NOW());

-- ---------------------------------------------------------------------------
-- 20. PRE_ENROLLMENT_DOCUMENTS
-- ---------------------------------------------------------------------------
INSERT IGNORE INTO pre_enrollment_documents (id, pre_enrollment_id, document_type, storage_reference, review_status, reviewed_by, reviewed_at, rejection_reason, submitted_at, active, created_at, updated_at) VALUES
(1,  1, 'BIRTH_CERTIFICATE', '/pre-enrollments/1/birth-certificate.pdf', 'APPROVED',    1, '2026-07-15 14:00:00.000000', NULL, '2026-07-10 09:00:00.000000', true, NOW(), NOW()),
(2,  1, 'REPORT_CARD',      '/pre-enrollments/1/report-card.pdf',      'APPROVED',    1, '2026-07-15 14:00:00.000000', NULL, '2026-07-10 09:00:00.000000', true, NOW(), NOW()),
(3,  1, 'PHOTO_ELEVE',      '/pre-enrollments/1/photo.jpg',            'APPROVED',    1, '2026-07-15 14:00:00.000000', NULL, '2026-07-10 09:00:00.000000', true, NOW(), NOW()),
(4,  2, 'BIRTH_CERTIFICATE', '/pre-enrollments/2/birth-certificate.pdf', 'SUBMITTED',   NULL, NULL, NULL, '2026-07-12 10:30:00.000000', true, NOW(), NOW()),
(5,  2, 'REPORT_CARD',      '/pre-enrollments/2/report-card.pdf',      'SUBMITTED',   NULL, NULL, NULL, '2026-07-12 10:30:00.000000', true, NOW(), NOW()),
(6,  3, 'BIRTH_CERTIFICATE', '/pre-enrollments/3/birth-certificate.pdf', 'SUBMITTED',   1, '2026-07-16 11:00:00.000000', NULL, '2026-07-14 08:15:00.000000', true, NOW(), NOW()),
(7,  3, 'REPORT_CARD',      '/pre-enrollments/3/report-card.pdf',      'SUBMITTED',   1, '2026-07-16 11:00:00.000000', NULL, '2026-07-14 08:15:00.000000', true, NOW(), NOW()),
(8,  6, 'BIRTH_CERTIFICATE', '/pre-enrollments/6/birth-certificate.pdf', 'APPROVED',    1, '2026-07-11 16:00:00.000000', NULL, '2026-07-05 09:30:00.000000', true, NOW(), NOW()),
(9,  6, 'REPORT_CARD',      '/pre-enrollments/6/report-card.pdf',      'APPROVED',    1, '2026-07-11 16:00:00.000000', NULL, '2026-07-05 09:30:00.000000', true, NOW(), NOW()),
(10, 6, 'CNI_PARENT',       '/pre-enrollments/6/cni-parent.pdf',       'APPROVED',    1, '2026-07-11 16:00:00.000000', NULL, '2026-07-05 09:30:00.000000', true, NOW(), NOW()),
(11, 5, 'BIRTH_CERTIFICATE', '/pre-enrollments/5/birth-certificate.pdf', 'REJECTED',    1, '2026-07-09 10:00:00.000000', 'Document illisible', '2026-07-08 14:00:00.000000', true, NOW(), NOW()),
(12, 8, 'BIRTH_CERTIFICATE', '/pre-enrollments/8/birth-certificate.pdf', 'APPROVED',    1, '2026-07-06 15:00:00.000000', NULL, '2026-07-01 08:00:00.000000', true, NOW(), NOW()),
(13, 8, 'REPORT_CARD',      '/pre-enrollments/8/report-card.pdf',      'REJECTED',    1, '2026-07-06 15:00:00.000000', 'Bulletin inadapte', '2026-07-01 08:00:00.000000', true, NOW(), NOW());

-- ---------------------------------------------------------------------------
-- 21. PRE_ENROLLMENT_GUARDIANS
-- ---------------------------------------------------------------------------
INSERT IGNORE INTO pre_enrollment_guardians (id, pre_enrollment_id, first_name, last_name, email, phone_number, address, relationship_type, relationship_details, primary_contact, financial_responsible, emergency_contact, active, created_at, updated_at) VALUES
(1,  1, 'Astou',      'Kane',     'astou.kane@example.com',     '+221771234507', 'Dakar, Senegal', 'MOTHER',  'Mere d''Amadou',           true, true, true, true, NOW(), NOW()),
(2,  2, 'Moussa',     'Sy',       'moussa.sy@example.com',      '+221771234508', 'Kaolack, Senegal', 'FATHER',  'Pere de Salimata',      true, true, true, true, NOW(), NOW()),
(3,  3, 'Bineta',     'Cisse',    'bineta.cisse@example.com',   '+221771234509', 'Ziguinhor, Senegal', 'MOTHER', 'Mere d''Oumar',        true, true, true, true, NOW(), NOW()),
(4,  4, 'El Hadj',    'Mbaye',    'elhadj.mbaye@example.com',   '+221771234510', 'Mbour, Senegal', 'FATHER',  'Pere d''Aissatou',        true, true, true, true, NOW(), NOW()),
(5,  5, 'Isabelle',   'Diallo',   'isabelle.diallo@example.com','+221771234517', 'Dakar, Senegal', 'MOTHER',  'Mere de Moussa Diallo',   true, true, true, true, NOW(), NOW()),
(6,  6, 'Serigne',    'Thiam',    'serigne.thiam@example.com',  '+221771234518', 'Rufisque, Senegal', 'FATHER', 'Pere de Ndeye Thiam', true, true, true, true, NOW(), NOW()),
(7,  7, 'Mariama',    'Mendy',    'mariama.mendy@example.com',  '+221771234519', 'Dakar, Senegal', 'MOTHER',  'Mere d''Ibrahima Mendy', true, true, true, true, NOW(), NOW()),
(8,  8, 'Abdou',      'Sow',      'abdou.sow@example.com',      '+221771234520', 'Thies, Senegal', 'FATHER',  'Pere de Fatima Sow',     true, true, true, true, NOW(), NOW());

-- ---------------------------------------------------------------------------
-- 22. PRE_ENROLLMENT_FEE_PAYMENTS
-- ---------------------------------------------------------------------------
INSERT IGNORE INTO pre_enrollment_fee_payments (id, pre_enrollment_id, amount, payment_date, transaction_reference, receipt_number, refundable, verified, active, created_at, updated_at) VALUES
(1, 1, 25000.00, '2026-07-10 09:05:00.000000', 'MOCK-TRX-KNE-001', 'REC-MOCK-TRX-KNE-001', true, true, true, NOW(), NOW()),
(2, 2, 25000.00, '2026-07-12 10:35:00.000000', 'MOCK-TRX-SYB-002', 'REC-MOCK-TRX-SYB-002', true, true, true, NOW(), NOW()),
(3, 3, 35000.00, '2026-07-14 08:20:00.000000', 'MOCK-TRX-CIS-003', 'REC-MOCK-TRX-CIS-003', true, true, true, NOW(), NOW()),
(4, 4, 35000.00, '2026-07-08 14:05:00.000000', 'MOCK-TRX-MBY-004', 'REC-MOCK-TRX-MBY-004', true, true, true, NOW(), NOW()),
(5, 6, 35000.00, '2026-07-05 09:35:00.000000', 'MOCK-TRX-THM-006', 'REC-MOCK-TRX-THM-006', true, true, true, NOW(), NOW()),
(6, 7, 15000.00, '2026-04-01 10:05:00.000000', 'MOCK-TRX-MND-007', 'REC-MOCK-TRX-MND-007', true, false, true, NOW(), NOW()),
(7, 8, 35000.00, '2026-07-01 08:05:00.000000', 'MOCK-TRX-SOW-008', 'REC-MOCK-TRX-SOW-008', true, true, true, NOW(), NOW());

-- ---------------------------------------------------------------------------
-- 23. INSCRIPTIONS (enrollments)
-- ---------------------------------------------------------------------------
INSERT IGNORE INTO enrollments (id, number, pre_enrollment_id, student_id, academic_year_id, classroom_id, enrollment_date, confirmation_date, withdrawal_date, cancellation_reason, status, type, active, created_at, updated_at) VALUES
(1, 'INS-2026-001', 1, 7,  1, 4,  '2026-09-01', '2026-09-01', NULL, NULL, 'COMPLETED', 'NEW_ADMISSION',    true, NOW(), NOW()),
(2, 'INS-2026-002', 2, 8,  1, 5,  '2026-09-01', '2026-09-01', NULL, NULL, 'COMPLETED', 'NEW_ADMISSION',    true, NOW(), NOW()),
(3, 'INS-2026-003', 3, 9,  1, 9,  '2026-09-01', '2026-09-01', NULL, NULL, 'COMPLETED', 'NEW_ADMISSION',    true, NOW(), NOW()),
(4, 'INS-2026-004', 6, 12, 1, 10, '2026-09-01', '2026-09-01', NULL, NULL, 'COMPLETED', 'REENROLLMENT',     true, NOW(), NOW()),
(5, 'INS-2026-005', 9,  1, 1, 4,  '2026-09-01', '2026-09-01', NULL, NULL, 'COMPLETED', 'NEW_ADMISSION',    true, NOW(), NOW()),
(6, 'INS-2026-006', 10, 2, 1, 5,  '2026-09-01', '2026-09-01', NULL, NULL, 'COMPLETED', 'NEW_ADMISSION',    true, NOW(), NOW()),
(7, 'INS-2026-007', 11, 3, 1, 9,  '2026-09-01', '2026-09-01', NULL, NULL, 'COMPLETED', 'NEW_ADMISSION',    true, NOW(), NOW()),
(8, 'INS-2026-008', 12, 4, 1, 11, '2026-09-01', '2026-09-01', NULL, NULL, 'COMPLETED', 'NEW_ADMISSION',    true, NOW(), NOW()),
(9, 'INS-2026-009', 13, 5, 1, 13, '2026-09-01', '2026-09-01', NULL, NULL, 'COMPLETED', 'TRANSFER',         true, NOW(), NOW()),
(10, 'INS-2026-010', 14, 6, 1, 14, '2026-09-01', '2026-09-01', NULL, NULL, 'COMPLETED', 'NEW_ADMISSION',    true, NOW(), NOW()),
(11, 'INS-2026-011', 15, 10, 1, 15, '2026-09-01', '2026-09-01', NULL, NULL, 'COMPLETED', 'REENROLLMENT',     true, NOW(), NOW()),
(12, 'INS-2026-012', 16, 11, 1, 6,  '2026-09-01', NULL, NULL, NULL, 'PENDING_CONFIRMATION', 'NEW_ADMISSION', true, NOW(), NOW());

-- ---------------------------------------------------------------------------
-- 24. MONTANTS (frais par classe)
-- ---------------------------------------------------------------------------
INSERT IGNORE INTO montant (id, classe_room_id, type_paiement, count) VALUES
(1,  1,  'FRAIS_PREINSCRIPTION', 15000.00),
(2,  2,  'FRAIS_PREINSCRIPTION', 15000.00),
(3,  3,  'FRAIS_PREINSCRIPTION', 15000.00),
(4,  4,  'FRAIS_PREINSCRIPTION', 25000.00),
(5,  5,  'FRAIS_PREINSCRIPTION', 25000.00),
(6,  6,  'FRAIS_PREINSCRIPTION', 25000.00),
(7,  7,  'FRAIS_PREINSCRIPTION', 25000.00),
(8,  8,  'FRAIS_PREINSCRIPTION', 25000.00),
(9,  9,  'FRAIS_PREINSCRIPTION', 35000.00),
(10, 10, 'FRAIS_PREINSCRIPTION', 35000.00),
(11, 11, 'FRAIS_PREINSCRIPTION', 35000.00),
(12, 12, 'FRAIS_PREINSCRIPTION', 35000.00),
(13, 13, 'FRAIS_PREINSCRIPTION', 35000.00),
(14, 14, 'FRAIS_PREINSCRIPTION', 35000.00),
(15, 15, 'FRAIS_PREINSCRIPTION', 35000.00),
(16, 4,  'FRAIS_SCOLAIRE',    50000.00),
(17, 5,  'FRAIS_SCOLAIRE',    50000.00),
(18, 9,  'FRAIS_SCOLAIRE',    75000.00),
(19, 13, 'FRAIS_SCOLAIRE',   100000.00),
(20, 14, 'FRAIS_SCOLAIRE',   100000.00),
(21, 4,  'FRAIS_INSCRIPTION', 10000.00),
(22, 9,  'FRAIS_INSCRIPTION', 15000.00),
(23, 13, 'FRAIS_INSCRIPTION', 20000.00),
(24, 4,  'FRAIS_TRANSPORT',   30000.00),
(25, 9,  'FRAIS_TRANSPORT',   40000.00),
(26, 13, 'FRAIS_TRANSPORT',   50000.00),
(27, 4,  'FRAIS_CANTINE',     25000.00),
(28, 9,  'FRAIS_CANTINE',     30000.00),
(29, 13, 'FRAIS_CANTINE',     35000.00),
(30, 9,  'FRAIS_BIBLIOTHEQUE', 5000.00),
(31, 13, 'FRAIS_BIBLIOTHEQUE', 8000.00),
(32, 4,  'FRAIS_ASSURANCE',   10000.00),
(33, 9,  'FRAIS_ASSURANCE',   12000.00),
(34, 13, 'FRAIS_ASSURANCE',   15000.00);

-- ---------------------------------------------------------------------------
-- 25. PLANS DE PAIEMENT (tuition_payment_plans)
-- ---------------------------------------------------------------------------
INSERT IGNORE INTO tuition_payment_plans (id, enrollment_id, total_amount, discount_amount, net_amount, status, active, created_at, updated_at) VALUES
(1,  1,  50000.00, 0.00,     50000.00, 'COMPLETED',  true, NOW(), NOW()),
(2,  2,  50000.00, 0.00,     50000.00, 'ACTIVE',     true, NOW(), NOW()),
(3,  3,  75000.00, 5000.00,  70000.00, 'ACTIVE',     true, NOW(), NOW()),
(4,  5,  50000.00, 0.00,     50000.00, 'COMPLETED',  true, NOW(), NOW()),
(5,  6,  50000.00, 0.00,     50000.00, 'ACTIVE',     true, NOW(), NOW()),
(6,  7,  75000.00, 0.00,     75000.00, 'DRAFT',      true, NOW(), NOW()),
(7,  8,  75000.00, 0.00,     75000.00, 'DRAFT',      true, NOW(), NOW()),
(8,  9,  100000.00, 10000.00, 90000.00, 'ACTIVE',     true, NOW(), NOW()),
(9,  10, 100000.00, 0.00,    100000.00, 'ACTIVE',     true, NOW(), NOW()),
(10, 11, 100000.00, 0.00,    100000.00, 'ACTIVE',     true, NOW(), NOW());

-- ---------------------------------------------------------------------------
-- 26. ECHEANCIERS DE PAIEMENT (payment_installments)
-- ---------------------------------------------------------------------------
INSERT IGNORE INTO payment_installments (id, plan_id, sequence_number, expected_amount, paid_amount, due_date, status, active, created_at, updated_at) VALUES
(1,  1, 1, 25000.00, 25000.00, '2026-09-15', 'PAID',    true, NOW(), NOW()),
(2,  1, 2, 25000.00, 25000.00, '2026-12-15', 'PAID',    true, NOW(), NOW()),
(3,  2, 1, 25000.00, 25000.00, '2026-09-15', 'PAID',    true, NOW(), NOW()),
(4,  2, 2, 25000.00, 10000.00, '2026-12-15', 'PARTIALLY_PAID', true, NOW(), NOW()),
(5,  3, 1, 25000.00, 25000.00, '2026-09-15', 'PAID',    true, NOW(), NOW()),
(6,  3, 2, 25000.00, 15000.00, '2026-12-15', 'PARTIALLY_PAID', true, NOW(), NOW()),
(7,  3, 3, 20000.00, 0.00,     '2027-03-15', 'PENDING', true, NOW(), NOW()),
(8,  4, 1, 25000.00, 25000.00, '2026-09-15', 'PAID',    true, NOW(), NOW()),
(9,  4, 2, 25000.00, 25000.00, '2026-12-15', 'PAID',    true, NOW(), NOW()),
(10, 5, 1, 25000.00, 25000.00, '2026-09-15', 'PAID',    true, NOW(), NOW()),
(11, 5, 2, 25000.00, 0.00,     '2026-12-15', 'OVERDUE', true, NOW(), NOW()),
(12, 8, 1, 30000.00, 30000.00, '2026-09-15', 'PAID',    true, NOW(), NOW()),
(13, 8, 2, 30000.00, 30000.00, '2026-12-15', 'PAID',    true, NOW(), NOW()),
(14, 8, 3, 30000.00, 20000.00, '2027-03-15', 'PARTIALLY_PAID', true, NOW(), NOW()),
(15, 9, 1, 50000.00, 50000.00, '2026-09-15', 'PAID',    true, NOW(), NOW()),
(16, 9, 2, 50000.00, 25000.00, '2026-12-15', 'PARTIALLY_PAID', true, NOW(), NOW()),
(17, 10, 1, 50000.00, 50000.00, '2026-09-15', 'PAID',   true, NOW(), NOW()),
(18, 10, 2, 50000.00, 0.00,     '2026-12-15', 'PENDING', true, NOW(), NOW());

-- ---------------------------------------------------------------------------
-- 27. PAIEMENTS
-- ---------------------------------------------------------------------------
INSERT IGNORE INTO paiements (id, enrollment_id, montant_id, student_id, montant_paye, montant_restant, remise, date_paiement, due_date, payment_method, receipt_number, description, type_paiement, cancellation_reason, cancelled_at, version) VALUES
(1,  1,  16, 7,  25000.00, 0.00,    0.00,    '2026-09-05', '2026-09-15', 'Especes',    'REC-2026-001', 'Frais scolaires tranche 1',  'FRAIS_SCOLAIRE', NULL, NULL, 0),
(2,  1,  16, 7,  25000.00, 0.00,    0.00,    '2026-12-10', '2026-12-15', 'Mobile Money','REC-2026-002', 'Frais scolaires tranche 2',  'FRAIS_SCOLAIRE', NULL, NULL, 0),
(3,  2,  17, 8,  25000.00, 0.00,    0.00,    '2026-09-06', '2026-09-15', 'Especes',    'REC-2026-003', 'Frais scolaires tranche 1',  'FRAIS_SCOLAIRE', NULL, NULL, 0),
(4,  2,  17, 8,  10000.00, 15000.00, 0.00,   '2026-12-12', '2026-12-15', 'Virement',   'REC-2026-004', 'Frais scolaires tranche 2 (partiel)', 'FRAIS_SCOLAIRE', NULL, NULL, 0),
(5,  3,  18, 9,  25000.00, 0.00,    0.00,    '2026-09-07', '2026-09-15', 'Especes',    'REC-2026-005', 'Frais scolaires tranche 1',  'FRAIS_SCOLAIRE', NULL, NULL, 0),
(6,  3,  18, 9,  15000.00, 10000.00, 5000.00, '2026-12-14', '2026-12-15', 'Mobile Money','REC-2026-006', 'Frais scolaires tranche 2 (partiel + remise)', 'FRAIS_SCOLAIRE', NULL, NULL, 0),
(7,  5,  16, 1,  25000.00, 0.00,    0.00,    '2026-09-08', '2026-09-15', 'Especes',    'REC-2026-007', 'Frais scolaires tranche 1',  'FRAIS_SCOLAIRE', NULL, NULL, 0),
(8,  5,  16, 1,  25000.00, 0.00,    0.00,    '2026-12-09', '2026-12-15', 'Cheque',     'REC-2026-008', 'Frais scolaires tranche 2',  'FRAIS_SCOLAIRE', NULL, NULL, 0),
(9,  6,  17, 2,  25000.00, 0.00,    0.00,    '2026-09-09', '2026-09-15', 'Especes',    'REC-2026-009', 'Frais scolaires tranche 1',  'FRAIS_SCOLAIRE', NULL, NULL, 0),
(10, 9,  19, 5,  30000.00, 0.00,    0.00,    '2026-09-10', '2026-09-15', 'Virement',   'REC-2026-010', 'Frais scolaires tranche 1',  'FRAIS_SCOLAIRE', NULL, NULL, 0),
(11, 9,  19, 5,  30000.00, 0.00,    0.00,    '2026-12-11', '2026-12-15', 'Mobile Money','REC-2026-011', 'Frais scolaires tranche 2',  'FRAIS_SCOLAIRE', NULL, NULL, 0),
(12, 9,  19, 5,  20000.00, 10000.00, 10000.00,'2027-03-10', '2027-03-15', 'Especes',    'REC-2026-012', 'Frais scolaires tranche 3 (partiel)', 'FRAIS_SCOLAIRE', NULL, NULL, 0),
(13, 10, 20, 6,  50000.00, 0.00,    0.00,    '2026-09-11', '2026-09-15', 'Virement',   'REC-2026-013', 'Frais scolaires tranche 1',  'FRAIS_SCOLAIRE', NULL, NULL, 0),
(14, 10, 20, 6,  25000.00, 25000.00, 0.00,   '2026-12-13', '2026-12-15', 'Especes',    'REC-2026-014', 'Frais scolaires tranche 2 (partiel)', 'FRAIS_SCOLAIRE', NULL, NULL, 0),
(15, 11, 20, 10, 50000.00, 0.00,    0.00,    '2026-09-12', '2026-09-15', 'Especes',    'REC-2026-015', 'Frais scolaires tranche 1',  'FRAIS_SCOLAIRE', NULL, NULL, 0),
(16, 6,  17, 2,  5000.00,  0.00,    0.00,    '2026-10-01', '2026-10-15', 'Especes',    'REC-2026-016', 'Frais de cantine',           'FRAIS_CANTINE', 'Annule par erreur de saisie', '2026-10-02 09:00:00.000000', 0);

-- ---------------------------------------------------------------------------
-- 28. NOTES (academic_grade)
-- ---------------------------------------------------------------------------
INSERT IGNORE INTO academic_grade (id, student_id, subject_id, classe_id, sequence_id, trimestre_id, grade, coefficient, assessment_date, status, period, pedagogical_comment, active, created_at, updated_at) VALUES
(1,  1,  1, 4,  1, 1, 14.50, 4, '2026-10-15 10:00:00.000000', 'VALIDATED', '2026-2027_S1', 'Bon travail en calcul mental', true, NOW(), NOW()),
(2,  1,  2, 4,  1, 1, 16.00, 3, '2026-10-15 10:00:00.000000', 'VALIDATED', '2026-2027_S1', 'Bonne maitrise de la lecture', true, NOW(), NOW()),
(3,  2,  1, 5,  1, 1, 11.00, 4, '2026-10-15 10:00:00.000000', 'VALIDATED', '2026-2027_S1', NULL, true, NOW(), NOW()),
(4,  2,  2, 5,  1, 1, 13.50, 3, '2026-10-15 10:00:00.000000', 'VALIDATED', '2026-2027_S1', 'Redaction en progres', true, NOW(), NOW()),
(5,  2,  5, 5,  1, 1, 15.00, 3, '2026-10-15 10:00:00.000000', 'VALIDATED', '2026-2027_S1', 'Curieux en sciences', true, NOW(), NOW()),
(6,  3,  1, 9,  1, 1, 09.50, 4, '2026-10-15 10:00:00.000000', 'VALIDATED', '2026-2027_S1', 'Difficultes en algebre', true, NOW(), NOW()),
(7,  3,  2, 9,  1, 1, 14.00, 3, '2026-10-15 10:00:00.000000', 'VALIDATED', '2026-2027_S1', 'Excellente expression ecrite', true, NOW(), NOW()),
(8,  3,  3, 9,  1, 1, 12.00, 2, '2026-10-15 10:00:00.000000', 'VALIDATED', '2026-2027_S1', NULL, true, NOW(), NOW()),
(9,  3,  5, 9,  1, 1, 11.50, 3, '2026-10-15 10:00:00.000000', 'VALIDATED', '2026-2027_S1', NULL, true, NOW(), NOW()),
(10, 4,  1, 11, 1, 1, 13.00, 4, '2026-10-15 10:00:00.000000', 'VALIDATED', '2026-2027_S1', 'Bon niveau general', true, NOW(), NOW()),
(11, 4,  5, 11, 1, 1, 15.50, 3, '2026-10-15 10:00:00.000000', 'VALIDATED', '2026-2027_S1', 'Excellent en SVT', true, NOW(), NOW()),
(12, 4,  2, 11, 1, 1, 12.50, 3, '2026-10-15 10:00:00.000000', 'VALIDATED', '2026-2027_S1', NULL, true, NOW(), NOW()),
(13, 5,  1, 13, 1, 1, 16.00, 4, '2026-10-15 10:00:00.000000', 'VALIDATED', '2026-2027_S1', 'Eleve brillante', true, NOW(), NOW()),
(14, 5,  3, 13, 1, 1, 17.50, 2, '2026-10-15 10:00:00.000000', 'VALIDATED', '2026-2027_S1', 'Niveau avance en anglais', true, NOW(), NOW()),
(15, 5,  5, 13, 1, 1, 14.00, 3, '2026-10-15 10:00:00.000000', 'VALIDATED', '2026-2027_S1', NULL, true, NOW(), NOW()),
(16, 5,  8, 13, 1, 1, 18.00, 1, '2026-10-15 10:00:00.000000', 'VALIDATED', '2026-2027_S1', 'Passee d''informatique', true, NOW(), NOW()),
(17, 6,  1, 14, 1, 1, 12.00, 4, '2026-10-15 10:00:00.000000', 'VALIDATED', '2026-2027_S1', NULL, true, NOW(), NOW()),
(18, 6,  2, 14, 1, 1, 14.50, 3, '2026-10-15 10:00:00.000000', 'VALIDATED', '2026-2027_S1', 'Bonne plume', true, NOW(), NOW()),
(19, 6,  3, 14, 1, 1, 15.00, 2, '2026-10-15 10:00:00.000000', 'VALIDATED', '2026-2027_S1', NULL, true, NOW(), NOW()),
(20, 6,  8, 14, 1, 1, 16.00, 1, '2026-10-15 10:00:00.000000', 'VALIDATED', '2026-2027_S1', NULL, true, NOW(), NOW()),
(21, 1,  1, 4,  2, 1, 15.00, 4, '2026-12-10 10:00:00.000000', 'DRAFT',    '2026-2027_S2', NULL, true, NOW(), NOW()),
(22, 1,  2, 4,  2, 1, 17.00, 3, '2026-12-10 10:00:00.000000', 'DRAFT',    '2026-2027_S2', NULL, true, NOW(), NOW()),
(23, 3,  1, 9,  2, 1, 10.50, 4, '2026-12-10 10:00:00.000000', 'LOCKED',   '2026-2027_S2', NULL, true, NOW(), NOW()),
(24, 3,  2, 9,  2, 1, 15.00, 3, '2026-12-10 10:00:00.000000', 'LOCKED',   '2026-2027_S2', NULL, true, NOW(), NOW()),
(25, 5,  1, 13, 2, 1, 17.00, 4, '2026-12-10 10:00:00.000000', 'DRAFT',    '2026-2027_S2', NULL, true, NOW(), NOW());

-- ---------------------------------------------------------------------------
-- 29. PRESENCES / ABSENCES
-- ---------------------------------------------------------------------------
INSERT IGNORE INTO absences (id, student_id, date, hours, justified, status, justification_note, updated_at) VALUES
(1,  1,  '2026-09-15', 4,  false, 'PENDING',    NULL,                                          NOW()),
(2,  1,  '2026-10-02', 4,  true,  'JUSTIFIED',  'Rendez-vous medical',                         NOW()),
(3,  3,  '2026-09-20', 8,  false, 'PENDING',    NULL,                                          NOW()),
(4,  3,  '2026-10-05', 4,  false, 'PENDING',    NULL,                                          NOW()),
(5,  3,  '2026-10-06', 8,  true,  'JUSTIFIED',  'Maladie - certificat medical fourni',          NOW()),
(6,  5,  '2026-09-22', 4,  true,  'JUSTIFIED',  'Participation concours inter-lycees',          NOW()),
(7,  6,  '2026-10-10', 4,  false, 'PENDING',    NULL,                                          NOW()),
(8,  6,  '2026-11-15', 8,  false, 'UNJUSTIFIED', NULL,                                         NOW()),
(9,  9,  '2026-09-25', 4,  false, 'PENDING',    NULL,                                          NOW()),
(10, 10, '2026-10-01', 4,  true,  'JUSTIFIED',  'Evenement familial',                           NOW()),
(11, 2,  '2026-11-03', 4,  false, 'PENDING',    NULL,                                          NOW()),
(12, 4,  '2026-10-18', 8,  true,  'JUSTIFIED',  'Transport en panne',                           NOW());

-- ---------------------------------------------------------------------------
-- 30. DOCUMENTS (documents generaux des eleves)
-- ---------------------------------------------------------------------------
INSERT IGNORE INTO document (id, student_id, type_document, chemin_fichier, date_ajout) VALUES
(1,  1,  'CERTIFICAT_NAISSANCE',  '/students/1/certificat-naissance.pdf',      '2026-09-01 09:00:00.000000'),
(2,  1,  'PHOTO_ELEVE',           '/students/1/photo-identite.jpg',            '2026-09-01 09:00:00.000000'),
(3,  2,  'CERTIFICAT_NAISSANCE',  '/students/2/certificat-naissance.pdf',      '2026-09-01 09:00:00.000000'),
(4,  3,  'CERTIFICAT_NAISSANCE',  '/students/3/certificat-naissance.pdf',      '2026-09-01 09:00:00.000000'),
(5,  3,  'PHOTO_ELEVE',           '/students/3/photo-identite.jpg',            '2026-09-01 09:00:00.000000'),
(6,  3,  'BULLETIN',              '/students/3/bulletin-annee-precedente.pdf',  '2026-09-01 09:00:00.000000'),
(7,  5,  'CERTIFICAT_NAISSANCE',  '/students/5/certificat-naissance.pdf',      '2026-09-01 09:00:00.000000'),
(8,  5,  'PHOTO_ELEVE',           '/students/5/photo-identite.jpg',            '2026-09-01 09:00:00.000000'),
(9,  5,  'CERTIFICAT_SCOLARITE',  '/students/5/certificat-scolarite.pdf',      '2026-09-01 09:00:00.000000'),
(10, 6,  'CERTIFICAT_NAISSANCE',  '/students/6/certificat-naissance.pdf',      '2026-09-01 09:00:00.000000'),
(11, 6,  'PHOTO_ELEVE',           '/students/6/photo-identite.jpg',            '2026-09-01 09:00:00.000000'),
(12, 6,  'BULLETIN',              '/students/6/bulletin-annee-precedente.pdf',  '2026-09-01 09:00:00.000000'),
(13, 6,  'CNI_PARENT',            '/students/6/cni-parent.pdf',                '2026-09-01 09:00:00.000000'),
(14, 10, 'CERTIFICAT_NAISSANCE',  '/students/10/certificat-naissance.pdf',     '2026-09-01 09:00:00.000000'),
(15, 10, 'BULLETIN',              '/students/10/bulletin-annee-precedente.pdf', '2026-09-01 09:00:00.000000');

-- ---------------------------------------------------------------------------
-- 31. RAPPORTS DE PROGRESSION (student_progress_report)
-- ---------------------------------------------------------------------------
INSERT IGNORE INTO student_progress_report (id, student_id, period, average_grade, attendance_rate, comments, created_at, updated_at) VALUES
(1,  1,  'Trimestre 1',  15.25, 92.5, 'Eleve assidu et motive',                   '2026-12-20 10:00:00.000000', '2026-12-20 10:00:00.000000'),
(2,  2,  'Trimestre 1',  13.17, 88.0, 'Progression reguliere',                     '2026-12-20 10:00:00.000000', '2026-12-20 10:00:00.000000'),
(3,  3,  'Trimestre 1',  11.75, 75.0, 'Quelques difficultes en mathematiques',      '2026-12-20 10:00:00.000000', '2026-12-20 10:00:00.000000'),
(4,  4,  'Trimestre 1',  13.67, 85.0, 'Bon potentiel, a encourager',                '2026-12-20 10:00:00.000000', '2026-12-20 10:00:00.000000'),
(5,  5,  'Trimestre 1',  16.38, 95.0, 'Eleve exemplaire, tres investie',            '2026-12-20 10:00:00.000000', '2026-12-20 10:00:00.000000'),
(6,  6,  'Trimestre 1',  14.38, 80.0, 'Resultats satisfaisants, continuer ainsi',   '2026-12-20 10:00:00.000000', '2026-12-20 10:00:00.000000');

-- ---------------------------------------------------------------------------
-- 32. NOTIFICATIONS
-- ---------------------------------------------------------------------------
INSERT IGNORE INTO notifications (id, user_id, message, is_read, created_at) VALUES
(1,  9,  'Le bulletin du trimestre 1 est disponible pour votre enfant Awa Diop',             false, '2026-12-21 08:00:00.000000'),
(2,  10, 'Le bulletin du trimestre 1 est disponible pour votre enfant Mamadou Ndiaye',        false, '2026-12-21 08:00:00.000000'),
(3,  1,  'Nouvelle pre-inscription en attente de validation : PRE-2026-004',                  true,  '2026-07-08 09:00:00.000000'),
(4,  1,  'Pre-inscription PRE-2026-002 soumise pour examen',                                  true,  '2026-07-12 10:35:00.000000'),
(5,  1,  'Pre-inscription PRE-2026-003 en cours d''examen',                                   true,  '2026-07-16 11:05:00.000000'),
(6,  3,  'Rappel : date limite de soumission des bulletins Sequence 2 le 15 decembre',        false, '2026-12-10 08:00:00.000000'),
(7,  7,  'Echeancier de paiement : tranche 2 de Fatou Sarr en retard',                        false, '2026-12-16 09:00:00.000000'),
(8,  8,  'Nouvel eleve inscrit : Amadou Kane en CP',                                           true,  '2026-09-01 14:00:00.000000'),
(9,  11, 'Resume mensuel des paiements de decembre disponible',                                false, '2026-12-31 08:00:00.000000'),
(10, 2,  'Convocation : reunion de conseil de classe le 20 decembre',                          false, '2026-12-15 10:00:00.000000');

-- ---------------------------------------------------------------------------
-- 33. LOGS D'ACTIVITE
-- ---------------------------------------------------------------------------
INSERT IGNORE INTO log_activite (id, user_id, action, table_cible, reference_id, ip_adresse, date_action) VALUES
(1,  1,  'LOGIN',                     'user_accounts',              1,  '192.168.1.100', '2026-09-01 08:00:00.000000'),
(2,  1,  'CREATE',                    'academic_year',              1,  '192.168.1.100', '2026-09-01 08:05:00.000000'),
(3,  8,  'CREATE',                    'enrollments',                1,  '192.168.1.105', '2026-09-01 09:30:00.000000'),
(4,  3,  'CREATE',                    'academic_grade',             1,  '192.168.1.110', '2026-10-15 10:15:00.000000'),
(5,  7,  'CREATE',                    'paiements',                  1,  '192.168.1.108', '2026-09-05 14:00:00.000000'),
(6,  1,  'UPDATE_STATUS',            'pre_enrollments',            1,  '192.168.1.100', '2026-07-15 14:05:00.000000'),
(7,  4,  'CREATE',                    'academic_grade',             10, '192.168.1.112', '2026-10-15 10:20:00.000000'),
(8,  8,  'UPLOAD_DOCUMENT',          'document',                   1,  '192.168.1.105', '2026-09-01 09:35:00.000000'),
(9,  1,  'APPROVE',                  'pre_enrollments',            6,  '192.168.1.100', '2026-07-11 16:05:00.000000'),
(10, 11, 'VERIFY_PAYMENT',           'pre_enrollment_fee_payments', 1, '192.168.1.108', '2026-07-10 09:10:00.000000'),
(11, 1,  'LOGOUT',                   'user_accounts',              1,  '192.168.1.100', '2026-09-01 17:00:00.000000'),
(12, 5,  'LOCK_GRADES',             'academic_grade',             23, '192.168.1.115', '2026-12-10 16:00:00.000000');

-- ---------------------------------------------------------------------------
-- 34. JETONS DE RAFFRAICHISSEMENT (refresh_tokens)
-- ---------------------------------------------------------------------------
INSERT IGNORE INTO refresh_tokens (id, user_id, token_hash, issued_at, expires_at, revoked_at, replaced_by_token_hash) VALUES
(1, 1, 'a1b2c3d4e5f6a7b8c9d0e1f2a3b4c5d6e7f8a9b0c1d2e3f4a5b6c7d8e9f0a1b2', '2026-09-01 08:00:00.000000', '2026-09-08 08:00:00.000000', NULL, NULL),
(2, 1, 'b2c3d4e5f6a7b8c9d0e1f2a3b4c5d6e7f8a9b0c1d2e3f4a5b6c7d8e9f0a1b2c3', '2026-09-08 08:00:00.000000', '2026-09-15 08:00:00.000000', NULL, NULL),
(3, 2, 'c3d4e5f6a7b8c9d0e1f2a3b4c5d6e7f8a9b0c1d2e3f4a5b6c7d8e9f0a1b2c3d4', '2026-09-01 09:00:00.000000', '2026-09-08 09:00:00.000000', NULL, NULL),
(4, 3, 'd4e5f6a7b8c9d0e1f2a3b4c5d6e7f8a9b0c1d2e3f4a5b6c7d8e9f0a1b2c3d4e5', '2026-09-02 10:00:00.000000', '2026-09-09 10:00:00.000000', '2026-09-05 10:00:00.000000', 'e5f6a7b8c9d0e1f2a3b4c5d6e7f8a9b0c1d2e3f4a5b6c7d8e9f0a1b2c3d4e5f6'),
(5, 3, 'e5f6a7b8c9d0e1f2a3b4c5d6e7f8a9b0c1d2e3f4a5b6c7d8e9f0a1b2c3d4e5f6', '2026-09-05 10:00:00.000000', '2026-09-12 10:00:00.000000', NULL, NULL);

-- ---------------------------------------------------------------------------
-- 35. EVENEMENTS D'AUDIT D'AUTHENTIFICATION
-- ---------------------------------------------------------------------------
INSERT IGNORE INTO authentication_audit_events (id, user_id, username, event_type, successful, client_ip, reason, created_at) VALUES
(1,  1,  'admin',       'LOGIN_SUCCESS',     true,  '192.168.1.100', NULL,                          '2026-09-01 08:00:00.000000'),
(2,  1,  'admin',       'LOGIN_SUCCESS',     true,  '192.168.1.100', NULL,                          '2026-09-02 08:00:00.000000'),
(3,  2,  'directeur',   'LOGIN_SUCCESS',     true,  '192.168.1.101', NULL,                          '2026-09-01 09:00:00.000000'),
(4,  NULL,'unknown',     'LOGIN_FAILURE',     false, '192.168.1.200', 'Mot de passe incorrect',       '2026-09-03 14:00:00.000000'),
(5,  3,  'mfall',       'LOGIN_SUCCESS',     true,  '192.168.1.110', NULL,                          '2026-09-02 08:15:00.000000'),
(6,  NULL,'hacker',      'LOGIN_FAILURE',     false, '10.0.0.1',      'Tentative de force brute',     '2026-09-05 03:00:00.000000'),
(7,  7,  'comptable',   'LOGIN_SUCCESS',     true,  '192.168.1.108', NULL,                          '2026-09-01 08:30:00.000000'),
(8,  1,  'admin',       'PASSWORD_CHANGE',   true,  '192.168.1.100', NULL,                          '2026-10-01 10:00:00.000000'),
(9,  8,  'secretaire',  'LOGIN_SUCCESS',     true,  '192.168.1.105', NULL,                          '2026-09-01 09:25:00.000000'),
(10, 11, 'prof_maths',  'LOGIN_SUCCESS',     true,  '192.168.1.111', NULL,                          '2026-09-02 08:10:00.000000');

-- ---------------------------------------------------------------------------
-- 36. FAVORIS DE RECHERCHE
-- ---------------------------------------------------------------------------
INSERT IGNORE INTO search_favorites (id, name, criteria_json, created_at) VALUES
('fav-001', 'Eleves CP Maternelle',     '{"section":"Maternelle","class":"PS","status":"active"}',                          '2026-10-01 10:00:00.000000'),
('fav-002', 'Eleves en retard paiement','{"paymentStatus":"OVERDUE","year":"2026-2027"}',                                  '2026-11-15 14:00:00.000000'),
('fav-003', 'Notes Sequence 1 Secondaire','{"section":"Secondaire","sequence":"Sequence 1","year":"2026-2027"}',          '2026-10-20 09:00:00.000000'),
('fav-004', 'Pre-inscriptions en cours',  '{"preEnrollmentStatus":"UNDER_REVIEW","year":"2026-2027"}',                    '2026-07-20 11:00:00.000000'),
('fav-005', 'Absences non justifiees',    '{"absenceStatus":"PENDING","justified":false,"year":"2026-2027"}',             '2026-10-10 08:00:00.000000');

-- ---------------------------------------------------------------------------
-- 37. TICKETS DE SUPPORT
-- ---------------------------------------------------------------------------
INSERT IGNORE INTO support_ticket (id, name, email, subject, message, priority, status, created_at) VALUES
(1, 'Mariama Diop',    'mariama.diop@example.com',     'Probleme d''acces au bulletin',
 'Je n''arrive pas a acceder au bulletin de mon enfant sur le portail parent. Le message indique "acces refuse".',
 'HIGH', 'IN_PROGRESS', '2026-12-22 09:00:00.000000'),
(2, 'Ousmane Ndiaye',  'ousmane.ndiaye@example.com',   'Erreur de paiement',
 'J''ai effectue un virement bancaire mais le paiement n''apparait toujours pas dans le systeme apres 48h.',
 'HIGH', 'OPEN', '2026-12-23 14:30:00.000000'),
(3, 'Fatou Sarr',      'fatou.sarr@example.com',       'Modification de niveau',
 'Je souhaite modifier le niveau demande pour la reincription de mon enfant de 5eme a 4eme.',
 'MEDIUM', 'IN_PROGRESS', '2026-12-20 11:00:00.000000'),
(4, 'Aly Sarr',        'aly.sarr@example.com',         'Demande de certificat de scolarite',
 'J''ai besoin d''un certificat de scolarite pour mon enfant Fatou Sarr (6eme). Merci de me l''envoyer par email.',
 'LOW', 'CLOSED', '2026-11-15 08:00:00.000000'),
(5, 'Admin Support',   'admin@ecoledesk.sn',            'Suggestion d''amelioration',
 'Il serait utile d''ajouter un module de notification par SMS pour les absences des eleves.',
 'LOW', 'OPEN', '2026-12-25 10:00:00.000000');

-- ---------------------------------------------------------------------------
-- 38. PREFERENCES DE L'APPLICATION
-- ---------------------------------------------------------------------------
INSERT IGNORE INTO app_preferences (id, school_name, school_email, school_phone, school_address, school_website, school_code,
    language, currency, date_format, time_format, start_of_week, timezone, theme,
    notify_absence, notify_grades, notify_email, notify_sms,
    session_timeout_minutes, page_size, password_min_length, require_two_factor) VALUES
(1, 'Groupe Scolaire Bahia Pontoise',
 'contact@gsbp.edu.sn', '+221338000000',
 'Route de Rufisque, Boudody, Dakar, Senegal',
 'https://www.gsbp.edu.sn', 'GSBP-DKR-001',
 'fr', 'FCFA', 'dd/MM/yyyy', '24h', 'MONDAY', 'Africa/Dakar', 'LIGHT',
 true, true, true, false,
 60, 25, 8, false);

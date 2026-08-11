-- Migration préparatoire non destructive de l'ancien modèle enseignant.
-- Teacher reste lisible tant que le code académique n'utilise pas encore staff_member_id.

ALTER TABLE staff_members
    ADD COLUMN speciality VARCHAR(100) NULL,
    ADD COLUMN level VARCHAR(50) NULL,
    ADD COLUMN cni_number VARCHAR(50) NULL,
    ADD COLUMN cni_photo_url VARCHAR(500) NULL;

-- Une identité Person est créée seulement lorsqu'aucune personne n'utilise déjà cet e-mail.
INSERT INTO persons (
    active, created_at, updated_at, first_name, last_name, email, phone,
    gender, address, photo_url
)
SELECT
    b'1', CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6),
    teacher.firstname_teacher, teacher.lastname_teacher, teacher.email,
    teacher.phone_number, teacher.gender, teacher.adress, teacher.photo_url
FROM enseignant teacher
LEFT JOIN persons person ON LOWER(person.email) = LOWER(teacher.email)
WHERE person.id IS NULL;

-- Le profil professionnel est rattaché à cette identité. Les profils déjà existants sont conservés.
INSERT INTO staff_members (
    active, employment_date, created_at, updated_at, person_id, employee_number,
    speciality, level, cni_number, cni_photo_url
)
SELECT
    b'1', COALESCE(teacher.date_embauche, CURRENT_DATE),
    CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6), person.id,
    CONCAT('TEA-', teacher.id), teacher.speciality, teacher.niveau,
    teacher.cni_number, teacher.cni_photo_url
FROM enseignant teacher
JOIN persons person ON LOWER(person.email) = LOWER(teacher.email)
LEFT JOIN staff_members staff ON staff.person_id = person.id
WHERE staff.id IS NULL;

-- Un profil déjà existant récupère les informations professionnelles manquantes.
UPDATE staff_members staff
JOIN persons person ON person.id = staff.person_id
JOIN enseignant teacher ON LOWER(teacher.email) = LOWER(person.email)
SET staff.speciality = COALESCE(staff.speciality, teacher.speciality),
    staff.level = COALESCE(staff.level, teacher.niveau),
    staff.cni_number = COALESCE(staff.cni_number, teacher.cni_number),
    staff.cni_photo_url = COALESCE(staff.cni_photo_url, teacher.cni_photo_url);

INSERT INTO staff_assignments (
    active, start_date, end_date, created_at, updated_at, staff_member_id, professional_function
)
SELECT
    b'1', COALESCE(teacher.date_embauche, CURRENT_DATE), NULL,
    CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6), staff.id, 'TEACHER'
FROM enseignant teacher
JOIN persons person ON LOWER(person.email) = LOWER(teacher.email)
JOIN staff_members staff ON staff.person_id = person.id
LEFT JOIN staff_assignments assignment
    ON assignment.staff_member_id = staff.id
   AND assignment.professional_function = 'TEACHER'
   AND assignment.end_date IS NULL
WHERE assignment.id IS NULL;

ALTER TABLE classes ADD COLUMN staff_member_id BIGINT NULL;
ALTER TABLE affectation ADD COLUMN staff_member_id BIGINT NULL;

UPDATE classes classroom
JOIN enseignant teacher ON teacher.id = classroom.teacher_id
JOIN persons person ON LOWER(person.email) = LOWER(teacher.email)
JOIN staff_members staff ON staff.person_id = person.id
SET classroom.staff_member_id = staff.id;

UPDATE affectation assignment
JOIN enseignant teacher ON teacher.id = assignment.teacher_id
JOIN persons person ON LOWER(person.email) = LOWER(teacher.email)
JOIN staff_members staff ON staff.person_id = person.id
SET assignment.staff_member_id = staff.id;

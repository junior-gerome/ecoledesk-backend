-- Canonicalise le responsable légal : Person porte l'identité, Guardian le profil,
-- et StudentGuardian porte la relation avec l'élève.

RENAME TABLE parents TO guardians;

ALTER TABLE student_guardians
    ADD COLUMN guardian_id BIGINT NULL,
    ADD COLUMN relationship_details VARCHAR(255) NULL,
    ADD COLUMN primary_contact BIT NOT NULL DEFAULT b'0',
    ADD COLUMN financial_responsible BIT NOT NULL DEFAULT b'0',
    ADD COLUMN emergency_contact BIT NOT NULL DEFAULT b'0';

-- Les anciens liens directs Person -> StudentGuardian obtiennent un profil Guardian.
INSERT INTO guardians (active, created_at, updated_at, person_id, occupation)
SELECT b'1', CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6), source.guardian_person_id, NULL
FROM (
    SELECT DISTINCT guardian_person_id
    FROM student_guardians
) source
LEFT JOIN guardians guardian ON guardian.person_id = source.guardian_person_id
WHERE guardian.id IS NULL;

UPDATE student_guardians link_table
JOIN guardians guardian ON guardian.person_id = link_table.guardian_person_id
SET link_table.guardian_id = guardian.id;

-- Les relations historiques Parent/StudentParent sont conservées comme relations Guardian.
INSERT INTO student_guardians (
    active, created_at, updated_at, guardian_id, student_id, relationship_type,
    primary_contact, financial_responsible, emergency_contact
)
SELECT
    legacy.active, legacy.created_at, legacy.updated_at, legacy.parent_id, legacy.student_id,
    legacy.relationship_type, b'1', b'0', b'0'
FROM student_parents legacy
WHERE NOT EXISTS (
    SELECT 1
    FROM student_guardians existing_link
    WHERE existing_link.student_id = legacy.student_id
      AND existing_link.guardian_id = legacy.parent_id
);

ALTER TABLE student_guardians
    DROP FOREIGN KEY FK2t9tc6xy5covs6wqevyt842r3,
    DROP INDEX uk_student_guardians_student_person,
    DROP COLUMN guardian_person_id,
    MODIFY COLUMN guardian_id BIGINT NOT NULL,
    ADD CONSTRAINT fk_student_guardians_guardian
        FOREIGN KEY (guardian_id) REFERENCES guardians (id),
    ADD CONSTRAINT uk_student_guardians_student_guardian
        UNIQUE (student_id, guardian_id);

DROP TABLE student_parents;

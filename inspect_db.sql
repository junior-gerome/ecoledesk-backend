-- Structure des tables
DESCRIBE persons;
DESCRIBE staff_members;
DESCRIBE staff_assignments;

-- Compter les lignes
SELECT 'persons' AS tbl, COUNT(*) AS nb FROM persons
UNION ALL SELECT 'staff_members', COUNT(*) FROM staff_members
UNION ALL SELECT 'staff_assignments', COUNT(*) FROM staff_assignments;

-- Apercu des enseignants (staff_members dont la fonction est TEACHER)
SELECT sm.*, sa.professional_function
FROM staff_members sm
         JOIN staff_assignments sa ON sa.staff_member_id = sm.id
WHERE sa.professional_function = 'TEACHER'
    LIMIT 5;
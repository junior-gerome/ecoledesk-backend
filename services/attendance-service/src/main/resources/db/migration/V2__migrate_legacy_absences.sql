-- Migre les pointages historiques du monolithe (bd_gsbp.absences) vers school_attendance.
-- Prerequis : MySQL heberge bd_gsbp et school_attendance (docker-compose par defaut).
-- Idempotent via INSERT IGNORE (cle unique student_id + class_id + attendance_date).

INSERT IGNORE INTO attendance_records (
    student_id,
    student_name,
    class_id,
    class_name,
    attendance_date,
    status,
    hours,
    justified,
    justification_note,
    updated_at
)
SELECT
    a.student_id,
    TRIM(CONCAT(COALESCE(s.first_name_student, ''), ' ', COALESCE(s.last_name_student, ''))),
    i.classe_room_id,
    COALESCE(c.name_classe, 'Classe'),
    a.date,
    a.status,
    COALESCE(a.hours, CASE WHEN a.status = 'PRESENT' THEN 0 ELSE 1 END),
    COALESCE(a.justified, FALSE),
    a.justification_note,
    COALESCE(a.updated_at, CURRENT_TIMESTAMP)
FROM bd_gsbp.absences a
INNER JOIN bd_gsbp.students s ON s.id = a.student_id
INNER JOIN (
    SELECT student_id, MAX(id) AS latest_inscription_id
    FROM bd_gsbp.inscriptionStudent
    GROUP BY student_id
) latest ON latest.student_id = a.student_id
INNER JOIN bd_gsbp.inscriptionStudent i ON i.id = latest.latest_inscription_id
INNER JOIN bd_gsbp.classes c ON c.id = i.classe_room_id
WHERE i.classe_room_id IS NOT NULL;

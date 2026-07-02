CREATE TABLE IF NOT EXISTS absences (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id BIGINT NOT NULL,
    date DATE NOT NULL,
    status VARCHAR(20) NOT NULL,
    hours INT NOT NULL DEFAULT 0,
    justified BOOLEAN NOT NULL DEFAULT FALSE,
    justification_note VARCHAR(500) NULL,
    updated_at DATETIME(6) NULL,
    UNIQUE KEY uk_student_date (student_id, date),
    KEY idx_absences_student (student_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

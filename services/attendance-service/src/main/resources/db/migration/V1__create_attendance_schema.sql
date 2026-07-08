CREATE TABLE attendance_records (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    student_id BIGINT NOT NULL,
    student_name VARCHAR(160) NOT NULL,
    class_id BIGINT NOT NULL,
    class_name VARCHAR(120) NOT NULL,
    attendance_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL,
    hours DECIMAL(5, 2) NOT NULL DEFAULT 0,
    justified BOOLEAN NOT NULL DEFAULT FALSE,
    justification_note VARCHAR(1000),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_attendance_student_class_date (student_id, class_id, attendance_date),
    INDEX idx_attendance_class_date (class_id, attendance_date),
    INDEX idx_attendance_student_id (student_id),
    INDEX idx_attendance_status (status),
    INDEX idx_attendance_justified (justified)
);

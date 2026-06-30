CREATE TABLE payments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    student_id BIGINT NOT NULL,
    student_name VARCHAR(160),
    amount DECIMAL(12, 2) NOT NULL,
    payment_date DATE NOT NULL,
    due_date DATE NOT NULL,
    type VARCHAR(50) NOT NULL,
    status VARCHAR(20) NOT NULL,
    payment_method VARCHAR(30),
    receipt_number VARCHAR(80),
    description VARCHAR(1000),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_payments_student_id (student_id),
    INDEX idx_payments_status (status),
    INDEX idx_payments_type (type),
    INDEX idx_payments_payment_date (payment_date)
);

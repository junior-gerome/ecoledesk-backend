ALTER TABLE paiements ADD COLUMN due_date DATE NULL;
ALTER TABLE paiements ADD COLUMN payment_method VARCHAR(40) NULL;
ALTER TABLE paiements ADD COLUMN receipt_number VARCHAR(80) NULL;

CREATE INDEX idx_paiements_receipt_number ON paiements (receipt_number);

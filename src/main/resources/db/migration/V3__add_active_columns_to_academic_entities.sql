-- Aligne les tables academiques avec le champ active herite de BaseEntity.
ALTER TABLE academic_grade
    ADD COLUMN active BIT NOT NULL DEFAULT b'1';

ALTER TABLE classes
    ADD COLUMN active BIT NOT NULL DEFAULT b'1';

ALTER TABLE subject
    ADD COLUMN active BIT NOT NULL DEFAULT b'1';

ALTER TABLE sequence
    ADD COLUMN active BIT NOT NULL DEFAULT b'1';

ALTER TABLE trimestre
    ADD COLUMN active BIT NOT NULL DEFAULT b'1';

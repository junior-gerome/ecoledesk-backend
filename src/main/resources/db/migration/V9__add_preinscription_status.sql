ALTER TABLE inscription_student
  ADD COLUMN statut_preinscription VARCHAR(30) NOT NULL DEFAULT 'INSCRITE',
  ADD COLUMN date_preinscription DATE NULL;

UPDATE inscription_student
SET date_preinscription = date_inscription
WHERE date_preinscription IS NULL;

-- Convert financial amount columns from integer-like values to fixed precision decimals.
ALTER TABLE paiements
  MODIFY COLUMN montant_paye DECIMAL(15,2) NULL;

ALTER TABLE paiements
  MODIFY COLUMN montant_restant DECIMAL(15,2) NULL;

ALTER TABLE paiements
  MODIFY COLUMN remise DECIMAL(15,2) NULL;

ALTER TABLE montant
  MODIFY COLUMN `count` DECIMAL(15,2) NOT NULL;

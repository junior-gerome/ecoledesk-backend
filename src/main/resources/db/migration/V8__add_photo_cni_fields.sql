-- Add photo and CNI fields to parents table
ALTER TABLE parents 
ADD COLUMN photo_url VARCHAR(500),
ADD COLUMN cni_number VARCHAR(50),
ADD COLUMN cni_photo_url VARCHAR(500);

-- Add photo and CNI fields to enseignant table
ALTER TABLE enseignant 
ADD COLUMN photo_url VARCHAR(500),
ADD COLUMN cni_number VARCHAR(50),
ADD COLUMN cni_photo_url VARCHAR(500);

-- Add photo field to students table
ALTER TABLE students 
ADD COLUMN photo_url VARCHAR(500);

-- Update TypeDocument enum values
ALTER TABLE document 
MODIFY COLUMN type_document ENUM('RECU', 'BULLETIN', 'CNI_PARENT', 'CNI_ENSEIGNANT', 'PHOTO_ELEVE', 'PHOTO_PARENT', 'PHOTO_ENSEIGNANT', 'CERTIFICAT_NAISSANCE', 'CERTIFICAT_SCOLARITE', 'AUTRE');

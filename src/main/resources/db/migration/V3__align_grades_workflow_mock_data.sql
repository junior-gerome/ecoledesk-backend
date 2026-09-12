-- ===========================================================================
-- V3 : Realignement des donnees mock V2 sur le contrat du workflow
-- "Notes et Bulletin" (menu sidebar -> grades).
--
-- Le frontend (et les services backend) attendent :
--   1. Des inscriptions actives au statut CONFIRMED
--      (GET /enrollments/by-class/{classroomId}?status=CONFIRMED).
--      V2 insérait COMPLETED -> liste des eleves vide dans le formulaire
--      de notes et le selecteur de bulletin.
--   2. Une periode de note egale au libelle du sequence envoye par l'UI
--      ("Sequence 1", "Sequence 2", ...). V2 utilisait le format "ANNEE_SN"
--      (ex : 2026-2027_S1) qui n'est retenu par aucune requete du workflow.
--
-- La regression est idempotente : re-executer V3 sur une base deja traitee
-- ne modifie rien.
-- ===========================================================================

-- ---------------------------------------------------------------------------
-- 1. ENROLLMENTS : COMPLETED -> CONFIRMED (eleves actuellement inscrits)
-- ---------------------------------------------------------------------------
UPDATE enrollments
SET status = 'CONFIRMED'
WHERE status = 'COMPLETED';

-- ---------------------------------------------------------------------------
-- 2. ACADEMIC_GRADE : "ANNEE_SN" -> "Sequence N"
--    Convertit 2026-2027_S1 en "Sequence 1", 2026-2027_S2 en "Sequence 2", etc.
-- ---------------------------------------------------------------------------
UPDATE academic_grade
SET period = CONCAT('Sequence ', SUBSTRING_INDEX(period, '_S', -1))
WHERE period LIKE '%_S%';
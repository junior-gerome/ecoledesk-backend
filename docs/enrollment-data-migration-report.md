# Rapport de migration Enrollment

## Portée

La migration `V24__migrate_legacy_inscription_data_to_enrollment_aggregates.sql` est additive. Elle ne supprime ni `inscription_student`, ni `paiements`, ni `document`, ni les relations de tuteurs historiques.

## Correspondance

| Source legacy | Cible | Règle |
|---|---|---|
| `inscription_student` + `students.person_id` | `pre_enrollments` | Numéro déterministe `MIG-PRE-{id}` ; statut converti vers le cycle de préinscription. |
| inscription `VALIDEE` / `INSCRITE` | `enrollments` | Inscription définitive `CONFIRMED`, numéro `MIG-ENR-{id}`. |
| `student_guardians` + `persons` | `pre_enrollment_guardians` | Copie immuable ; le premier tuteur historique devient contact principal. |
| `document` | `pre_enrollment_documents` | Uniquement pour les dossiers acceptés ; état `APPROVED` car l’historique ne porte pas de revue documentaire. |
| `paiements` de type `FRAIS_PREINSCRIPTION` | `pre_enrollment_fee_payments` | Référence traçable `MIG-PAY-{id}`, paiement vérifié, hors paiement annulé. |

## Exclusions volontaires

Aucune préinscription n’est créée lorsque l’identité de l’élève est incomplète (`first_name`, `last_name`, `birth_date` ou genre absent/non conforme). C’est nécessaire car les invariants de `PreEnrollment` interdisent les dossiers incomplets. Ces lignes restent intégralement dans le legacy et doivent être corrigées avant une migration manuelle ciblée.

Aucun échéancier n’est inféré depuis les paiements existants : l’historique ne contient ni dates d’échéance planifiées ni ventilation contractuelle fiable. Les échéanciers sont donc créés explicitement après validation métier.

## Contrôles à exécuter après V24

```sql
SELECT COUNT(*) AS legacy_eligible
FROM inscription_student i
JOIN students s ON s.id = i.student_id
JOIN persons p ON p.id = s.person_id
WHERE p.first_name IS NOT NULL AND TRIM(p.first_name) <> ''
  AND p.last_name IS NOT NULL AND TRIM(p.last_name) <> ''
  AND p.birth_date IS NOT NULL AND p.gender IN ('MASCULIN', 'FEMININ');

SELECT COUNT(*) AS migrated_pre_enrollments
FROM pre_enrollments WHERE number LIKE 'MIG-PRE-%';

SELECT COUNT(*) AS migrated_confirmed_enrollments
FROM enrollments WHERE number LIKE 'MIG-ENR-%' AND status = 'CONFIRMED';

SELECT i.id AS excluded_legacy_inscription
FROM inscription_student i
JOIN students s ON s.id = i.student_id
JOIN persons p ON p.id = s.person_id
LEFT JOIN pre_enrollments pre ON pre.number = CONCAT('MIG-PRE-', i.id)
WHERE pre.id IS NULL;
```
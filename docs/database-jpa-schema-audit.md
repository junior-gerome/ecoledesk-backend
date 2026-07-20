# Audit JPA / schema de base

Date : 2026-07-20

## Resultat

Le backend est demarre avec `spring.jpa.hibernate.ddl-auto=validate`. Flyway a applique avec succes les 22 migrations, de V1 a V21, sur `bd_gsbp`; Hibernate a ensuite initialise l'EntityManagerFactory sans erreur de validation de schema.

## Matrice JPA / schema

| Entite / VO | Table ou jointure | Mapping attendu | Etat final |
|---|---|---|---|
| BaseEntity | tables metier | `id`, `active`, `created_at`, `updated_at` | Valide |
| Person / Address | `persons` | `address`, `city`, `region`, `country`, `address_complement` | Valide apres V21 |
| Person / Email, PhoneNumber, BirthDate | `persons` | `email`, `phone`, `birth_date` | Valide |
| Student | `students` | `person_id` | Valide |
| StaffMember / EmployeeNumber | `staff_members` | `person_id`, `employee_number`, `employment_date` | Valide |
| StaffAssignment / StaffPosition | `staff_assignments` | `staff_member_id`, `professional_function`, dates | Valide |
| StudentGuardian | `student_guardians` | `student_id`, `guardian_person_id`, `relationship_type` | Valide apres V21 |
| UserAccount | `user_accounts` | `person_id`, identifiants et statut | Valide |
| UserAccount / Role | `user_roles` | `user_id`, `role_id` | Valide |
| Role / Permission | `role_permissions` | `role_id`, `permission_id` | Valide |

## Mapping explicite

Les noms de colonnes sont fixes par `@AttributeOverride` et `@JoinColumn`. En particulier, `Address` est mappe vers les cinq colonnes de `persons`; aucune colonne n'est laissee a une convention implicite. Les jointures canoniques sont `students.person_id`, `staff_members.person_id`, `staff_assignments.staff_member_id`, `student_guardians.student_id`, `student_guardians.guardian_person_id` et `user_accounts.person_id`.

`StudentGuardian` est le lien canonique direct entre un eleve et une `Person`. Les tables historiques `parents` et `student_parents` restent disponibles temporairement pour ne pas rompre les endpoints existants; V21 copie leurs relations exploitables vers le nouveau lien sans les supprimer.

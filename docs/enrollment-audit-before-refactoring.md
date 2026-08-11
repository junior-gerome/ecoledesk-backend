# Audit fonctionnel Enrollment

Audit avant modification. Inventaire: entites `Student`, `Parent`, `StudentParent`, `StudentGuardian`, `InscriptionStudent`; enums `PreinscriptionStatus`, `RelationshipType`, `TypeParent`, `Gender`; services `InscriptionStudentService`, `StudentService`, `StudentCommandService`, `StudentQueryService`, `StudentStatisticsService`, `StudentExportService`, `ParentService`; repositories correspondants; DTO/mappers/controllers exposes.

## Constat

`InscriptionStudent` melange demande, decision et inscription definitive. `POST /preinscription` appelle deja `createInscription`: il exige classe/frais/annee, controle capacite, cree `Student` et enregistre une inscription. `validatePreinscription` passe ensuite le meme objet a `VALIDEE` et journalise une confirmation, sans creer un enrollment distinct ni appliquer `INSCRITE`.

Doublons: `StudentService` et `StudentCommandService` dupliquent creation/mise a jour/archive et resolution de parent; `StudentService` et `StudentStatisticsService` dupliquent statistiques; `StudentParent/Parent` et `StudentGuardian/Person` portent le meme lien. `TypeParent` est obligatoire dans `ParentDTO` mais n'est ni persiste ni mappe, tandis que `RelationshipType` porte deja le concept. Les DTO Basic/Medium/Full ne sont pas produits par les mappers actifs.

## Decisions

Conserver/refactoriser: `Student` comme profil durable cree apres admission, `StudentGuardian`, `RelationshipType`, StudentRepository, StudentGuardianRepository, queries et exports. Fusionner StudentService/StudentCommandService et centraliser les statistiques. Supprimer apres migration: `StudentParent`, Parent/ParentRepository, StudentParentRepository, TypeParent, InscriptionStudentDTO/Mapper et l'ancien agregat `InscriptionStudent`.

## Architecture cible

`PreEnrollment` est le premier agregat: candidat ou Person provisoire, contacts/tuteurs, demande de placement, decision. Etats: DRAFT, SUBMITTED, UNDER_REVIEW, ACCEPTED, REJECTED, CANCELLED, EXPIRED. Soumission exige identite, naissance, tuteur contactable et annee; refus/annulation exigent un motif; acceptation ne cree pas automatiquement l'eleve.

`Enrollment` est le second agregat: Student, annee, classe, resultat de politique de paiement et metadonnees. Etats: PENDING_PAYMENT, CONFIRMED, CANCELLED, WITHDRAWN. Invariants: demande acceptee ou inscription directe autorisee, unicite Student/annee active, capacite reverifiee a confirmation, paiement requis selon politique, historique conserve. La confirmation cree Student et StudentGuardian si necessaire.

Services cibles: PreEnrollmentCommand/QueryService, EnrollmentCommand/QueryService, StudentProfileService et EnrollmentExportService.

## Migration

Ecrire des tests de caracterisation; ajouter tables pre_enrollments, pre_enrollment_guardians et enrollments sans suppression; migrer les donnees selon statuts valides; generaliser StudentGuardian; migrer clients; supprimer le modele legacy seulement apres reconciliation capacite et paiement.
## Complements du cahier des charges

Les flux de paiement observes sont portes par Billing (Montant, Paiement) et la preinscription utilise actuellement Montant comme frais. Aucun plan de scolarite a echeances dynamiques n'est present dans Enrollment. Les endpoints legacy /preinscription, /inscription, /students et /parents doivent rester compatibles pendant la transition. Le modele cible isole donc les frais de preinscription des futurs plans de scolarite, sans introduire de tranches enumerees.

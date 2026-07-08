# Audit fonctionnel et technique du backend

Date: 2026-07-02
Projet: `C:\Projects\Primary-School\backend`

## 1. Resume executif

Le backend permet deja de couvrir un parcours scolaire minimal: authentification, parametrage scolaire, creation eleve/parent, preinscription, inscription, paiement, absences, saisie de notes et generation de bulletin PDF.

Le verdict global est: **OUI, mais partiellement**. Le scenario preinscription -> bulletin est faisable techniquement, mais il n'est pas encore un workflow metier complet et verrouille. Plusieurs etapes existent sous forme CRUD ou service technique, sans transition metier explicite, validation administrative, publication de bulletin, affectation REST des matieres aux classes/enseignants, ni historique scolaire complet.

Build verifie: `.\gradlew.bat test` -> `BUILD SUCCESSFUL`.

## 2. Verdict global

| Question | Verdict |
|---|---|
| Peut-on faire une preinscription ? | Oui, via `POST /api/preinscription`. |
| Peut-on transformer proprement une preinscription en inscription definitive ? | Partiel: mise a jour du statut possible, mais pas de workflow dedie `valider`. |
| Peut-on creer/rattacher parent et eleve ? | Oui, dans `StudentService.createStudentWithParent`. |
| Peut-on affecter eleve a section/classe/annee ? | Oui via `InscriptionStudent` et `ClasseRoom`; section verifiee via classe. |
| Peut-on gerer paiements et restes a payer ? | Oui, via `PaymentService`. |
| Peut-on configurer matieres/classes/enseignants ? | Partiel: CRUD matieres/classes/enseignants OK; affectation matiere-classe-enseignant existe sans controller REST. |
| Peut-on saisir des notes ? | Oui, via `POST /api/grades`. |
| Peut-on calculer moyennes et rang ? | Partiel: calculs existent, mais moyennes simples, coefficients peu exploites dans bulletin. |
| Peut-on generer un bulletin PDF ? | Oui, via `GET /api/reports/bulletin/{studentId}`. |
| Pret pour Angular ? | Oui pour un MVP admin; pas encore pour un ERP scolaire professionnel complet. |
| Pret production ? | Non: workflow, tests metier, endpoints legacy, observabilite et securite applicative doivent etre finis. |

## 3. Matrice fonctionnelle

| Domaine | Fonctionnalite | Statut | Ce que le backend fait deja | Manques principaux | Fichiers preuves | Endpoints |
|---|---|---|---|---|---|---|
| Authentification | Login/register/refresh/logout | COMPLET | JWT, refresh token, audit auth, reset password | Tests controller limites | `AuthController`, `AuthentificationService`, `JwtAuthenticationFilter`, `V3`, `V17` | `/api/auth/*` |
| Utilisateurs | CRUD utilisateurs | PARTIEL | CRUD, role, statut, reset password | `getAllUsers` non pagine, endpoint `UserAccount` legacy | `UsersController`, `UsersService`, `UserAccountController` | `/api/users`, `/api/user-accounts` |
| Roles/profils/permissions | RBAC | PARTIEL | Roles, permissions, profils dynamiques | Permissions pas toujours utilisees finement, surtout roles Spring | `RoleController`, `PermissionController`, `ProfileController`, `V13` | `/api/roles`, `/api/permissions`, `/api/profiles` |
| Parents | CRUD parent/tuteur | PARTIEL | Creation/rattachement via eleve, CRUD parent | Relation multi-parent partielle mal exposee | `ParentController`, `ParentService`, `StudentParent`, `V15` | `/api/parents` |
| Eleves | CRUD eleve | COMPLET pour base | Creation avec parent, update, archive, export Excel/PDF | Historique scolaire incomplet | `StudentController`, `StudentService`, `StudentDTO` | `/api/students` |
| Preinscriptions | Creation/list/update | PARTIEL | Cree inscription avec statut `EN_ATTENTE`, eleve et parent | Pas de statut enum, pas d'endpoint validation/refus, documents non lies au dossier | `PreinscriptionController`, `InscriptionStudentService.createPreinscription`, `V9` | `/api/preinscription` |
| Inscriptions | Inscription eleve/classe/annee | PARTIEL | Creation, update, liste par classe, anti-doublon, capacite | Pas de workflow definitive, pas de contrat administratif | `InscriptionController`, `InscriptionStudent`, `V11`, `V14` | `/api/inscription` |
| Sections | CRUD section | COMPLET base | CRUD et count | Peu de regles metier | `SectionController`, `SectionService` | `/api/section` |
| Classes | CRUD classe | COMPLET base | CRUD, capacite, enseignant principal | Matiere-classe pas exposee REST | `ClassRoomController`, `ClasseRoom` | `/api/classes` |
| Annees scolaires | CRUD + active | COMPLET base | Creation, activation, inactivation; unique active en DB | Transition annuelle non automatisee | `AnneeScolaireController`, `AnneeScolaireService`, `V14` | `/api/annees-scolaires` |
| Matieres | CRUD matiere | COMPLET base | Creation, update, delete, list | Pas de coefficient par classe/matiere expose | `SubjectController`, `SubjectService` | `/api/subject` |
| Affectations | Enseignant-classe-matiere-annee | TECHNIQUE SEULEMENT | Entite/service/repository + contrainte unique | Aucun controller REST trouve | `Affectation`, `AffectationService`, `AffectationRepository`, `V4_1`, `V11` | Aucun endpoint direct |
| Enseignants | CRUD enseignant | PARTIEL | CRUD, dispo, matieres/schedule consultables | Affectation non exposable proprement | `TeacherController`, `TeacherService` | `/api/teachers` |
| Paiements | Paiement total/partiel | COMPLET base | Paiement, reste a payer, status, recu PDF, annulation | Pas d'echeancier/facturation formelle | `PaymentController`, `PaymentService`, `Paiement`, `V7`, `V11`, `V14` | `/api/payments` |
| Frais/montants | Frais par classe/type | COMPLET base | Montant par classe/type, preinscription par classe | Pas de grille annuelle avancee | `MontantController`, `MontantService`, `TypePaiement` | `/api/montant` |
| Sequences | CRUD sequence | COMPLET base | CRUD sequence | Calendrier pedagogique limite | `SequenceController` | `/api/sequence` |
| Trimestres | CRUD trimestre | COMPLET base | CRUD trimestre | Liaison workflow bulletin limitee | `TrimestreController` | `/api/trimestre` |
| Notes | Saisie/lecture/statistiques | PARTIEL | CRUD note, validation 0..20, unicite eleve-matiere-sequence-classe | Pas de validation/publication des notes, pas de verrouillage | `GradeController`, `GradeService`, `GradeCreateRequestDTO`, `V11` | `/api/grades` |
| Moyennes/rang | Calculs | PARTIEL | Moyenne, stats eleve/classe, rang | Bulletin utilise surtout moyenne simple; coefficients non aboutis | `GradeService`, `GradeCalculationService`, `GradeRepository` | `/api/grades/stats/*` |
| Absences | Pointage et resume | PARTIEL | Presence/absence/retard/excuse, justification, resume | Pas de calendrier scolaire, pas de workflow justification avance | `AttendanceController`, `Absence`, `V5` | `/api/attendance` |
| Bulletins | PDF eleve/classe ZIP | PARTIEL | Bulletin PDF, rapport classe, ZIP bulletins | Pas de validation direction, publication parent, decision conseil | `ReportController`, `ReportService`, `PdfGenerationService` | `/api/reports/bulletin/{studentId}` |
| Exports | PDF/Excel | PARTIEL | Eleves Excel/PDF, bulletins PDF, paiements PDF | Export officiel pas uniforme | `StudentExportService`, `ReportController` | `/api/students/export/*`, `/api/reports/*` |
| Audit logs | Audit reporting/auth | PARTIEL | Audit auth + logs activite consultables | Couverture metier incomplete | `AuthenticationAuditService`, `AuditController`, `LogActivite` | `/api/audit/logs` |
| Notifications | WebSocket notes | PARTIEL | Notification nouvelle note / classe | Pas de preferences, lecture parent complete | `NotificationService`, `WebSocketConfig` | WebSocket `/ws`, pas REST complet |
| Dashboard | Direction/statistiques | PARTIEL | Dashboard direction, stats students, financial/performance | KPIs encore calcules simplement | `DirectionDashboardController`, `ReportController` | `/api/dashboard/direction`, `/api/reports/*` |

## 4. Scenario preinscription -> bulletin

### Etape 1: creer une preinscription

Statut: **PARTIEL mais utilisable**.

Preuve: `PreinscriptionController` expose `POST /preinscription`; `InscriptionStudentService.createPreinscription` force `statutPreinscription = "EN_ATTENTE"`, initialise les dates, puis appelle `createInscription`.

Donnees couvertes: eleve, parent, classe, section, montant, annee scolaire.

Manques: documents non lies explicitement au dossier; statut en `String`; pas de workflow soumis/refuse/accepte.

### Etape 2: validation administrative

Statut: **PARTIEL / A CORRIGER**.

Preuve: `PUT /preinscription/{id}` et `PUT /inscription/{id}` peuvent mettre a jour le dossier. La DB connait `EN_ATTENTE`, `INSCRITE`, `ANNULEE`.

Manques: aucun endpoint `POST /preinscription/{id}/validate`; pas d'historique de decision; pas de regle "paiement preinscription obligatoire avant validation".

### Etape 3: paiement

Statut: **COMPLET base**.

Preuve: `PaymentController` expose CRUD + recu; `PaymentService.applyRequest` cherche l'eleve, sa derniere inscription, le montant de classe/type, calcule `montantRestant`, genere `receiptNumber`.

Manques: pas de facture/echeancier, pas de rapprochement comptable, pas de statut de dossier lie automatiquement au paiement.

### Etape 4: matieres et affectations

Statut: **PARTIEL**.

Preuve: `SubjectController` gere les matieres. `Affectation` et `AffectationService` modelisent enseignant + classe + matiere + annee.

Blocage: aucun controller REST d'affectation trouve; Angular ne peut pas administrer proprement ces affectations aujourd'hui.

### Etape 5: saisie des notes

Statut: **PARTIEL mais utilisable**.

Preuve: `GradeController` expose `POST /grades`, `PUT /grades/{id}`, lectures par eleve/classe; `GradeService` valide eleve, matiere, classe, sequence, note 0..20, doublon.

Manques: pas de validation de notes, pas de verrouillage apres publication, pas de bulk endpoint REST expose malgre `saveBulkGrades`.

### Etape 6: calculs

Statut: **PARTIEL**.

Preuve: `GradeService.getStudentReport`, `getStudentGradeStats`, `getClassGradeStats`, `calculateStudentRank`.

Manques: coefficients pas pleinement pris en compte dans le bulletin; moyenne annuelle/conseil non modelisee.

### Etape 7: bulletin

Statut: **PARTIEL mais export PDF utilisable**.

Preuve: `ReportController.GET /reports/bulletin/{studentId}` appelle `ReportService.generateStudentReport`, qui produit un PDF via `PdfGenerationService.generateReportCard`.

Manques: bulletin non valide/non publie; appreciation direction vide; decision finale absente; format officiel simplifie.

Conclusion scenario: **OUI, mais partiellement**. Le parcours est executable techniquement, mais pas encore un workflow ERP scolaire complet.

## 5. Endpoints disponibles, regroupes

Toutes les routes du module principal sont sous `server.servlet.context-path=/api`.

| Domaine | Endpoints principaux | Roles |
|---|---|---|
| Auth | `POST /api/auth/register`, `/login`, `/refresh`, `/logout`, `GET /auth/health` | public sauf logout authentifie par filtre selon contexte |
| Users | CRUD `/api/users`, reset password | ADMIN/AGENT/self selon endpoint |
| Roles/profils/permissions | `/api/roles`, `/api/profiles`, `/api/permissions` | ADMIN |
| Students | CRUD `/api/students`, exports, stats | ADMIN/AGENT/ENSEIGNANT lecture; ADMIN/AGENT ecriture |
| Parents | CRUD `/api/parents`, `/parents/{id}/students`, messages | ADMIN/AGENT principalement |
| Preinscription | CRUD partiel `/api/preinscription` | ADMIN/AGENT ecriture; ENSEIGNANT lecture |
| Inscription | CRUD/list/count `/api/inscription` | ADMIN/AGENT ecriture; ENSEIGNANT lecture |
| Academic | `/api/section`, `/classes`, `/annees-scolaires`, `/subject`, `/sequence`, `/trimestre`, `/teachers` | ADMIN/AGENT ecriture; ENSEIGNANT lecture |
| Grades | `/api/grades`, stats, report | ADMIN/ENSEIGNANT ecriture; ADMIN/AGENT/ENSEIGNANT lecture |
| Billing | `/api/montant`, `/api/payments`, `/payments/{id}/receipt` | ADMIN/AGENT |
| Attendance | `/api/attendance/records`, `/daily`, justification, summary | ADMIN/AGENT/ENSEIGNANT |
| Reports | `/api/reports/bulletin/{studentId}`, `/classe/{id}`, `/classe/{id}/bulletins.zip`, `/paiements`, `/performance` | selon rapport |
| Files | `/api/files/upload`, download, delete | ADMIN/AGENT; ENSEIGNANT download |
| Search | `/api/search/*` | ADMIN/AGENT/ENSEIGNANT |
| Settings/support/dashboard/audit | `/api/settings`, `/support`, `/dashboard/direction`, `/audit/logs` | variable, audit ADMIN |

Sous-modules experimentaux inclus dans Gradle:

| Module | Endpoints | Remarque |
|---|---|---|
| `services:attendance-service` | `/attendance/*` | Service separe, teste, mais duplication avec module principal attendance. |
| `services:billing-service` | `/payments/*` | Service separe, teste, mais duplication avec billing principal. |

## 6. Regles metier presentes

| Regle | Ou | Etat |
|---|---|---|
| Une preinscription doit avoir eleve, classe, montant, annee | `InscriptionStudentService.createInscription` | Complete base |
| La classe doit appartenir a la section demandee | `validateSectionConsistency` | Complete |
| Capacite de classe controlee avec verrou pessimiste | `ClasseRoomRepository.findByIdForUpdate`, `validateClassCapacity` | Bonne base |
| Un eleve ne peut pas avoir deux inscriptions actives dans la meme annee | `validateNoDuplicateEnrollment`, `V14 uk_enrollment_student_year_active` | Bonne base |
| Parent existant reutilise par email/telephone | `StudentService.resolveParentForCreate` | Partiel |
| Paiement lie a l'eleve et a sa derniere inscription | `PaymentService.applyRequest` | Fonctionnel mais simplificateur |
| Montant paye positif et pas superieur au payable | `PaymentService.applyRequest` | Complete base |
| Recu unique | `PaymentService.generateReceiptNumber`, `V11 uk_paiements_receipt_number` | Complete base |
| Note entre 0 et 20 | `GradeService.validateRequest`, `Grade` annotations | Complete base |
| Une note unique eleve/matiere/sequence/classe | `GradeService.validateRequest`, `V11 uk_grade_student_subject_sequence_class` | Complete base |
| Une seule annee scolaire active | `AnneeScolaireService`, `V14 uk_one_active_school_year` | Complete base |
| Passwords/reset tokens/refresh tokens proteges | `UsersService`, `AuthentificationService`, `V17` | Bonne base |

Regles manquantes importantes:

| Regle manquante | Impact |
|---|---|
| Validation/refus formel d'une preinscription | Impossible de tracer une decision administrative propre. |
| Paiement obligatoire avant inscription definitive | Le dossier peut etre marque inscrit sans controle financier. |
| Validation/verrouillage des notes | Notes modifiables sans cycle de publication. |
| Publication de bulletin | Pas de separation brouillon/valide/publie. |
| Coefficients par matiere/classe | Moyennes non conformes a un vrai bulletin officiel. |
| Historique scolaire annuel complet | Consultation long terme insuffisante. |

## 7. Tests

Tests presents:

| Zone | Fichiers |
|---|---|
| Securite / JWT | `JwtServiceTest`, `JwtAuthenticationFilterTest`, `AuthentificationServiceTest`, `UsersServiceTest` |
| Settings/support | `SettingsServiceTest`, `SupportServiceTest` |
| Sous-module attendance | `AttendanceApplicationServiceTest` |
| Sous-module billing | `PaymentApplicationServiceTest` |

Manque majeur: aucun test d'integration complet `preinscription -> paiement -> note -> bulletin`, peu ou pas de tests controllers metier principaux, pas de tests Flyway avec donnees representative.

## 8. Analyse securite

Points solides:

- JWT stateless avec filtre dedie.
- CORS valide en production.
- Health details a `never`.
- Reset tokens hashes.
- Refresh tokens hashes, rotation et detection reuse.
- Method security avec `@PreAuthorize`.

Risques:

- `UserAccountController` legacy expose encore des mots de passe en `@RequestParam`.
- Rate-limit reset password fail-open si Redis indisponible.
- Plusieurs endpoints utilisent seulement role large, pas permission fine.
- Audit metier incomplet hors auth.
- Upload de fichiers protege par roles, mais controle MIME/antivirus a renforcer.

## 9. Architecture cible recommandee

Conserver le monolithe modulaire DDD, avec contexts explicites:

```text
com.school.platform
  shared
  identityaccess
  enrollment
    preinscription
    inscription
    student
    parent
  academic
    schoolyear
    section
    classroom
    subject
    teacher
    assignment
  billing
    fee
    invoice
    payment
  grading
    sequence
    term
    grade
    average
    reportcard
  attendance
  reporting
  notification
```

Chaque context doit garder: `domain`, `application`, `infrastructure`, `web`. Les sous-projets `services/*` doivent rester experimentaux ou etre reintegres/retirees pour eviter une fausse architecture microservices.

## 10. Workflow cible a implementer

| Etape | Existe ? | A faire |
|---|---|---|
| Creer annee active | Oui | Tests + migration propre |
| Creer sections/classes | Oui | Regles capacite/age/niveau |
| Creer matieres | Oui | Coefficients par classe |
| Affecter matieres aux classes/enseignants | Technique seulement | Ajouter `AffectationController` REST |
| Creer preinscription | Oui partiel | Statut enum + documents + workflow |
| Valider/refuser preinscription | Non explicite | Endpoint decision + audit |
| Generer inscription definitive | Partiel | Transition controlee |
| Creer frais | Oui base | Grille annuelle/echeancier |
| Enregistrer paiements | Oui | Lier paiement au statut dossier |
| Saisir notes | Oui | Bulk endpoint + controle affectation enseignant |
| Valider notes | Non | Workflow `DRAFT/VALIDATED/LOCKED` |
| Calculer moyennes/rangs | Partiel | Coefficients, trimestre, annuel |
| Generer bulletins | Oui partiel | Validation, publication, decision |
| Exporter/imprimer | Oui PDF base | Template officiel |

## 11. Plan d'action priorise

### P0 - Rendre le parcours metier fiable

1. Ajouter `PreinscriptionStatus` enum et endpoints `validate`, `reject`, `cancel`.
2. Ajouter `AffectationController` pour enseignant-classe-matiere-annee.
3. Ajouter un workflow notes/bulletins: `DRAFT`, `VALIDATED`, `PUBLISHED`.
4. Corriger ou supprimer `UserAccountController`.

### P1 - Completer le calcul scolaire

1. Modeliser coefficient par matiere/classe/annee.
2. Recalculer moyennes ponderees par sequence/trimestre/annee.
3. Ajouter historique scolaire annuel par eleve.
4. Ajouter tests integration preinscription -> paiement -> note -> bulletin.

### P2 - Professionnaliser l'ERP

1. Factures/echeanciers.
2. Documents de dossier d'inscription.
3. Publication parent + portail parent.
4. Audit metier complet.
5. Exports officiels.

### P3 - Industrialisation

1. Observabilite metier.
2. Nettoyage legacy `com.school.gestionuser`.
3. Decision claire sur `services/*`.
4. Durcissement upload, rate-limit, permissions fines.

## 12. Conclusion finale

Le backend peut deja faire concretement: authentifier, gerer utilisateurs/roles, creer eleves/parents, creer preinscriptions/inscriptions, gerer classes/sections/annees/matieres/enseignants, enregistrer paiements, saisir notes, compter absences, generer bulletins PDF et rapports.

Il ne peut pas encore faire proprement: valider administrativement une preinscription avec workflow, garantir le paiement avant inscription, exposer l'affectation matiere-classe-enseignant, valider/verrouiller/publier les bulletins, calculer des moyennes scolaires officielles completes, fournir un historique scolaire complet.

Verdict final: **OUI, mais partiellement**. Le backend supporte un MVP Angular admin, mais il n'est pas encore pret comme ERP scolaire professionnel complet ni comme systeme de production sans les P0/P1 ci-dessus.

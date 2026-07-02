# Plan priorise de refonte, correction et amelioration totale

Date: 2026-06-29  
Projet: School Management Backend  
Objectif: repartir vers une architecture saine, compilable, maintenable, DDD/Clean/Hexagonal, prete a evoluer vers des microservices plus tard.

## 1. Reponse courte: par quoi commencer ?

Tu dois commencer par **rendre le projet compilable**.

Avant DDD, avant microservices, avant PostgreSQL, avant frontend, avant nouvelles fonctionnalites: le backend doit passer `compileJava` puis `test`.

Ordre absolu:

1. Corriger les erreurs de compilation.
2. Stabiliser l'arborescence et les packages.
3. Mettre en quarantaine le code experimental `gestion-user`.
4. Obtenir un build vert.
5. Ensuite seulement commencer la refonte architecturale.

Pourquoi ?

Parce qu'un projet qui ne compile pas ne peut pas etre audite dynamiquement, teste, deploye, securise ou refactore proprement.

## 2. Phase 0: Stabilisation urgente du build

Priorite: Critique  
Objectif: obtenir `.\gradlew.bat clean test` vert.

### Etape 0.1: Corriger `UserDetailsServiceImpl.java`

Fichier:

```text
src/main/java/com/school/management/security/UserDetailsServiceImpl.java
```

Probleme actuel:

```java
blic class UserDetailsServiceImpl implements UserDetailsService
```

Correction:

```java
public class UserDetailsServiceImpl implements UserDetailsService
```

Verifier aussi:

- supprimer les imports inutiles;
- garder uniquement le code Java;
- ne pas laisser de texte Markdown ou rapport dans `src/main/java`.

Critere de validation:

```powershell
.\gradlew.bat compileJava
```

### Etape 0.2: Corriger `ProfileAssignmentDomainService.java`

Fichier:

```text
src/main/java/com/school/management/gestion-user/domain/service/ProfileAssignmentDomainService.java
```

Probleme actuel:

```java
for (Role role : rolesToAssign) {-
```

Correction:

```java
for (Role role : rolesToAssign) {
```

Critere de validation:

```powershell
.\gradlew.bat compileJava
```

### Etape 0.3: Decider quoi faire du module `gestion-user`

Probleme:

Le dossier est dans:

```text
src/main/java/com/school/management/gestion-user
```

Mais plusieurs fichiers declarent:

```java
package com.erp.school...
```

Decision recommandee:

Mettre temporairement `gestion-user` en quarantaine tant qu'il n'est pas integre correctement.

Options:

1. Court terme: sortir `gestion-user` du source set pour retrouver un build vert.
2. Moyen terme: renommer tous les packages vers `com.school.platform.identityaccess`.
3. Long terme: transformer ce module en bounded context `identityaccess`.

Recommandation:

Commencer par l'option 1 si beaucoup d'erreurs apparaissent apres les deux corrections syntaxiques.

### Etape 0.4: Remettre la classe principale au bon endroit

Probleme observe:

```text
src/main/SchoolManagementApplication.java
```

Ce fichier est hors structure Java standard.

Chemin cible:

```text
src/main/java/com/school/SchoolManagementApplication.java
```

Ou, mieux pour la refonte:

```text
src/main/java/com/school/platform/SchoolPlatformApplication.java
```

Mais pour stabiliser vite, ne change pas encore le root package global. Remets d'abord la classe principale dans une structure valide.

### Etape 0.5: Obtenir le premier build vert

Commandes:

```powershell
.\gradlew.bat clean compileJava
.\gradlew.bat test
```

Critere de sortie de Phase 0:

- `compileJava` passe.
- Les tests se lancent.
- Les erreurs restantes sont identifiees et classees.

## 3. Phase 1: Nettoyage du depot

Priorite: Critique  
Objectif: retrouver un depot comprehensible.

### Etape 1.1: Ajouter ou corriger `.gitignore`

Ignorer:

```text
node_modules/
build/
.gradle/
uploads/
*.log
.env
```

Pourquoi:

`node_modules/` ne doit pas polluer un backend Java.

### Etape 1.2: Supprimer les fichiers parasites du backend

A verifier:

```text
q-dev-chat-2026-06-25.md
q-dev-chat1-2026-06-25.md
node_modules/
package.json
package-lock.json
```

Decision:

- garder seulement s'ils ont une utilite documentee;
- sinon les retirer du backend ou les deplacer dans un dossier dedie.

### Etape 1.3: Aligner chemins et packages Java

Regle:

Le chemin doit correspondre au package.

Exemple incorrect:

```text
src/main/java/com/school/shared/...
package com.school.management.shared;
```

Exemple correct:

```text
src/main/java/com/school/management/shared/...
package com.school.management.shared;
```

Ou:

```text
src/main/java/com/school/shared/...
package com.school.shared;
```

Choisir une seule direction.

Recommandation cible:

```text
com.school.platform
```

Mais ne fais pas ce changement global tout de suite. D'abord, stabilise l'existant.

## 4. Phase 2: Stabilisation technique du legacy

Priorite: Critique / Eleve  
Objectif: garder le metier actuel, mais rendre le code fiable.

### Etape 2.1: Unifier les exceptions

Probleme:

Il existe plusieurs familles:

```text
com.school.exception
com.school.management.shared.exceptions
```

Cible:

```text
com.school.platform.shared.exception
```

Actions:

1. Garder un seul `GlobalExceptionHandler`.
2. Garder un seul format `ApiErrorResponse`.
3. Ne jamais exposer les messages SQL bruts.

### Etape 2.2: Unifier les reponses API

Cible erreur:

```json
{
  "timestamp": "...",
  "status": 400,
  "code": "VALIDATION_ERROR",
  "message": "...",
  "details": [],
  "path": "...",
  "correlationId": "..."
}
```

Actions:

- supprimer les doublons `ErrorResponse`;
- ne plus construire de JSON a la main dans `SecurityConfig`;
- utiliser le meme format pour securite, validation, not found et erreurs metier.

### Etape 2.3: Remplacer les `Map<String,Object>` publics

Probleme:

Les controllers exposent des structures non typees.

Correction:

Creer des DTOs:

```text
AttendanceRecordResponse
DailyAttendanceRequest
DirectionDashboardResponse
PaymentSummaryResponse
```

Regle:

Aucun endpoint public ne doit retourner `Map<String,Object>` sauf cas technique exceptionnel documente.

### Etape 2.4: Enlever la logique metier des controllers

Exemple critique:

```text
AttendanceController
```

Le controller fait:

- validation;
- parsing;
- transaction;
- acces repository;
- logique metier;
- mapping response.

Cible:

```text
AttendanceController
-> AttendanceUseCase
-> AttendanceDomainService
-> AttendanceRepositoryPort
```

### Etape 2.5: Imposer la pagination

Regle:

Pas de `findAll()` sans pagination sur les donnees metier.

Actions:

- remplacer les endpoints liste par `Page<T>`;
- ajouter `Pageable`;
- imposer une taille maximum;
- utiliser des projections DTO.

## 5. Phase 3: Securite production

Priorite: Eleve  
Objectif: securiser l'authentification et les droits.

### Etape 3.1: JWT

Actions:

1. Secret obligatoire hors local.
2. Ajouter issuer.
3. Ajouter audience.
4. Ajouter rotation future avec `kid`.
5. Verifier expiration courte pour access token.

### Etape 3.2: Refresh token

Actions:

1. Stocker uniquement le hash du refresh token.
2. Rotation a chaque refresh.
3. Revocation possible.
4. Detection de reuse.

### Etape 3.3: Rate limiting

Endpoints critiques:

```text
/auth/login
/auth/refresh
/users/password-reset-request
/users/password-reset
```

Ajouter:

- limitation IP;
- limitation username/email;
- logs securite.

### Etape 3.4: Roles et permissions

Roles cibles:

```text
SUPER_ADMIN
SCHOOL_ADMIN
DIRECTION
AGENT_ADMINISTRATIF
ENSEIGNANT
PARENT
ELEVE
SUPPORT
```

Permissions cibles:

```text
student:create
student:read
student:update
student:delete
enrollment:create
grade:record
grade:publish
attendance:mark
attendance:justify
billing:invoice
billing:payment
reporting:read
settings:update
```

## 6. Phase 4: Refonte API REST

Priorite: Eleve  
Objectif: fournir une API stable au frontend.

### Etape 4.1: Versionner l'API

Cible:

```text
/api/v1
```

Exemples:

```text
/api/v1/students
/api/v1/classrooms
/api/v1/enrollments
/api/v1/grades
/api/v1/attendance-records
/api/v1/payments
```

### Etape 4.2: Normaliser les routes

Remplacer progressivement:

```text
/subject
/trimestre
/sequence
/montant
```

Par:

```text
/subjects
/trimesters
/sequences
/fees
```

Ou garder le vocabulaire francais, mais le faire partout:

```text
/matieres
/trimestres
/sequences
/montants
```

Recommandation:

Utiliser l'anglais dans le code et l'API, garder le francais dans les libelles UI.

### Etape 4.3: Documenter OpenAPI

Chaque endpoint doit avoir:

- request example;
- response example;
- codes HTTP;
- erreurs possibles;
- permissions requises.

## 7. Phase 5: Refonte DDD en modular monolith

Priorite: Eleve  
Objectif: transformer le backend en architecture metier.

### Etape 5.1: Creer le root package cible

Cible:

```text
com.school.platform
```

### Etape 5.2: Creer les bounded contexts

Ordre recommande:

1. `identityaccess`
2. `people`
3. `schoolstructure`
4. `studentlifecycle`
5. `enrollment`
6. `academic`
7. `attendance`
8. `billing`
9. `document`
10. `notification`
11. `reporting`
12. `support`

### Etape 5.3: Structure interne de chaque contexte

```text
context/
|-- domain/
|   |-- model/
|   |-- valueobject/
|   |-- service/
|   |-- event/
|-- application/
|   |-- command/
|   |-- query/
|   |-- port/in/
|   |-- port/out/
|   |-- service/
|-- infrastructure/
|   |-- persistence/
|   |-- security/
|   |-- messaging/
|-- web/
|   |-- controller/
|   |-- request/
|   |-- response/
|   |-- mapper/
```

### Etape 5.4: Regles strictes

1. `domain` ne depend pas de Spring.
2. `domain` ne depend pas de JPA.
3. `web` ne depend pas directement des repositories.
4. Les transactions sont dans les application services.
5. Les controllers ne font que HTTP mapping.
6. Les repositories JPA sont des adapters.

## 8. Phase 6: Refonte domaine par domaine

### Etape 6.1: Identity & Access

Commencer ici parce que tout le reste depend de l'utilisateur connecte.

A faire:

- nettoyer `Users`, `UserAccount`, `Role`, `Permission`;
- choisir un seul modele utilisateur;
- supprimer la duplication entre legacy et `gestion-user`;
- creer `UserAccount` comme aggregate root;
- stabiliser JWT, refresh token, roles.

### Etape 6.2: People

A faire:

- creer `Person` comme aggregate root;
- sortir les informations civiles de `Student`, `Teacher`, `Parent`;
- creer value objects `Email`, `PhoneNumber`, `Address`.

### Etape 6.3: School Structure

A faire:

- stabiliser `SchoolYear`, `Section`, `Classroom`, `Subject`, `AcademicPeriod`;
- ajouter contraintes;
- gerer le verrouillage des periodes.

### Etape 6.4: Student Lifecycle

A faire:

- creer aggregate `Student`;
- gerer matricule;
- gerer statut;
- gerer liens parents.

### Etape 6.5: Enrollment

A faire:

- separer preinscription et inscription;
- creer workflow:

```text
DRAFT -> SUBMITTED -> APPROVED -> ENROLLED
                       -> REJECTED
```

- garantir une seule inscription active par annee scolaire.

### Etape 6.6: Academic

A faire:

- creer `Grade`, `GradeBook`, `ReportCard`;
- formaliser calcul de moyenne;
- formaliser rang;
- verrouiller periode;
- publier bulletin.

### Etape 6.7: Attendance

A faire:

- choisir une seule source de verite: monolithe ou `attendance-service`;
- supprimer la duplication;
- creer `AttendanceSheet`;
- gerer justification.

### Etape 6.8: Billing

A faire:

- choisir une seule source de verite: legacy ou `billing-service`;
- creer `Invoice`, `Payment`, `Receipt`, `FeeSchedule`;
- gerer paiements partiels;
- gerer remises;
- gerer annulations;
- auditer chaque operation financiere.

## 9. Phase 7: Base de donnees

Priorite: Eleve  
Objectif: rendre le modele reproductible, coherent et performant.

### Etape 7.1: Refaire la baseline Flyway

Probleme:

`V1__baseline_school_management.sql` ne cree pas le schema complet.

Action:

Creer une baseline propre pour tout nouvel environnement.

### Etape 7.2: Ajouter les colonnes techniques standard

Pour les tables metier:

```text
id
created_at
updated_at
created_by
updated_by
version
deleted_at
```

Si multi-etablissement:

```text
school_id
```

### Etape 7.3: Ajouter les contraintes metier

Exemples:

```text
unique student_number par school_id
unique username par school_id
unique grade par student + subject + period + classroom
unique attendance par student + class + date
unique receipt_number par school_id
```

### Etape 7.4: Optimiser les indexes

Indexes prioritaires:

```text
students(school_id, student_number)
enrollments(school_id, student_id, school_year_id)
grades(school_id, classroom_id, period_id)
attendance_records(school_id, classroom_id, attendance_date)
payments(school_id, student_id, paid_at)
```

## 10. Phase 8: Tests

Priorite: Eleve  
Objectif: securiser la refonte.

### Etape 8.1: Tests unitaires domaine

Tester:

- inscription impossible deux fois dans la meme annee;
- note entre 0 et 20;
- paiement partiel;
- absence justifiee;
- periode verrouillee.

### Etape 8.2: Tests application

Tester les use cases:

```text
RegisterStudentUseCase
RecordGradeUseCase
MarkAttendanceUseCase
RecordPaymentUseCase
GenerateReportCardUseCase
```

### Etape 8.3: Tests repository

Avec Testcontainers:

- contraintes uniques;
- queries critiques;
- pagination;
- indexes indirectement par plans si possible.

### Etape 8.4: Tests API/security

Avec `@WebMvcTest`:

- auth obligatoire;
- role insuffisant -> 403;
- payload invalide -> 400;
- ressource absente -> 404.

## 11. Phase 9: Observabilite et production readiness

Priorite: Important  
Objectif: preparer la production.

Actions:

1. Ajouter correlation ID.
2. Logs JSON en production.
3. Actuator health DB/Redis.
4. Metrics par endpoint.
5. Audit logs metier.
6. Backup/restore documente.
7. Dockerfile final.
8. Profils `dev`, `test`, `prod`.
9. Secrets via variables ou secret manager.
10. Swagger protege hors local.

## 12. Phase 10: Microservices plus tard

Priorite: Optionnel / futur  
Objectif: extraire seulement quand le monolithe modulaire est stable.

Premiers candidats:

1. `billing-service`
2. `attendance-service`
3. `notification-service`
4. `reporting-service`

Conditions avant extraction:

- schema de donnees separe;
- API stable;
- events publies;
- tests contractuels;
- monitoring;
- pas de double ecriture concurrente;
- pas de logique dupliquee dans le monolithe.

## 13. Ordre final recommande

Voici l'ordre exact que je recommande.

### Bloc A: Corrections obligatoires

1. Corriger `UserDetailsServiceImpl.java`.
2. Corriger `ProfileAssignmentDomainService.java`.
3. Remettre `SchoolManagementApplication` au bon endroit.
4. Aligner packages et dossiers.
5. Mettre `gestion-user` en quarantaine si necessaire.
6. Obtenir `compileJava` vert.
7. Obtenir `test` vert.

### Bloc B: Nettoyage technique

8. Nettoyer `.gitignore`.
9. Retirer `node_modules` du backend.
10. Unifier `shared`.
11. Unifier exceptions.
12. Unifier responses API.
13. Standardiser configuration.

### Bloc C: API et securite

14. Introduire `/api/v1`.
15. Normaliser routes.
16. Remplacer les `Map<String,Object>`.
17. Ajouter pagination.
18. Finaliser JWT/refresh token.
19. Ajouter rate limiting.
20. Documenter OpenAPI.

### Bloc D: Refonte DDD

21. Creer `identityaccess`.
22. Creer `people`.
23. Creer `schoolstructure`.
24. Creer `studentlifecycle`.
25. Creer `enrollment`.
26. Creer `academic`.
27. Creer `attendance`.
28. Creer `billing`.
29. Creer `reporting`.

### Bloc E: Donnees et performance

30. Refaire baseline Flyway.
31. Ajouter contraintes et indexes.
32. Ajouter audit columns.
33. Ajouter read models.
34. Optimiser dashboards.
35. Rendre exports asynchrones.

### Bloc F: Production

36. Ajouter logs structures.
37. Ajouter correlation ID.
38. Ajouter audit trail.
39. Ajouter health checks.
40. Ajouter sauvegarde/restauration.
41. Faire tests de charge.
42. Preparer deploiement.

## 14. Definition of Done finale

Le projet pourra etre considere sain quand:

1. `.\gradlew.bat clean test` passe.
2. Les packages correspondent aux chemins.
3. Il n'y a plus de code experimental non integre.
4. Les APIs sont versionnees.
5. Les erreurs sont standardisees.
6. Les listes sont paginees.
7. Les controllers ne contiennent plus de logique metier.
8. Les bounded contexts sont explicites.
9. Les use cases sont testes.
10. Les migrations Flyway reconstruisent la base.
11. Les roles/permissions sont clairs.
12. Les operations sensibles sont auditees.
13. Les dashboards ne font pas de calculs massifs en memoire.
14. La documentation OpenAPI est fiable.
15. Le systeme est deployable en environnement propre.

## 15. Decision finale

Commence par le build.

Ensuite seulement:

```text
Build vert
-> nettoyage depot
-> stabilisation API/securite
-> modular monolith DDD
-> base de donnees propre
-> tests solides
-> production readiness
-> microservices si necessaire
```

Ne commence pas par les microservices. Ne commence pas par une grosse reecriture globale. Ne commence pas par le frontend.

Commence par rendre le backend fiable, compilable et comprehensible. C'est le socle.

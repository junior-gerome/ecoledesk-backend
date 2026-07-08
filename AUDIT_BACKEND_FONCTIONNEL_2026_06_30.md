# Audit backend fonctionnel et recommandations d'amelioration

Date: 2026-06-30  
Projet: Primary School / School Management Backend  
Objectif: rendre le backend fonctionnel, maintenable et conforme aux bonnes pratiques Java/Spring Boot, DDD, Clean Architecture et architecture hexagonale.

## 1. Synthese executive

Le backend a fortement evolue depuis les premiers audits. Le projet n'est plus seulement un monolithe organise par couches techniques `controller/service/repository/model`: il est maintenant en grande partie reorganise autour d'un package racine `com.school.platform` avec des domaines fonctionnels:

- `identityaccess`
- `academic`
- `enrollment`
- `billing`
- `attendance`
- `document`
- `notification`
- `reporting`
- `settings`
- `support`
- `staff`
- `shared`
- `search`

Deux services independants existent aussi:

- `services/billing-service`
- `services/attendance-service`

Le point le plus important: **le build passe maintenant**.

Commande executee:

```powershell
.\gradlew.bat test
```

Resultat:

```text
BUILD SUCCESSFUL
```

Cela signifie que le projet est revenu dans un etat techniquement compilable et testable. C'est une grosse amelioration par rapport aux audits precedents.

Cependant, le backend n'est pas encore pleinement pret pour une production robuste. Les principaux risques restants sont:

1. Architecture cible partiellement appliquee, mais encore melangee avec du legacy.
2. Routes REST encore heterogenes et non versionnees.
3. Beaucoup de reponses `Map<String, Object>` dans les APIs publiques.
4. `findAll()` et calculs en memoire encore presents sur des flux metier.
5. Deux sources potentielles pour `attendance` et `billing`: monolithe + services dedies.
6. `gestionuser` existe hors `com.school.platform` et n'est pas scanne par l'application principale.
7. La configuration production doit encore etre durcie.
8. Les tests passent, mais ne prouvent pas encore tout le comportement fonctionnel et securite.

Verdict:

```text
Build: OK
Architecture: en progression
Production readiness: pas encore OK
Priorite: stabiliser l'architecture modulaire et les contrats API
```

## 2. Etat actuel du projet

### 2.1 Build Gradle

Le projet est un multi-project Gradle:

```text
rootProject.name = 'school-management'
include 'services:billing-service'
include 'services:attendance-service'
```

Le build principal utilise:

- Java 21
- Spring Boot 3.5.14
- Spring Data JPA
- Spring Security
- Spring Validation
- WebSocket
- Redis
- Actuator
- Flyway
- MySQL
- MapStruct
- Lombok
- JUnit 5
- Mockito
- AssertJ
- Testcontainers declare

### 2.2 Compilation et tests

Etat verifie:

```text
compileJava: OK
test: OK
services:attendance-service:test: OK
services:billing-service:test: OK
```

Point positif:

- Les erreurs critiques precedentes (`blic class`, `{-`, packages `com.erp`) ont ete corrigees ou contournees.
- Le backend est de nouveau dans un etat executable par la chaine Gradle.

Point de vigilance:

- Les tests sont majoritairement des tests unitaires/services.
- Il manque encore des tests d'integration complets avec contexte Spring, securite, base de donnees et endpoints REST.

## 3. Audit architecture

### 3.1 Points forts

Le projet a maintenant une architecture plus lisible:

```text
src/main/java/com/school/platform/
|-- academic
|-- attendance
|-- billing
|-- document
|-- enrollment
|-- identityaccess
|-- notification
|-- reporting
|-- search
|-- settings
|-- shared
|-- staff
|-- support
```

Cette organisation est beaucoup plus proche d'un **modular monolith DDD**.

Points positifs:

- Les domaines metier commencent a etre visibles.
- Les anciens packages `com.school.management` ont ete largement remplaces.
- `shared` centralise une partie des composants communs.
- `identityaccess` regroupe la securite, les utilisateurs, roles et permissions.
- `academic`, `enrollment`, `billing`, `attendance` deviennent des domaines identifiables.
- Les services `billing-service` et `attendance-service` suivent deja un modele hexagonal plus propre.

### 3.2 Points faibles

L'architecture est encore hybride:

- Le package `com.school.platform` est modulaire, mais beaucoup de classes restent anemiques et JPA-centric.
- Les `domain/model` contiennent souvent encore des entites JPA directement.
- Les controllers appellent encore des services applicatifs legacy.
- Les repositories Spring Data sont encore tres proches de la logique applicative.
- Le module `com.school.gestionuser` est separe du scan principal.
- Le monolithe contient encore `attendance` et `billing`, alors que des services dedies existent aussi.

### 3.3 Note architecture

| Axe | Note | Commentaire |
|---|---:|---|
| Compilabilite | 8/10 | Le build passe, bon retour a la stabilite |
| Modularite | 6/10 | Decoupage par domaines en place, mais frontieres pas strictes |
| DDD | 5/10 | Bounded contexts visibles, domaine encore anemique |
| Clean Architecture | 5/10 | Separation en progression, mais JPA/Spring encore dans le coeur |
| Hexagonal Architecture | 6/10 | Bonne dans les services dedies, partielle dans le monolithe |
| Maintenabilite | 6/10 | Beaucoup mieux qu'avant, mais legacy encore present |
| Testabilite | 5/10 | Tests OK, couverture fonctionnelle insuffisante |
| Production readiness | 4/10 | Config, securite, API et data encore a durcir |

## 4. Audit du point d'entree Spring Boot

Fichier:

```text
src/main/java/com/school/platform/SchoolPlatformApplication.java
```

Code actuel:

```java
@SpringBootApplication(scanBasePackages = "com.school.platform")
public class SchoolPlatformApplication
```

Point positif:

- Le root package cible `com.school.platform` est clair.
- Le scan Spring est limite au nouveau monolithe modulaire.

Risque:

- Le module `com.school.gestionuser` n'est pas scanne par Spring.
- Il compile, mais ses beans `@Service`, `@Controller`, `@Repository` ne seront pas actifs dans l'application principale.

Decision a prendre:

1. Si `gestionuser` doit etre actif, il faut l'integrer sous `com.school.platform.identityaccess` ou ajouter explicitement son scan.
2. Si `gestionuser` est experimental, il faut l'isoler hors application principale.

Recommandation:

Ne pas ajouter simplement `com.school.gestionuser` au scan. Il faut d'abord decider si ce module remplace ou complete `identityaccess`. Sinon il y aura deux modeles d'identite concurrents.

## 5. Audit du module `gestionuser`

Le module est maintenant sous:

```text
src/main/java/com/school/gestionuser
```

Il contient:

- `domain/model`
- `domain/service`
- `domain/port/in`
- `domain/port/out`
- `application/usecase`
- `application/dto`
- `infrastructure/web`
- `infrastructure/persistence`
- `infrastructure/security`

Point positif:

- La structure est proche d'une architecture hexagonale.
- Les packages ne sont plus `com.erp.school`.
- Les erreurs syntaxiques precedentes sont corrigees.

Risque majeur:

- Le module n'est pas integre au scan Spring principal.
- Il chevauche `com.school.platform.identityaccess`.
- Il introduit une deuxieme vision de l'identite, des roles, permissions et profils.

Probleme fonctionnel potentiel:

Si le frontend appelle les endpoints de `gestionuser`, ils ne seront probablement pas disponibles dans l'application principale actuelle, car `scanBasePackages` exclut `com.school.gestionuser`.

Recommandation:

Choisir une seule strategie:

### Option A: integrer `gestionuser` dans `identityaccess`

Chemin cible:

```text
com.school.platform.identityaccess.profile
```

Avantage:

- Un seul bounded context identite.
- Pas de duplication.
- Plus simple pour la production.

Inconvenient:

- Refactoring necessaire.

### Option B: garder `gestionuser` experimental

Action:

- Le sortir du build principal ou le transformer en module Gradle separe.

Avantage:

- Le monolithe principal reste stable.

Inconvenient:

- Les fonctionnalites profile dynamique ne sont pas livrees tout de suite.

Recommandation finale:

Prendre l'option A, mais progressivement.

## 6. Audit REST API

### 6.1 Points positifs

- Les controllers sont regroupes par domaines.
- Les annotations `@PreAuthorize` sont largement presentes.
- Certains endpoints utilisent `Pageable`.
- Le context path global est `/api`.

### 6.2 Problemes restants

#### Probleme 1: routes non versionnees

Actuel:

```text
/students
/teachers
/subject
/trimestre
/montant
/attendance
/payments
```

Cible:

```text
/api/v1/students
/api/v1/teachers
/api/v1/subjects
/api/v1/trimesters
/api/v1/fees
/api/v1/attendance-records
/api/v1/payments
```

Recommandation:

Introduire `/v1` sans casser tout de suite l'existant:

```text
server.servlet.context-path=/api
controllers: /v1/students, /v1/payments, etc.
```

#### Probleme 2: routes avec `/api` dans le controller

Exemples detectes:

```text
PersonController: @RequestMapping("/api/persons")
PositionController: @RequestMapping("/api/positions")
EmployeeController: @RequestMapping("/api/employees")
```

Comme `server.servlet.context-path=/api`, cela peut donner:

```text
/api/api/persons
/api/api/positions
/api/api/employees
```

Recommandation:

Supprimer `/api` des controllers:

```java
@RequestMapping("/persons")
```

Ou mieux:

```java
@RequestMapping("/v1/persons")
```

#### Probleme 3: usage public de `Map<String, Object>`

Exemples:

- `AttendanceController`
- `PaymentController`
- `TeacherController`
- `ParentController`
- `AnalyticsService`
- `DirectionDashboardService`

Impact:

- Contrats API instables.
- OpenAPI moins fiable.
- Frontend fragile.
- Tests plus difficiles.

Recommandation:

Remplacer progressivement par des DTOs:

```text
AttendanceRecordResponse
AttendanceSummaryResponse
PaymentResponse
TeacherScheduleResponse
LinkedStudentResponse
DashboardSummaryResponse
```

## 7. Audit securite

### 7.1 Points forts

- Spring Security est actif.
- JWT stateless.
- `@EnableMethodSecurity`.
- `@PreAuthorize` present.
- BCrypt cost 12.
- Swagger autorise seulement hors profil `prod`.
- `jwt.secret` est valide par `JwtService`.

### 7.2 Risques

#### Risque 1: secret JWT de developpement par defaut

Actuel:

```properties
jwt.secret=${JWT_SECRET:dev-only-local-jwt-secret-change-me-32chars-minimum}
```

Le service verifie deja que ce fallback n'est pas utilise en `prod`, ce qui est bien.

Recommandation:

- Garder le fallback uniquement en profil local.
- Documenter explicitement `JWT_SECRET` obligatoire.
- Ajouter `issuer` et `audience`.

#### Risque 2: health details toujours visibles

Actuel:

```properties
management.endpoint.health.show-details=always
```

Risque:

- Exposition d'informations internes.

Recommandation:

```properties
management.endpoint.health.show-details=${ACTUATOR_HEALTH_DETAILS:when_authorized}
```

#### Risque 3: firewall strict desactive

Actuel:

```properties
spring.security.firewall.strict=false
```

Risque:

- Reduction de protection sur certaines URLs mal formees.

Recommandation:

- Revenir au comportement strict par defaut.
- Ne desactiver que si un besoin documente existe.

#### Risque 4: JSON d'erreur securite construit a la main

Dans `SecurityConfig`, les erreurs 401/403 sont ecrites manuellement.

Recommandation:

- Utiliser le meme format que `GlobalExceptionHandler`.
- Ajouter `correlationId`.

## 8. Audit gestion des erreurs

Point positif:

`GlobalExceptionHandler` ne renvoie plus les erreurs SQL brutes:

```java
return build(HttpStatus.CONFLICT, "Contrainte d'integrite violee");
```

C'est une amelioration importante.

Reste a ameliorer:

- Il existe encore plusieurs familles d'exceptions:
  - `com.school.platform.shared.domain.exception`
  - `com.school.platform.shared.domain.exception.shared`
  - `com.school.platform.shared.domain.exception.compat`
- Le format `ApiResponse` reste different du format 401/403 de Spring Security.

Recommandation:

Unifier vers:

```text
com.school.platform.shared.error
```

Avec:

```text
ApiError
ErrorCode
GlobalExceptionHandler
SecurityErrorWriter
```

## 9. Audit base de donnees et Flyway

### 9.1 Points forts

Le projet contient maintenant davantage de migrations:

- `V4_1__create_core_school_schema.sql`
- `V13__dynamic_user_profiles_and_roles.sql`
- `V14__ddd_audit_hardening.sql`
- `V15__create_identity_staff_links.sql`

Point positif:

- La base est plus reproductible qu'avant.
- Des contraintes et indexes metier ont ete ajoutes.
- Des tables roles/permissions sont presentes.
- Des durcissements existent pour paiements, annee scolaire et inscriptions.

### 9.2 Problemes restants

#### Probleme 1: nomenclature legacy

Exemples:

```text
enseignant
subject
classes
inscription_student
anneescolaire_id
montant
paiements
```

Ce n'est pas bloquant, mais cela complique l'ubiquitous language.

Recommandation:

Ne pas renommer toutes les tables maintenant. Stabiliser d'abord. Ensuite, creer une convention cible pour les nouvelles tables.

#### Probleme 2: absence de `school_id`

Si l'application doit gerer plusieurs etablissements, il manque une colonne de tenant:

```text
school_id
```

Sur les tables critiques:

- students
- parents
- users
- classes
- inscriptions
- grades
- paiements
- attendance_records

Recommandation:

Ne pas ajouter le multi-tenancy trop tot. D'abord stabiliser le mono-etablissement. Puis creer une migration multi-tenant.

#### Probleme 3: generated columns MySQL

`V14` utilise des colonnes generees pour contraintes partielles.

Avantage:

- Bonne technique MySQL pour simuler des indexes conditionnels.

Risque:

- Portabilite plus faible vers PostgreSQL.

Recommandation:

Si PostgreSQL devient la cible finale, planifier une migration dediee et remplacer ces patterns par des partial indexes PostgreSQL.

## 10. Audit performance

### 10.1 Points forts

- `open-in-view=false` est configure.
- Des indexes ont ete ajoutes.
- Certains services utilisent `Pageable`.
- Les exports semblent traiter par batches dans certains cas.

### 10.2 Risques

#### Risque 1: `findAll()` encore nombreux

Exemples:

- `DirectionDashboardService`
- `StudentStatisticsService`
- `InscriptionStudentService`
- `ClassRoomService`
- `AffectationService`
- `PaymentApplicationService`
- `AttendanceApplicationService`
- `MontantService`

Impact:

- Risque memoire.
- Temps de reponse degrade.
- Dashboards lents.

Recommandation:

- Remplacer par des requetes agregats.
- Ajouter `Pageable`.
- Creer des read models pour reporting.

#### Risque 2: calculs dashboard en memoire

Exemples:

- paiements charges puis sommes en Java;
- classes chargees puis filtrees en Java;
- statistiques eleves calculees depuis les inscriptions en memoire.

Recommandation:

Creer des requetes SQL/JPA dediees:

```text
countStudentsByStatus
sumPaymentsByPeriod
countEnrollmentsByClass
countAbsencesByPeriod
averageGradesByClass
```

## 11. Audit modules `attendance` et `billing`

### 11.1 Situation actuelle

Il existe:

```text
com.school.platform.attendance
services/attendance-service
```

Et:

```text
com.school.platform.billing
services/billing-service
```

### 11.2 Risque

Deux sources de verite possibles:

- Donnees et logique dans le monolithe.
- Donnees et logique dans le microservice.

Impact:

- incoherence de donnees;
- frontend difficile a configurer;
- evolution double;
- bugs de migration.

### 11.3 Recommandation

Court terme:

- Garder le monolithe comme source de verite officielle.
- Garder les services comme prototypes ou extraction en preparation.

Moyen terme:

- Choisir un domaine pilote a extraire, probablement `attendance`.
- Depricier les endpoints legacy correspondants.
- Ajouter une migration de donnees claire.
- Ajouter des tests contractuels.

## 12. Audit `SchoolApiConfig` du service attendance

Fichier:

```text
services/attendance-service/src/main/java/com/school/attendance/adapter/config/SchoolApiConfig.java
```

Point positif:

- Utilise `RestClient`.
- `SchoolApiProperties` est active via `@EnableConfigurationProperties`.
- Le service attendance depend du backend principal via un adapter explicite.

Risque:

- Pas de timeout explicite visible.
- Pas de retry/backoff.
- Pas de circuit breaker.
- Auth inter-service a verifier.

Recommandation:

Ajouter:

- connect timeout;
- read timeout;
- propagation de correlation ID;
- authentification de service a service;
- fallback documente;
- resilience4j si le service devient critique.

## 13. Audit tests

### 13.1 Etat actuel

Tests detectes:

- identity/access:
  - `JwtServiceTest`
  - `UsersServiceTest`
  - `AuthentificationServiceTest`
- settings:
  - `SettingsServiceTest`
- support:
  - `SupportServiceTest`
- services:
  - `AttendanceApplicationServiceTest`
  - `PaymentApplicationServiceTest`

Le build passe.

### 13.2 Manques

Il manque encore:

- tests `@SpringBootTest` du contexte principal;
- tests `@WebMvcTest` des controllers;
- tests Spring Security des roles;
- tests repository avec Testcontainers;
- tests Flyway sur base neuve;
- tests OpenAPI ou contract tests;
- tests des workflows critiques:
  - inscription;
  - paiement;
  - note;
  - absence;
  - reset password;
  - permissions.

### 13.3 Recommandation

Priorite tests:

1. `contextLoads` avec profil test.
2. Auth login/refresh/logout.
3. Role ADMIN/AGENT/ENSEIGNANT sur endpoints critiques.
4. Repository tests sur inscriptions, notes, paiements.
5. API tests sur students, payments, attendance, grades.

## 14. Recommandations prioritaires

### Priorite P0: rendre l'application vraiment fonctionnelle

1. Supprimer ou deplacer `src/main/SchoolManagementApplication.java` hors structure standard s'il est inutile.
2. Clarifier si `com.school.gestionuser` doit etre actif ou experimental.
3. Corriger les routes qui contiennent deja `/api` dans `@RequestMapping`.
4. Ajouter un test `@SpringBootTest` pour verifier que le contexte principal demarre.
5. Ajouter un profil `test` avec datasource controlee.

### Priorite P1: stabiliser l'API

1. Introduire `/api/v1`.
2. Normaliser les routes:
   - `/subject` -> `/subjects`
   - `/trimestre` -> `/trimesters`
   - `/montant` -> `/fees` ou `/amounts`
   - `/preinscription` -> `/pre-enrollments`
   - `/inscription` -> `/enrollments`
3. Remplacer `Map<String, Object>` par DTOs.
4. Documenter OpenAPI.
5. Standardiser les erreurs.

### Priorite P2: solidifier DDD/Clean Architecture

1. Garder `com.school.platform` comme root package.
2. Integrer ou isoler `gestionuser`.
3. Supprimer les doublons `compat/shared`.
4. Mettre les repositories derriere des ports dans les domaines critiques.
5. Deplacer la logique metier hors controllers.
6. Definir les aggregates:
   - UserAccount
   - Student
   - Enrollment
   - Grade
   - AttendanceRecord
   - Payment

### Priorite P3: performance et donnees

1. Supprimer les `findAll()` non pagines.
2. Creer des queries agregats pour dashboards.
3. Ajouter read models reporting.
4. Ajouter indexes manquants.
5. Preparer event/outbox pour notifications et reporting.

### Priorite P4: production readiness

1. Durcir Actuator.
2. Retirer `spring.security.firewall.strict=false`.
3. Ajouter correlation ID.
4. Ajouter logs structures.
5. Ajouter rate limiting.
6. Ajouter health checks DB/Redis.
7. Documenter les variables d'environnement.
8. Supprimer `node_modules` du backend ou justifier sa presence.

## 15. Roadmap conseillee

### Semaine 1: stabilisation fonctionnelle

- Verifier `SpringBootTest contextLoads`.
- Corriger routes `/api/api`.
- Clarifier `gestionuser`.
- Ajouter profil `test`.
- Nettoyer fichiers parasites.

### Semaine 2: API propre

- Ajouter `/v1`.
- Remplacer les premiers `Map<String,Object>`:
  - attendance;
  - billing;
  - dashboard;
  - teacher schedule.
- Standardiser erreurs 401/403.

### Semaine 3: securite

- Ajouter issuer/audience JWT.
- Ajouter rate limiting.
- Tester roles.
- Proteger actuator.
- Revoir CORS prod.

### Semaine 4: donnees et performance

- Supprimer les `findAll()` critiques.
- Ajouter requetes agregats dashboard.
- Tester migrations sur base neuve.
- Verifier indexes.

### Mois 2: DDD propre

- Stabiliser `identityaccess`.
- Stabiliser `enrollment`.
- Stabiliser `academic`.
- Stabiliser `billing`.
- Stabiliser `attendance`.

### Mois 3: production

- Monitoring.
- Audit trail.
- Backup/restore.
- Tests de charge.
- Documentation de deploiement.

## 16. Checklist backend fonctionnel

Le backend sera considere fonctionnel proprement quand:

1. `.\gradlew.bat clean test` passe.
2. `@SpringBootTest contextLoads` passe.
3. Les endpoints principaux sont testes.
4. Les routes sont sous `/api/v1`.
5. Aucune route ne genere `/api/api`.
6. Les erreurs sont uniformes.
7. Les roles sont testes.
8. Les `Map<String,Object>` publics sont remplaces.
9. Les listes sont paginees.
10. Les dashboards n'utilisent pas de gros `findAll()`.
11. Flyway reconstruit une base neuve.
12. Les secrets de prod sont obligatoires.
13. `gestionuser` est soit integre, soit isole.
14. Attendance et billing ont une source de verite claire.
15. La documentation OpenAPI correspond au backend reel.

## 17. Conclusion

Le backend est maintenant dans un etat bien meilleur: il compile, les tests passent, et l'organisation `com.school.platform` donne une vraie direction modulaire.

La prochaine etape ne doit pas etre de refaire encore toute l'arborescence. La bonne suite est:

```text
conserver com.school.platform
-> verifier le demarrage Spring complet
-> clarifier gestionuser
-> stabiliser routes API
-> remplacer Map par DTO
-> supprimer findAll critiques
-> durcir securite/config
-> ajouter tests d'integration
```

Si ces etapes sont faites dans cet ordre, le backend pourra devenir une base solide pour le frontend final et les evolutions futures.

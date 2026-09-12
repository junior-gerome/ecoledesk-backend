# SKILL.md

Skill spécifique au backend **ecoledesk-backend** (school-management).
Ce fichier décrit le contexte, les conventions et les contraintes à respecter par tout agent IA
(ou développeur) amené à analyser, étendre ou corriger ce référentiel, sans modifier son architecture
sauf accord explicite.

---

## 1. Purpose

Fournir une référence opérationnelle du backend Spring Boot de la plateforme de gestion scolaire afin
d'assurer la cohérence du code, de préserver l'architecture modulaire et de limiter la dette technique.

## 2. Project Context

- Backend Java 21 / Spring Boot 3.5.14 pour une plateforme de gestion scolaire (pré-inscriptions, inscriptions, scolarité/notes, paiements, staff, présences, rapports, notifications).
- Repo : `C:\school\ecoledesk-backend` (racine du projet/SKILL).
- Frontend séparé : application Angular-like consommant `/api` avec JWT Bearer + refresh token.
- Historique de développement français avec commentaires de code en français ; mixtes français/anglais dans les identifiants — **préférer des noms français compréhensibles**, et des messages d'API en français.

## 3. Technology Stack

| Domaine | Choix impératif |
|---|---|
| Langage/plateforme | Java 21 (toolchain Gradle) |
| Framework | Spring Boot 3.5.14 |
| Build | Gradle 9.6.1 (wrapper) |
| Persistance | Spring Data JPA + Hibernate (MySQL, dialect `MySQLDialect`) |
| Migrations | Flyway (`flyway-mysql`), `classpath:db/migration` |
| Cache | Spring Cache + Redis (Lettuce) |
| Sécurité | Spring Security, JWT (jjwt 0.11.5), BCrypt |
| Validation | Jakarta Validation (hibernate-validator 8.0.1.Final) |
| Mapping | MapStruct 1.5.5.Final + Lombok |
| API/OpenAPI | springdoc-openapi-starter-webmvc-ui 2.3.0 |
| Bureautique | Apache POI 5.2.3 (Excel), iText 7 + iText 5 (PDF) |
| Notifications | Firebase Admin SDK 9.1.1 (push) |
| Métriques | Actuator + Micrometer / Prometheus |

## 4. Gradle Conventions

- Ne pas ajouter de dépendance sans justification métier ; utiliser `implementation` sauf dev/processeur (`compileOnly` + `annotationProcessor`).
- Ne pas écraser le wrapper ni la version de Spring Boot.
- `bootRun` charge les variables du fichier externe `C:/school/variable_environnement/.env` (les variables système priment). **Ne jamais committer ce fichier ni ses valeurs.**
- Tâches existantes : `test` (JUnit Platform), `repairFlyway` (réparation d'historique Flyway).
- Garder `open-in-view=false`, HikariCP configuré, `context-path=/api`, port 8080.

## 5. Environment Variables & Configuration

Toutes les clés ci-dessous sont **obligatoires** pour démarrer (place-holders `${...}` dans `application.properties`).
Les valeurs réelles sont hors repo (`.env` externe).

| Clé | Rôle |
|---|---|
| `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` | Accès MySQL |
| `JPA_DDL_AUTO`, `JPA_SHOW_SQL` | Stratégie de schéma et journal SQL |
| `FLYWAY_ENABLED` | Activation de Flyway |
| `JWT_SECRET`, `JWT_EXPIRATION`, `JWT_REFRESH_EXPIRATION` | Signature/durées JWT |
| `APP_BOOTSTRAP_ADMIN_ENABLED|EMAIL|PASSWORD` | Création du compte admin au boot |
| `APP_CORS_ALLOWED_ORIGINS` | Origines CORS autorisées |
| `SPRING_CACHE_TYPE`, `REDIS_HOST`, `REDIS_PORT`, `REDIS_TIMEOUT` | Cache Redis |
| `MANAGEMENT_HEALTH_SHOW_DETAILS` | Détails /actuator/health |
| `APP_LOG_LEVEL`, `HIBERNATE_SQL_LOG_LEVEL`, `HIBERNATE_BINDER_LOG_LEVEL`, `SPRING_SECURITY_LOG_LEVEL`, `APP_SECURITY_LOG_LEVEL`, `SPRING_WEB_LOG_LEVEL`, `HIBERNATE_TYPE_LOG_LEVEL`, `HIBERNATE_VALIDATOR_LOG_LEVEL`, `SPRING_SECURITY_FIREWALL_STRICT` | Niveaux de logging/firewall |
| `FILE_UPLOAD_DIR` | Dossier de stockage des fichiers uploadés |

Profils : `dev` (défaut), `prod` (Swagger désactivé, log-link du reset masqué), `schema-export`.

## 6. Architecture

- **Monolithe modulaire** : modules = bounded contexts. Modules : `academic`, `attendance`, `billing`, `document`, `enrollment`, `identityaccess`, `notification`, `reporting`, `search`, `settings`, `shared`, `staff`, `support`.
- `shared` = transverse (API responses, exceptions, config, utilitaires).
- Les modules **ne doivent pas** dépendre des détails internes d'un autre module ; le passage par `shared` est le seul couplage autorisé.
- Règles d'architecture vérifiées par `ModularMonolithArchitectureTest` : à maintenir quand on modifie les packages.

## 7. Domain Rules

- **Pré-inscription** (`PreEnrollment`) : agrégat racine du dossier d'admission ; états `PreEnrollmentStatus` (DRAFT, SUBMITTED, UNDER_REVIEW, APPROVED, REJECTED, CANCELLED, …) ; invariants de soumission (candidat, année, niveau, tuteur principal, documents requis, frais/exonération) ; ne crée jamais `Student` directement.
- **Inscription** (`Enrollment`) : issue d'une pré-inscription validée ; unique par élève/année ; vérifie capacité, année ouverte, cohérence niveau/classe ; conserve l'historique.
- **Finances** : `PreEnrollmentFeePayment` (frais de dossier) ; `TuitionPaymentPlan` + `PaymentInstallment` (scolarité) — les tranches restent des données, jamais une enum figée.
- **Règles transverses** : notes 0–20 (`Grade`), soft delete par flag `active`, génération des numéros métier (élève/inscription/employé) via VOs embarqués.
- Réf. langage : `docs/UBIQUITOUS_LANGUAGE.md`.

## 8. Package Structure

```
com.school.platform.<module>/
├── web/ (ou adapter.in.rest pour billing)   # Controllers REST
├── application/
│   ├── dto/          # Requêtes / réponses
│   ├── interfaces/   # Interfaçage application du module (optionnel)
│   ├── impl/         # Implémentation des services
│   ├── mapper/       # MapStruct
│   └── policy/       # Règles de politique applicative (optionnel)
├── domain/
│   ├── model/        # Entités & agrégats
│   ├── valueobject/  # VOs embarqués
│   └── policy/       # Règles de domaine (optionnel)
└── infrastructure/
    ├── persistence/  # JPA repositories
    ├── security/     # Filtres, services de sécurité
    ├── config/       # Configurations (cache, …)
    ├── bootstrap/    # Seeds
    ├── flyway/       # Tooling migrations
    └── backup/       # Sauvegardes planifiées
```

## 9. Controller Conventions

- Retourner **`ApiResponse<T>`** (et `PageResponse<T>` pour la pagination) plutôt que des entités ou `Map` brutes.
- Annoter les routes sensibles avec `@PreAuthorize` (rôles : `ADMIN`, `AGENT`, `ENSEIGNANT`…).
- Valider les `@RequestBody` avec `@Valid` ; garder les contrôleurs minces (règle : pas de logique métier dans le contrôleur).
- HTTP : 200/201 pour succès, exceptions → `GlobalExceptionHandler`.
- Éviter l'exposition directe d'entités JPA (fuite d'abstraction) : utiliser DTO/projection.

## 10. Service Conventions

- Préférer **interface + implémentation** (`application.interfaces` + `application.impl`) ; les classes `AbstractService`/`IGenericService` et `UserAccountManagementService` legacy sont **à migrer**.
- Marquer les cas d'usage avec `UseCase`, orchestrer entre repos et politiques de domaine.
- `@Transactional` explicite : **read-only** pour les lectures (`readOnly = true`), valider les règles métier dans la transaction d'écriture (REQUIRES_NEW pour les événements type audit).
- Journaliser les actions métier sensibles via `BusinessAuditService` (`LogActivite`).

## 11. Repository Conventions

- Spring Data JPA : requêtes dérivées + `@Query` SNIPPETS ; **éviter `nativeQuery`** sauf abandon explicite.
- Préférer `@EntityGraph` pour éviter N+1 sur les chemins chauds (person+roles+permissions, enrollments…).
- PESSIMISTIC_WRITE pour les sections critiques de concurrence (ex. numéro d'élève unique).
- Ne pas charger des entités sur les listes ; utiliser projections + DTO.

## 12. Entity Conventions

- Hériter de `identityaccess.domain.model.BaseEntity` (id auto-incrément, `active`, `created_at`/`updated_at`, AuditingEntityListener). Ne pas réintroduire l'ancien `shared.domain.legacy.BaseEntity`.
- `@Table` nom explicite en minuscules/snake_case ; unique constraints nommées (`uk_<table>_<col>`).
- `@Inheritance(JOINED)` pour `Person`/sous-types ; VO embarqués pour identité (email, téléphone, date de naissance, adresse).
- `fetch = LAZY` sur toutes les relations ; `EnumType.STRING` pour les enums.

## 13. JPA & Database Conventions

- Dialect : `MySQLDialect`. Pilote : `com.mysql.cj.jdbc.Driver`.
- **Schéma** : Flyway devrait devenir la seule source de vérité (actuellement UNE migration `V1__insert_default_roles.sql`). Toute modification de schéma passe par une migration Flyway `V<N+1>__*.sql` et idéalement `JPA_DDL_AUTO=validate` en prod.

## 14. Migration / Flyway Conventions

- Ajouter une nouvelle migration versionnée (ex. `V2__add_table_x.sql`) dans `src/main/resources/db/migration`.
- Ne jamais éditer une migration déjà appliquée (Flyway checksum). `repairFlyway` existe pour les cas de force majeure.
- Noms : `V<version>__<description>.sql`.

## 15. Redis / Cache Conventions

- Spronté via `RedisConfig` : caches `students` (TTL 10 min), `grades` (5 min), `guardians` (10 min).
- Annoter lectures coûteuses `@Cacheable`, mutations `@CacheEvict` (clé cohérente), éviter les nulls.
- Ne pas mettre en cache les entités entières ; privilégier des DTO sérialisables JSON.
- Rate limiting (`LoginAttemptService`, `PasswordResetRateLimitService`) : garder la résilience fail-open documentée ; alerter si Redis indisponible.

## 16. Security Conventions

- Spring Security `SecurityConfig` : stateless, CSRF off, `JwtAuthenticationFilter`, `BCryptPasswordEncoder` (12).
- Rôles/permissions : `UserAccount` → `Role` → `Permission` ; autorités exposées en `ROLE_<code>` + permissions.
- WebSocket STOMP (endpoints `/ws`, `/api/ws`) — garder CORS/backend dans la limite `app.cors.allowed-origins`.
- Ne jamais écrire/committer de secret ; le fallback JWT est réservé au développement.

## 17. JWT Conventions

- Signé HS256 via jjwt 0.11.5 (`JwtService`) ; secret ≥ 32 octets en prod, `jwt.expiration` / `jwt.refresh-expiration` variables d'env.
- Refresh token : rotation + révocation chaînée (`RefreshToken.replaced_by_token_hash`, `isActive`).
- Charger l'utilisateur via `UserDetailsServiceImpl` (`@EntityGraph` person + roles + permissions).
- Authentifier le principal avec `AuthenticatedUserPrincipal`.

## 18. Validation Conventions

- Bean Validation sur les DTO de requête (`@NotBlank`, `@Size`, `@Email`, `@Pattern`), messages **en français**.
- Validateurs métier complexes en classes `Validator` Spring (ex. `StudentValidator`, `GradeValidator`, `ClassValidator`).
- Politiques de domaine dans `domain.policy` (ex. `AdmissionPolicy`, `PaymentValidator`).

## 19. Exception Conventions

- Utiliser le catalogue unifié (`shared.domain.exception`) : `BusinessException` (400), `BadRequestException` (400), `ResourceNotFoundException` (404), `ValidationException` (400), `AuthorizationException` (403) — ne pas créer de nouvelles familles.
- Laisser `GlobalExceptionHandler` produire les réponses d'erreur ; ne pas renvoyer directement des exceptions.
- Migrer les doublons `compat` / `shared` vers le catalogue commun.

## 20. API Response Conventions

- Réponses standard : `ApiResponse<T>` et `PageResponse<T>` (en `shared.web`).
- Erreurs : `ErrorResponse` / `ValidationErrorResponse` (champs par erreur) / `SharedErrorResponse`.
- Pagination : `PageResponse.from(page)`.
- Ne pas introduire de formats de réponse parallèles (`Map`, entités nues, anciens `compat`).

## 21. Logging & Auditing Conventions

- Niveaux pilotés par variables d'env (pas de dur).
- Auditer les actions sensibles via `BusinessAuditService` / `LogActivite`.
- Journaliser les tentatives d'authentification (succès/échec + IP) via `AuthenticationAuditEvent`.
- Ne jamais logger de secrets, mots de passe ou contenus JWT.

## 22. Testing Conventions

- JUnit 5 + Mockito + AssertJ pour les tests unitaires ; `@WebMvcTest`/`@SpringBootTest` possibles en intégration.
- Testcontainers (déclaré) à utiliser pour les tests d'intégration DB/Redis.
- `@WithMockUser` pour la sécurité web.
- Conserver/étendre `ModularMonolithArchitectureTest` (garde-fou d'architecture).
- Structure : `src/test/java/...` par module.

## 23. Performance & N+1 Rules

- Interdire N+1 sur lectures listées : `@EntityGraph`, projections, ou requête join.
- Cache TTL courts autours des lectures "hot" ; evict systématiques sur mutations.
- Éviter `open-in-view` (désactivé) : les entités ne doivent pas être accédées en dehors de la transaction.
- `PESSIMISTIC_WRITE` réservé aux secteurs concurrents critiques.

## 24. Documentation & Style

- Commentaires et commits : français, concis.
- Suivre le langage ubiquitaire (`docs/UBIQUITOUS_LANGUAGE.md`).
- Pas de `/** javadoc */` mécanique ; documenter le **pourquoi**.
- Ne pas committer de `build.log`, `hs_err_pid*.log`, `inspect_db.sql` ni valeurs `.env`.

## 25. Scope

- Le SKILL couvre : backend Spring Boot (identification, inscriptions, scolarité, finances, académie, staff, présences, reporting, search, settings, support, notifications, documents) et son API REST.
- Hors scope : front-end, déploiement cloud, scripts d'infrastructure externe, base de données tunée en dehors des migrations.

## 26. No-Regression Rules (MUST / MUST NOT)

- **MUST** : respecter les règles d'architecture (test `ModularMonolithArchitectureTest`).
- **MUST** : passer toute modification de schéma par une migration Flyway.
- **MUST** : garder les réponses API uniformes (`ApiResponse` / `PageResponse`).
- **MUST** : valider les entrées des requêtes et respecter le catalogue d'exceptions.
- **MUST NOT** : écrire/committer des secrets (clés JWT, `.env`, mots de passe, clés Firebase).
- **MUST NOT** : introduire de nouvelles hiérarchies d'exceptions ni types de réponse parallèles.
- **MUST NOT** : exposer des entités JPA directement (utiliser DTO/projection).
- **MUST NOT** : modifier une migration Flyway déjà appliquée ; réparer via `repairFlyway` seulement en cas de force majeure.
- **MUST NOT** : réintroduire `open-in-view=true`, CSRF activé, ou retirer la protection par rôle sans motif.

## 27. Development Workflow

1. Lire `docs/UBIQUITOUS_LANGUAGE.md` et ce SKILL.
2. Repérer le module concerné : créer/modifier uniquement dans le module adéquat.
3. Écrire les tests d'abord ou en parallèle (unitaire → intégration si nécessaire).
4. Suivre le flux contrôleur → service (interface+impl) → repository → entité/VO → DTO/mapper.
5. Vérifier la build : `./gradlew test` puis `./gradlew build`.
6. Vérifier les migrations Flyway et la cohérence du schéma.
7. Mettre à jour `BACKEND_AUDIT.md`/`docs/` si un choix structurant change.

## 28. Validation Checklist

- [ ] `./gradlew test` passe.
- [ ] `./gradlew build` passe (compilation + packaging).
- [ ] Pas de N+1 introduit (lectures chaudes).
- [ ] Réponses API conformes (`ApiResponse`/`PageResponse`).
- [ ] Migrations Flyway cohérentes (nouvelle version, non éditées).
- [ ] Secrets absents du diff ; fichiers sensibles git-ignorés.
- [ ] Tests unitaires écrits/mis à jour pour les nouvelles règles métier.
- [ ] Architecture : pas de dépendance inter-module illégale.

## 29. Reporting Format

Quand un travail est demandé sur ce référentiel, fournir un compte-rendu conforme au modèle suivant :

```
BACKEND SKILL AUDIT COMPLETED
Java: 21
Spring Boot: 3.5.14
Gradle: 9.6.1 (wrapper)
Architecture: monolithe modulaire - modules: academic, attendance, billing, document,
               enrollment, identityaccess, notification, reporting, search, settings,
               shared, staff, support
Database: MySQL (Flyway: 1 migration, V1__insert_default_roles.sql)
Flyway: activable via FLYWAY_ENABLED; schéma actuel géré par Hibernate ddl-auto
Redis: cache (students, grades, guardians + par défaut) + rate limiting
Security: JWT (HS256, jjwt 0.11.5) + Spring Security + BCrypt
Testing: 10 fichiers/25 méthodes (JUnit5+Mockito+AssertJ); test d'architecture
Critical Issues: schéma non versionné Flyway; hiérarchies d'exceptions multiples
Skill: actif (SKILL.md) - Audit: BACKEND_AUDIT.md
```

---

*Skill valide pour `ecoledesk-backend` (school-management). Dernière révision : 2026-09-09.*
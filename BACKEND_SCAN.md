> **Document produit par une équipe virtuelle composée de :** Architecte Logiciel Senior · Architecte Cloud · Expert Spring Boot · Expert Base de Données · Expert Performance · Expert Sécurité OWASP · Expert DevOps · Tech Lead Backend
>
> **Cible de déploiement :** 100+ établissements · 20 000+ élèves · 1 000+ enseignants · Millions d'enregistrements · Centaines d'utilisateurs simultanés

---

## TABLE DES MATIÈRES

1. [Résumé Exécutif](#1-résumé-exécutif)
2. [Analyse Globale & Notations](#2-analyse-globale--notations)
3. [Analyse du Code](#3-analyse-du-code)
4. [Analyse Base de Données](#4-analyse-base-de-données)
5. [Analyse Performance](#5-analyse-performance)
6. [Analyse Sécurité](#6-analyse-sécurité)
7. [Préparation Multi-Établissements (Multi-Tenancy)](#7-préparation-multi-établissements-multi-tenancy)
8. [Migration vers les Microservices](#8-migration-vers-les-microservices)
9. [Architecture Cible](#9-architecture-cible)
10. [50+ Nouvelles Fonctionnalités](#10-50-nouvelles-fonctionnalités)
11. [Roadmap 12 Mois](#11-roadmap-12-mois)
12. [Rapport Final](#12-rapport-final)

---

## 1. RÉSUMÉ EXÉCUTIF

Le projet `school-management` est une application Spring Boot monolithique gérant un établissement scolaire camerounais (système francophone/anglophone). Le code révèle un développeur sérieux qui a fait les bons choix technologiques de surface (Spring Boot 3, Flyway, JWT, Hexagonal dans les microservices en cours), mais dont l'implémentation souffre de défauts structurels graves empêchant tout passage à l'échelle.

### Verdict en une phrase

> **Le projet est démontable pour un seul établissement. Il est inutilisable tel quel pour 100 établissements et tombera sous charge dès 500 utilisateurs simultanés.**

### Chiffres critiques identifiés

| Problème | Occurrences | Impact |
|---|---|---|
| `findAll()` sans pagination | 12+ | Crash mémoire à 10 000+ élèves |
| Absence totale de multi-tenancy | Système entier | Isolation des données impossible |
| Cache Redis désactivé explicitement | 1 ligne critique | Pas de scalabilité horizontale |
| Tests unitaires | 4 fichiers | Couverture estimée < 5% |
| Clé JWT vide par défaut | `jwt.secret=` | Vulnérabilité critique en production |
| `@Data` sur entités JPA | 10+ entités | Fuites mémoire, boucles infinies |
| Swagger public en production | SecurityConfig | Exposition de l'API sans auth |

---

## 2. ANALYSE GLOBALE & NOTATIONS

### Architecture actuelle

```
school-management/
├── backend/                          ← Monolithe principal (Spring Boot 3, MySQL)
│   ├── src/main/java/com/school/
│   │   ├── management/              ← Domaine métier principal
│   │   │   ├── controller/          ← 20 contrôleurs REST
│   │   │   ├── service/             ← 20 services métier
│   │   │   ├── repository/          ← 25 repositories JPA
│   │   │   ├── model/               ← 20 entités JPA
│   │   │   ├── dto/                 ← 40+ DTOs
│   │   │   ├── mappers/             ← MapStruct mappers
│   │   │   ├── config/              ← Spring Security, CORS, Swagger
│   │   │   └── security/            ← JWT, UserDetails
│   │   ├── service/                 ← Services analytiques (AnalyticsService...)
│   │   └── validator/               ← Validateurs custom
│   └── services/                    ← Microservices en cours d'extraction
│       ├── billing-service/         ← Hexagonal architecture ✅
│       └── attendance-service/      ← Hexagonal architecture ✅
```

**Modèle de données identifié :** Student → Parent → InscriptionStudent → ClasseRoom → Section → AnneeScolaire → Grade / Paiement / Absence

### Forces du projet

- ✅ Spring Boot 3 avec Java (moderne)
- ✅ Flyway pour les migrations (V1 à V11)
- ✅ Début de microservices avec architecture hexagonale (billing-service, attendance-service)
- ✅ JWT avec refresh token persisté en base
- ✅ BCrypt avec cost factor 12
- ✅ CSRF désactivé correctement pour REST
- ✅ `open-in-view=false` (bonne pratique JPA)
- ✅ Prometheus/Actuator configuré
- ✅ Pagination présente dans certains services
- ✅ Optimistic locking (`@Version`) sur Grade et Paiement
- ✅ CORS configurable par variables d'environnement
- ✅ Validation Bean Validation (`@NotBlank`, `@Past`, etc.)
- ✅ Soft delete sur Student (`active=false`)

### Faiblesses majeures

- ❌ **Zéro multi-tenancy** — une seule base, toutes données mélangées
- ❌ Redis **explicitement désactivé** dans `application.properties`
- ❌ `findAll()` non paginé dans SearchService, ParentService, StudentExportService
- ❌ `@Data` Lombok sur entités JPA → `hashCode`/`equals` sur relations → boucle infinie
- ❌ 4 fichiers de test seulement pour ~100 classes
- ❌ Clé JWT vide par défaut (`jwt.secret=`)
- ❌ Swagger UI public en production (pas de filtre par profil)
- ❌ MySQL au lieu de PostgreSQL (performances, JSON, partitionnement)
- ❌ Montants financiers en `Long` (centimes ?) au lieu de `BigDecimal`
- ❌ Pas de rate limiting sur les endpoints d'authentification
- ❌ Colonnes mixtes (`montanRestant` — typo en base)
- ❌ `UserDetailsServiceImpl` dupliqué (deux classes identiques dans deux packages)

### Notations

| Critère | Note /10 | Justification |
|---|---|---|
| **Architecture** | **5/10** | Monolithe bien structuré, hexagonal entamé, mais pas de bounded contexts clairs |
| **Qualité du code** | **5.5/10** | Code lisible, Lombok correct, mais anti-patterns JPA graves |
| **Sécurité** | **4/10** | JWT OK, mais clé vide par défaut, Swagger public, pas de rate limiting |
| **Performance** | **3/10** | `findAll()` partout, Redis désactivé, pas de cache distribué |
| **Scalabilité** | **2/10** | Monolithe sans multi-tenancy, impossible à scaler horizontalement |
| **Maintenabilité** | **5/10** | Code lisible mais dette technique élevée, duplication |
| **Testabilité** | **2/10** | Couverture estimée < 5%, 4 fichiers de test |

**Note globale : 3.8/10 pour une cible production multi-établissements**

---

## 3. ANALYSE DU CODE

### 3.1 Anti-Pattern Critique : `@Data` sur les Entités JPA

**Problème :** `@Data` de Lombok génère `hashCode()` et `equals()` en incluant toutes les relations. Sur des entités avec des `@OneToMany` / `@ManyToOne`, cela provoque des boucles infinies lors de la sérialisation, des `LazyInitializationException` et des N+1 implicites.

**Fichiers concernés :** `Student.java`, `Users.java`, `Grade.java`, `Paiement.java`, `ClasseRoom.java`, `Parent.java`, `Teacher.java`, `Absence.java`, `InscriptionStudent.java`, `Montant.java`

**Impact :** StackOverflow en production, fuites mémoire, logs illisibles.

**Correction :**

```java
// AVANT (dangereux)
@Data
@Entity
@Table(name = "students")
public class Student { ... }

// APRÈS (correct)
@Entity
@Table(name = "students")
@Getter
@Setter
@NoArgsConstructor
@ToString(exclude = {"parent", "absences"})  // Exclure les relations
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Student {
    @EqualsAndHashCode.Include
    @Id
    private Long id;
    // ...
}
```

---

### 3.2 Violation SRP : `StudentService` fait trop de choses

**Problème :** `StudentService.java` gère : création d'élève, résolution de parent, génération de rapport, statistiques mensuelles, tendances d'inscription. C'est au moins 4 responsabilités distinctes.

**Impact :** Impossible à tester unitairement, couplage fort avec GradeRepository, AbsenceRepository, InscriptionStudentRepository.

**Correction :**

```java
// Découper en :
StudentCommandService   // createStudent, updateStudent, deleteStudent
StudentQueryService     // getById, getAllStudents, getByParent
StudentReportService    // generateStudentReport, generateBulletin
StudentStatisticsService // getStatistics, monthlyTrend
```

---

### 3.3 Duplication de `UserDetailsServiceImpl`

**Problème :** Deux classes `UserDetailsServiceImpl` existent dans deux packages différents :
- `com.school.management.service.UserDetailsServiceImpl`
- `com.school.management.service.UserDetailsServiceImpl` (identique)

**Impact :** Ambiguïté Spring, comportement imprévisible selon le chargement des beans.

**Correction :** Supprimer l'une, garder uniquement `com.school.management.security.UserDetailsServiceImpl`.

---

### 3.4 `GradeService.java` : 565 lignes — God Class

**Problème :** 565 lignes pour un seul service. Mélange de : calcul de notes, statistiques de classe, calcul de rang, génération de rapport, logique de période, gestion du cache.

**Correction :** Extraire :
```java
GradeCommandService     // save, update, delete, bulkAdd
GradeQueryService       // findByClass, findByStudent
GradeCalculationService // average, rank, weightedAverage
GradeReportService      // generateClassReport, generateStudentReport
```

---

### 3.5 Gestion des exceptions incohérente

**Problème :** Deux hiérarchies d'exceptions coexistent :
- `com.school.exception.*` (BusinessException, ResourceNotFoundException...)
- `com.school.management.shared.exceptions.*` (BusinessException, NotFoundException...)

Les services utilisent les deux de manière aléatoire.

**Correction :** Supprimer `com.school.exception` et n'utiliser que `com.school.management.shared.exceptions`.

---

### 3.6 Validation des données insuffisante dans `PaymentService`

**Problème :** Les montants financiers (`montantPaye`, `montantRestant`) sont de type `Long`. Aucune contrainte de cohérence (`montantPaye + montantRestant = montantTotal`) n'est vérifiée en service. Le champ `montanRestant` contient une faute de frappe qui se répercute en base de données.

**Correction :**
```java
// Montants en BigDecimal avec contrainte métier
@AssertTrue(message = "montantPaye + montantRestant doit égaler le montant total")
public boolean isAmountCoherent() {
    return montantPaye.add(montantRestant).compareTo(montantTotal) == 0;
}
```

---

### 3.7 Logging insuffisant et non structuré

**Problème :** Les logs utilisent des messages texte simples. Aucun MDC (Mapped Diagnostic Context) avec `userId`, `schoolId`, `requestId`. Impossible de tracer une requête en production.

**Correction :**
```java
// Ajouter un filter MDC
public class MdcFilter implements Filter {
    public void doFilter(ServletRequest request, ...) {
        MDC.put("requestId", UUID.randomUUID().toString());
        MDC.put("userId", extractUserId(request));
        MDC.put("schoolId", extractSchoolId(request));
        try { chain.doFilter(request, response); }
        finally { MDC.clear(); }
    }
}
```

---

## 4. ANALYSE BASE DE DONNÉES

### 4.1 Moteur de base de données inadapté

**Problème :** MySQL est utilisé. Pour une application scolaire à grande échelle avec millions d'enregistrements, **PostgreSQL** offre :
- Partitionnement natif (par `annee_scolaire` ou `school_id`)
- JSONB pour les préférences et métadonnées
- `RANK()` / `DENSE_RANK()` plus performants (déjà utilisés en native query)
- Row-Level Security pour le multi-tenant
- `pg_partman` pour l'archivage automatique

**Migration recommandée :** MySQL → PostgreSQL (Flyway peut gérer la migration).

---

### 4.2 Absence d'index critiques

Les index ajoutés en V11 sont insuffisants. Index manquants identifiés :

```sql
-- Index manquants critiques
CREATE INDEX idx_students_active_lastname ON students(active, last_name_student);
CREATE INDEX idx_students_parent ON students(parent_id) WHERE active = true;
CREATE INDEX idx_inscription_active_year ON inscription_student(anneescolaire_id, active);
CREATE INDEX idx_paiements_type_date ON paiements(type_paiement, date_paiement);
CREATE INDEX idx_absences_student_date ON absences(student_id, date_absence);
CREATE INDEX idx_users_username ON users(username);  -- probablement manquant
CREATE INDEX idx_grade_subject_class ON academic_grade(subject_id, classe_id);

-- Pour la recherche full-text
CREATE INDEX idx_students_fulltext ON students USING gin(
    to_tsvector('french', last_name_student || ' ' || first_name_student)
);
```

---

### 4.3 Normalisation insuffisante — Montants financiers

**Problème :** La table `montant` stocke le montant par type de paiement et par classe. Il n'y a pas de table `frais_scolaire` liée à l'année scolaire. Si les frais changent d'une année à l'autre, l'historique est perdu.

**Correction :**
```sql
CREATE TABLE frais_scolaire (
    id BIGSERIAL PRIMARY KEY,
    annee_scolaire_id BIGINT NOT NULL REFERENCES annee_scolaire(id),
    classe_room_id BIGINT NOT NULL REFERENCES classe_room(id),
    type_paiement VARCHAR(50) NOT NULL,
    montant DECIMAL(15,2) NOT NULL,
    actif BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE(annee_scolaire_id, classe_room_id, type_paiement)
);
```

---

### 4.4 Partitionnement recommandé pour la montée en charge

```sql
-- Partitionner academic_grade par année scolaire (PostgreSQL)
CREATE TABLE academic_grade (
    id BIGSERIAL,
    annee_scolaire_id BIGINT NOT NULL,
    -- ... autres colonnes
    PRIMARY KEY (id, annee_scolaire_id)
) PARTITION BY RANGE (annee_scolaire_id);

CREATE TABLE academic_grade_2024_2025 PARTITION OF academic_grade
    FOR VALUES FROM (1) TO (2);

CREATE TABLE academic_grade_2025_2026 PARTITION OF academic_grade
    FOR VALUES FROM (2) TO (3);
```

---

### 4.5 Stratégie d'archivage

```sql
-- Table d'archive pour les données > 3 ans
CREATE TABLE academic_grade_archive AS TABLE academic_grade WITH NO DATA;

-- Job cron mensuel
INSERT INTO academic_grade_archive
    SELECT * FROM academic_grade
    WHERE annee_scolaire_id IN (
        SELECT id FROM annee_scolaire WHERE fin < NOW() - INTERVAL '3 years'
    );

DELETE FROM academic_grade WHERE id IN (SELECT id FROM academic_grade_archive);
```

---

## 5. ANALYSE PERFORMANCE

### 5.1 Catastrophe : `findAll()` sans pagination dans SearchService

**Problème (code actuel) :**
```java
// SearchService.java — CATASTROPHIQUE à 20 000 élèves
return studentRepository.findAll().stream()
    .filter(student -> matches(student, keyword, ...))
    .limit(DEFAULT_LIMIT)
    .collect(...)
```

**Impact :** À 20 000 élèves, cela charge 20 000 entités en mémoire, leurs relations (`Parent`, `InscriptionStudent`...) soit potentiellement **1-5 GB de données** avant filtrage.

**Correction :**
```java
// Utiliser une Specification JPA ou une requête JPQL avec LIKE
@Query("""
    SELECT s FROM Student s
    JOIN s.parent p
    WHERE s.active = true
    AND (
        LOWER(s.lastNameStudent) LIKE LOWER(CONCAT('%', :keyword, '%'))
        OR LOWER(s.firstNameStudent) LIKE LOWER(CONCAT('%', :keyword, '%'))
    )
    """)
Page<Student> searchStudents(@Param("keyword") String keyword, Pageable pageable);
```

---

### 5.2 N+1 Query dans `StudentService.getStudentStatistics()`

**Problème :**
```java
List<InscriptionStudent> inscriptions = inscriptionStudentRepository.findAll();
// Pour chaque inscription : accès lazy à inscription.getClasseRoom().getSection()
// → N requêtes SQL supplémentaires
```

**Correction :**
```java
@Query("""
    SELECT i FROM InscriptionStudent i
    JOIN FETCH i.classRoom cr
    JOIN FETCH cr.section s
    WHERE i.anneeScolaire.id = :anneeId
    """)
List<InscriptionStudent> findAllWithDetails(@Param("anneeId") Long anneeId);
```

---

### 5.3 Redis désactivé — Opportunité manquée

**Problème critique :**
```properties
# application.properties — LIGNE HONTEUSE
spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration
```

Redis est exclus alors que `@Cacheable` est utilisé sur `GradeService`. Le cache tourne donc sur ConcurrentHashMap **in-memory**, non partageable entre instances, non persistant.

**Correction immédiate :**
```properties
# Supprimer la ligne d'exclusion et ajouter :
spring.data.redis.host=${REDIS_HOST:localhost}
spring.data.redis.port=${REDIS_PORT:6379}
spring.data.redis.password=${REDIS_PASSWORD:}
spring.cache.type=redis
spring.cache.redis.time-to-live=3600000
```

```java
@Configuration
@EnableCaching
public class CacheConfig {
    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory factory) {
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofHours(1))
            .serializeValuesWith(
                RedisSerializationContext.SerializationPair.fromSerializer(
                    new GenericJackson2JsonRedisSerializer()
                )
            );
        return RedisCacheManager.builder(factory)
            .cacheDefaults(config)
            .withCacheConfiguration("grades", config.entryTtl(Duration.ofMinutes(30)))
            .withCacheConfiguration("students", config.entryTtl(Duration.ofHours(2)))
            .build();
    }
}
```

---

### 5.4 Connexions MySQL : Pool sous-dimensionné

**Problème :**
```properties
spring.datasource.hikari.maximum-pool-size=10
```

Pour 500 utilisateurs simultanés, 10 connexions est insuffisant. Formule : `connexions = (nb_cores × 2) + nb_disques_effectifs`.

**Correction :**
```properties
spring.datasource.hikari.maximum-pool-size=${DB_POOL_SIZE:50}
spring.datasource.hikari.minimum-idle=${DB_POOL_MIN_IDLE:10}
spring.datasource.hikari.connection-timeout=30000
spring.datasource.hikari.max-lifetime=1800000
spring.datasource.hikari.leak-detection-threshold=60000
```

---

### 5.5 Rapport étudiant : requêtes en cascade

**Problème dans `StudentService.generateStudentReport()`:**
```java
// 1. findById(studentId)          → 1 query
// 2. gradeRepository.findByStudentId(...) → 1 query + N lazy loads
// 3. absenceRepository.countByStudentId  → 1 query
// 4. gradeRepository.calculateStudentRank → 1 native query
// 5. findByClasseId(classe.getId())      → 1 query + M lazy loads
// TOTAL : 5 + N + M requêtes pour un seul rapport
```

**Correction :** Créer une requête consolidée ou utiliser des projections :
```java
@Query("""
    SELECT new com.school.dto.StudentReportProjection(
        s.id, s.lastNameStudent, s.firstNameStudent,
        COUNT(DISTINCT a.id), AVG(g.grade)
    )
    FROM Student s
    LEFT JOIN Absence a ON a.student.id = s.id
    LEFT JOIN Grade g ON g.student.id = s.id AND g.sequence.id = :sequenceId
    WHERE s.id = :studentId
    GROUP BY s.id, s.lastNameStudent, s.firstNameStudent
    """)
Optional<StudentReportProjection> findStudentReportData(Long studentId, Long sequenceId);
```

---

## 6. ANALYSE SÉCURITÉ

### 6.1 🔴 CRITIQUE — Clé JWT vide par défaut

**Niveau de risque : CRITIQUE**

**Problème :**
```properties
jwt.secret=${JWT_SECRET:}
```

Si `JWT_SECRET` n'est pas défini en environnement, la clé est une chaîne vide. Le `@PostConstruct` `validateSecretKey()` lève bien une exception si < 32 caractères, mais si la variable est définie avec une valeur faible (ex: `"secret"` ou `"12345678901234567890123456789012"`), aucun avertissement n'est levé.

**Correctif :**
```java
@PostConstruct
public void validateSecretKey() {
    if (!StringUtils.hasText(secretKey)) {
        throw new IllegalStateException("JWT_SECRET non défini. Variable d'environnement obligatoire.");
    }
    byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
    if (keyBytes.length < 64) { // 512 bits minimum pour HS512
        throw new IllegalStateException("JWT_SECRET trop court. Minimum 64 caractères pour HS512.");
    }
    // Vérifier l'entropie minimale
    long uniqueChars = secretKey.chars().distinct().count();
    if (uniqueChars < 20) {
        throw new IllegalStateException("JWT_SECRET insuffisamment complexe.");
    }
}
```

---

### 6.2 🔴 CRITIQUE — Swagger UI accessible en production sans authentification

**Niveau de risque : ÉLEVÉ**

**Problème dans `SecurityConfig.java` :**
```java
.requestMatchers("/swagger-ui/**", "/swagger-ui.html").permitAll()
.requestMatchers("/v3/api-docs/**").permitAll()
```

Swagger est public, exposant tous les endpoints, leurs paramètres et leurs modèles de données à n'importe qui.

**Correctif :**
```java
// Conditionner par profil Spring
@Value("${app.swagger.enabled:false}")
private boolean swaggerEnabled;

// Dans securityFilterChain :
if (swaggerEnabled) {
    auth.requestMatchers("/swagger-ui/**", "/v3/api-docs/**").hasRole("ADMIN");
} else {
    auth.requestMatchers("/swagger-ui/**", "/v3/api-docs/**").denyAll();
}
```

---

### 6.3 🟠 ÉLEVÉ — Absence de Rate Limiting sur l'authentification

**Niveau de risque : ÉLEVÉ**

**Problème :** Les endpoints `/auth/login` et `/users/password-reset-request` sont ouverts sans limitation de taux. Vulnérables aux attaques par force brute et credential stuffing.

**Correctif avec Bucket4j :**
```java
@Component
public class RateLimitingFilter extends OncePerRequestFilter {
    private final Cache<String, Bucket> buckets = Caffeine.newBuilder()
        .expireAfterAccess(1, TimeUnit.HOURS)
        .build();

    @Override
    protected void doFilterInternal(HttpServletRequest request, ...) {
        if (request.getRequestURI().startsWith("/auth/")) {
            String ip = request.getRemoteAddr();
            Bucket bucket = buckets.get(ip, k -> Bucket4j.builder()
                .addLimit(Bandwidth.classic(5, Refill.intervally(5, Duration.ofMinutes(1))))
                .build());
            if (!bucket.tryConsume(1)) {
                response.setStatus(429);
                response.getWriter().write("{\"error\": \"Too Many Requests\"}");
                return;
            }
        }
        filterChain.doFilter(request, response);
    }
}
```

---

### 6.4 🟠 ÉLEVÉ — Tokens de réinitialisation de mot de passe en clair

**Problème dans `Users.java` :**
```java
@Column(name = "reset_token", length = 255)
private String resetToken;
```

Le token de reset est stocké en clair en base de données. Si la base est compromise, tous les tokens actifs peuvent être utilisés.

**Correctif :**
```java
// Stocker uniquement le hash SHA-256 du token
public void setResetToken(String plainToken) {
    this.resetTokenHash = hashToken(plainToken);
}

private String hashToken(String token) {
    return HexFormat.of().formatHex(
        MessageDigest.getInstance("SHA-256").digest(
            token.getBytes(StandardCharsets.UTF_8)
        )
    );
}
```

---

### 6.5 🟡 MOYEN — `@Slf4j` logs les URI avec données potentiellement sensibles

**Problème dans `SecurityConfig` :**
```java
log.warn("🚫 Unauthorized access attempt to: {} from {}", req.getRequestURI(), req.getRemoteAddr());
```

Les URI peuvent contenir des paramètres sensibles (tokens, ids privés).

**Correctif :** Sanitiser les URI avant de les logger, ou utiliser des masques.

---

### 6.6 🟡 MOYEN — Actuator exposé publiquement

**Problème :**
```properties
management.endpoint.health.show-details=always
```

`/actuator/health` avec `show-details=always` expose des informations sur la base de données, le status des services tiers.

**Correctif :**
```properties
management.endpoint.health.show-details=when-authorized
management.endpoints.web.exposure.include=health,metrics,prometheus
```

---

### Tableau récapitulatif sécurité OWASP

| Risque OWASP | Présent | Niveau | Correctif prioritaire |
|---|---|---|---|
| A01 - Broken Access Control | Partiel | 🟡 | Vérifier isolation données inter-rôles |
| A02 - Cryptographic Failures | Oui | 🔴 | Clé JWT vide, token reset en clair |
| A03 - Injection | Non détecté | 🟢 | JPA prévient l'injection SQL |
| A04 - Insecure Design | Oui | 🟠 | Pas de multi-tenancy = fuite de données |
| A05 - Security Misconfiguration | Oui | 🔴 | Swagger public, Actuator exposé |
| A07 - Auth Failures | Oui | 🟠 | Pas de rate limiting |
| A09 - Security Logging | Partiel | 🟡 | Pas de MDC, pas d'audit trail complet |

---

## 7. PRÉPARATION MULTI-ÉTABLISSEMENTS (MULTI-TENANCY)

### 7.1 État actuel : Zéro multi-tenancy

Le projet est **single-tenant par conception**. Aucune colonne `school_id` ou `tenant_id` n'existe dans le modèle de données. Toutes les données de tous les établissements seraient mélangées dans les mêmes tables.

### 7.2 Architecture Multi-Tenant recommandée : Schema-per-Tenant

Pour 100 établissements scolaires, l'approche **schema-per-tenant sur PostgreSQL** est la plus adaptée :

```
PostgreSQL cluster
├── schema: public          ← Tables partagées (licences, plans)
├── schema: school_001      ← Établissement A (données isolées)
├── schema: school_002      ← Établissement B
└── schema: school_NNN      ← Établissement N
```

**Implémentation Spring Boot :**

```java
@Configuration
public class MultiTenantConfig {

    @Bean
    public MultiTenantConnectionProvider multiTenantConnectionProvider(DataSource ds) {
        return new SchemaBasedMultiTenantConnectionProvider(ds);
    }

    @Bean
    public CurrentTenantIdentifierResolver tenantIdentifierResolver() {
        return new JwtTenantIdentifierResolver(); // Extrait schoolId du JWT
    }
}

public class JwtTenantIdentifierResolver implements CurrentTenantIdentifierResolver {
    @Override
    public String resolveCurrentTenantIdentifier() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth instanceof JwtAuthenticationToken jwt) {
            return "school_" + jwt.getClaims().get("schoolId");
        }
        return "public";
    }
}
```

**Modifications JWT requises :**
```java
// Ajouter schoolId dans le token
claims.put("schoolId", user.getSchoolId());
claims.put("schoolCode", user.getSchool().getCode());
```

---

### 7.3 Table centrale de gestion des établissements

```sql
-- Schema public (partagé)
CREATE TABLE schools (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(20) UNIQUE NOT NULL,       -- 'SCHOOL_001'
    name VARCHAR(200) NOT NULL,
    country VARCHAR(100),
    city VARCHAR(100),
    schema_name VARCHAR(50) UNIQUE NOT NULL, -- 'school_001'
    plan_id BIGINT REFERENCES subscription_plans(id),
    active BOOLEAN NOT NULL DEFAULT true,
    max_students INT NOT NULL DEFAULT 500,
    max_teachers INT NOT NULL DEFAULT 50,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE subscription_plans (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,  -- 'STARTER', 'PROFESSIONAL', 'ENTERPRISE'
    max_students INT NOT NULL,
    max_teachers INT NOT NULL,
    price_monthly DECIMAL(10,2),
    features JSONB                -- Features activées par plan
);

CREATE TABLE school_licenses (
    id BIGSERIAL PRIMARY KEY,
    school_id BIGINT NOT NULL REFERENCES schools(id),
    valid_from DATE NOT NULL,
    valid_until DATE NOT NULL,
    status VARCHAR(20) NOT NULL,  -- 'ACTIVE', 'EXPIRED', 'SUSPENDED'
    payment_reference VARCHAR(100)
);
```

---

### 7.4 Gestion des quotas

```java
@Service
public class QuotaService {

    public void checkStudentQuota(Long schoolId) {
        School school = schoolRepo.findById(schoolId).orElseThrow();
        long currentStudents = studentRepo.countBySchoolId(schoolId);
        if (currentStudents >= school.getMaxStudents()) {
            throw new QuotaExceededException(
                "Quota élèves atteint: " + currentStudents + "/" + school.getMaxStudents()
            );
        }
    }

    public void checkLicense(Long schoolId) {
        SchoolLicense license = licenseRepo.findActiveBySchoolId(schoolId)
            .orElseThrow(() -> new LicenseExpiredException("Licence expirée ou inactive"));
        if (license.getValidUntil().isBefore(LocalDate.now())) {
            throw new LicenseExpiredException("Licence expirée le " + license.getValidUntil());
        }
    }
}
```

---

## 8. MIGRATION VERS LES MICROSERVICES

> **Bonne nouvelle :** Le projet a déjà amorcé la décomposition avec `billing-service` et `attendance-service` en architecture hexagonale. Cette section propose le découpage complet.

### 8.1 Service Authentification (`auth-service`)

**Port :** 8081 | **Base :** PostgreSQL `schema: auth`

**Responsabilités :**
- Gestion des utilisateurs (`Users`, `UsersProfil`)
- Authentification JWT (access + refresh tokens)
- Gestion des rôles et permissions
- Reset de mot de passe
- Session management

**APIs exposées :**
```
POST /auth/login
POST /auth/logout
POST /auth/refresh
POST /auth/register
POST /auth/password-reset-request
POST /auth/password-reset
GET  /auth/me
GET  /users/{id}/profile
PUT  /users/{id}/profile
```

**Événements publiés :**
- `UserCreatedEvent { userId, schoolId, roleType, email }`
- `UserDeactivatedEvent { userId, schoolId }`
- `PasswordChangedEvent { userId, timestamp }`

**Événements consommés :** Aucun

---

### 8.2 Service Élèves (`student-service`)

**Port :** 8082 | **Base :** PostgreSQL `schema: school_{id}`

**Responsabilités :**
- CRUD élèves et parents
- Dossiers scolaires (documents, photos)
- Historique des changements d'état

**APIs exposées :**
```
GET    /students
GET    /students/{id}
POST   /students
PUT    /students/{id}
DELETE /students/{id}
GET    /students/{id}/parent
GET    /students/{id}/documents
POST   /students/{id}/photo
GET    /parents
GET    /parents/{id}/students
```

**Événements publiés :**
- `StudentCreatedEvent { studentId, schoolId, parentId }`
- `StudentDeactivatedEvent { studentId, schoolId }`

**Événements consommés :**
- `InscriptionCreatedEvent` (pour mettre à jour le statut actif)

---

### 8.3 Service Inscriptions (`enrollment-service`)

**Port :** 8083 | **Base :** PostgreSQL `schema: school_{id}`

**Responsabilités :**
- Pré-inscriptions
- Inscriptions et réinscriptions
- Affectation des élèves aux classes
- Gestion des années scolaires et séquences

**APIs exposées :**
```
GET    /enrollments
POST   /enrollments
GET    /enrollments/{id}
PUT    /enrollments/{id}
GET    /enrollments/student/{studentId}
POST   /pre-enrollments
GET    /academic-years
GET    /classes
GET    /sections
GET    /sequences
```

**Événements publiés :**
- `InscriptionCreatedEvent { inscriptionId, studentId, classId, schoolId, anneeId }`
- `StudentClassChangedEvent { studentId, oldClassId, newClassId }`

**Événements consommés :**
- `StudentCreatedEvent`
- `PaymentCompletedEvent` (pour valider l'inscription)

---

### 8.4 Service Paiements (`billing-service`) — DÉJÀ EN COURS

**Port :** 8084 | **Base :** PostgreSQL `schema: billing_{school_id}`

**Responsabilités :**
- Enregistrement des paiements de scolarité
- Génération de reçus PDF
- Suivi des soldes et impayés
- Rapports financiers
- Intégration PSP (Mobile Money, virement)

**APIs exposées :**
```
GET    /payments
POST   /payments
GET    /payments/{id}
GET    /payments/student/{studentId}
GET    /payments/receipt/{id}
GET    /payments/summary
GET    /payments/overdue
POST   /payments/{id}/void
```

**Événements publiés :**
- `PaymentCompletedEvent { paymentId, studentId, amount, type, schoolId }`
- `PaymentOverdueEvent { studentId, dueDate, remainingAmount }`

**Événements consommés :**
- `InscriptionCreatedEvent` (créer le dossier de paiement)
- `StudentDeactivatedEvent` (archiver les paiements)

---

### 8.5 Service Notes (`grade-service`)

**Port :** 8085 | **Base :** PostgreSQL `schema: school_{id}`

**Responsabilités :**
- Saisie et modification des notes
- Calcul des moyennes et rangs
- Génération des bulletins PDF
- Statistiques de performance

**APIs exposées :**
```
GET    /grades
POST   /grades
POST   /grades/bulk
PUT    /grades/{id}
DELETE /grades/{id}
GET    /grades/student/{studentId}
GET    /grades/class/{classId}
GET    /grades/student/{studentId}/report
GET    /grades/class/{classId}/performance
POST   /bulletins/generate
```

**Événements publiés :**
- `GradeAddedEvent { gradeId, studentId, subjectId, value, teacherId }`
- `BulletinGeneratedEvent { bulletinId, studentId, period }`

**Événements consommés :**
- `InscriptionCreatedEvent`
- `StudentDeactivatedEvent`

---

### 8.6 Service Présences (`attendance-service`) — DÉJÀ EN COURS

**Port :** 8086 | **Base :** PostgreSQL `schema: school_{id}`

**Responsabilités :**
- Saisie des absences et retards
- Justification des absences
- Statistiques de présence
- Alertes parentales sur les absences

**APIs exposées :**
```
GET    /attendances
POST   /attendances/daily
GET    /attendances/student/{id}
GET    /attendances/summary
POST   /attendances/{id}/justify
GET    /attendances/class/{classId}/today
```

**Événements publiés :**
- `AbsenceRecordedEvent { studentId, date, justified, schoolId }`
- `AbsenceThresholdReachedEvent { studentId, count, threshold }`

**Événements consommés :**
- `InscriptionCreatedEvent`

---

### 8.7 Service Communication (`notification-service`)

**Port :** 8087 | **Base :** PostgreSQL `schema: notifications` + Redis

**Responsabilités :**
- Notifications WebSocket (déjà présent dans le monolithe)
- Envoi d'emails
- Envoi de SMS (Africa's Talking, Orange, MTN)
- Notifications Push (FCM)
- Templates de messages

**APIs exposées :**
```
POST   /notifications/send
GET    /notifications/user/{userId}
PUT    /notifications/{id}/read
POST   /notifications/bulk
GET    /templates
POST   /templates
```

**Événements consommés :**
- `GradeAddedEvent` → notifier le parent
- `AbsenceRecordedEvent` → notifier le parent
- `PaymentOverdueEvent` → rappel de paiement
- `BulletinGeneratedEvent` → bulletin disponible

---

### 8.8 Service Reporting (`reporting-service`)

**Port :** 8088 | **Base :** PostgreSQL read replica + Redis cache

**Responsabilités :**
- Tableaux de bord direction
- Statistiques globales
- Export Excel/PDF
- Rapports personnalisés

**APIs exposées :**
```
GET    /reports/dashboard
GET    /reports/enrollment-stats
GET    /reports/payment-summary
GET    /reports/attendance-summary
GET    /reports/grade-distribution
POST   /reports/custom
GET    /reports/export/{format}
```

**Événements consommés :**
- Tous les événements (pour agréger les métriques)

---

## 9. ARCHITECTURE CIBLE

### 9.1 Vue d'ensemble

```
╔═══════════════════════════════════════════════════════════════════╗
║                    CLIENTS                                         ║
║   Angular SPA   │   React Native App   │   Admin Portal            ║
╚═══════════════════════════════════════════════════════════════════╝
                              │
                              ▼
╔═══════════════════════════════════════════════════════════════════╗
║                  API GATEWAY (Kong / Spring Cloud Gateway)         ║
║   Rate Limiting │ Auth Filter │ CORS │ Load Balancing │ SSL        ║
╚═══════════════════════════════════════════════════════════════════╝
                              │
              ┌───────────────┼───────────────┐
              ▼               ▼               ▼
╔══════════╗ ╔══════════╗ ╔══════════╗ ╔══════════════╗
║  auth-   ║ ║ student- ║ ║enrollment║ ║   billing-   ║
║ service  ║ ║ service  ║ ║ service  ║ ║   service    ║
╚══════════╝ ╚══════════╝ ╚══════════╝ ╚══════════════╝
╔══════════╗ ╔══════════╗ ╔══════════╗ ╔══════════════╗
║  grade-  ║ ║attendance║ ║notifica- ║ ║  reporting-  ║
║ service  ║ ║ service  ║ ║tion-svc  ║ ║   service    ║
╚══════════╝ ╚══════════╝ ╚══════════╝ ╚══════════════╝
              │
              ▼
╔═══════════════════════════════════════════════════════════════════╗
║               MESSAGE BROKER (Apache Kafka)                        ║
║   Topics: student.events │ payment.events │ grade.events │ ...     ║
╚═══════════════════════════════════════════════════════════════════╝
              │
              ▼
╔═══════════════════════════════════════════════════════════════════╗
║                     DONNÉES                                        ║
║  PostgreSQL (multi-schema) │ Redis Cluster │ MinIO (fichiers)      ║
╚═══════════════════════════════════════════════════════════════════╝
              │
              ▼
╔═══════════════════════════════════════════════════════════════════╗
║               OBSERVABILITÉ                                        ║
║  Prometheus + Grafana │ ELK Stack │ Jaeger (distributed tracing)  ║
╚═══════════════════════════════════════════════════════════════════╝
              │
              ▼
╔═══════════════════════════════════════════════════════════════════╗
║           INFRASTRUCTURE (Kubernetes / Docker Swarm)               ║
║  Helm Charts │ HPA │ PodDisruptionBudget │ NetworkPolicies          ║
╚═══════════════════════════════════════════════════════════════════╝
```

### 9.2 Stack technologique cible

| Composant | Technologie | Justification |
|---|---|---|
| **Language** | Java 21 (LTS) | Virtual Threads, records, pattern matching |
| **Framework** | Spring Boot 3.3 | Natif GraalVM, Spring Security 6 |
| **Base de données** | PostgreSQL 16 | Multi-schema, partitionnement, JSON |
| **Cache** | Redis 7 Cluster | Sessions, cache distribué, pub/sub |
| **Message broker** | Apache Kafka | Événements entre microservices |
| **API Gateway** | Kong / SCG | Rate limiting, auth centralisée |
| **Service discovery** | Kubernetes DNS | Natif K8s |
| **Conteneurs** | Docker + K8s | Orchestration |
| **Storage** | MinIO | Photos élèves, documents PDF |
| **Logs** | ELK Stack | Centralisation, recherche |
| **Monitoring** | Prometheus + Grafana | Métriques, alertes |
| **Tracing** | OpenTelemetry + Jaeger | Tracing distribué |
| **CI/CD** | GitHub Actions + ArgoCD | GitOps |

### 9.3 Configuration Kubernetes de base

```yaml
# HPA pour auto-scaling
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: grade-service-hpa
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: grade-service
  minReplicas: 2
  maxReplicas: 10
  metrics:
    - type: Resource
      resource:
        name: cpu
        target:
          type: Utilization
          averageUtilization: 70
    - type: Resource
      resource:
        name: memory
        target:
          type: Utilization
          averageUtilization: 80
```

---

## 10. 50+ NOUVELLES FONCTIONNALITÉS

### 10.1 Administration (10 fonctionnalités)

| # | Fonctionnalité | Description | Valeur Métier | Priorité | Complexité |
|---|---|---|---|---|---|
| A1 | **Portail multi-établissements** | Console d'administration centrale pour gérer 100+ établissements depuis une seule interface | Opérationnel | 🔴 P0 | Élevée |
| A2 | **Gestion des licences** | Activation/désactivation automatique des établissements selon les licences payées | Financier | 🔴 P0 | Moyenne |
| A3 | **Tableau de bord super-admin** | Vue agrégée de tous les établissements : élèves totaux, revenus, alertes | Décisionnel | 🔴 P0 | Moyenne |
| A4 | **Sauvegarde automatique** | Backup quotidien chiffré vers S3/MinIO avec restauration en 1 clic | Sécurité | 🔴 P0 | Moyenne |
| A5 | **Audit trail complet** | Toutes les actions (qui a modifié quoi et quand) stockées et consultables | Conformité | 🟠 P1 | Faible |
| A6 | **Gestion des années scolaires** | Clôture/ouverture d'année avec archivage automatique des données N-1 | Opérationnel | 🟠 P1 | Moyenne |
| A7 | **Paramétrage des niveaux scolaires** | Configurer les sections, classes, coefficients par établissement | Flexibilité | 🟠 P1 | Faible |
| A8 | **Import en masse CSV/Excel** | Importer 500 élèves depuis un fichier Excel en moins de 2 minutes | Efficacité | 🟠 P1 | Moyenne |
| A9 | **Gestion des droits granulaires** | Permissions fines : CRUD par entité, par classe, par section | Sécurité | 🟡 P2 | Élevée |
| A10 | **Mode maintenance** | Basculer un établissement en maintenance sans affecter les autres | Opérationnel | 🟡 P2 | Faible |

---

### 10.2 Enseignants (8 fonctionnalités)

| # | Fonctionnalité | Description | Valeur Métier | Priorité | Complexité |
|---|---|---|---|---|---|
| E1 | **Cahier de texte numérique** | Enseignants saisissent les cours dispensés, devoirs donnés | Pédagogique | 🟠 P1 | Moyenne |
| E2 | **Saisie de notes hors ligne** | Application mobile avec sync différée en cas de coupure internet | Terrain | 🟠 P1 | Élevée |
| E3 | **Génération de bulletins en 1 clic** | Bulletins PDF générés automatiquement depuis les notes saisies | Efficacité | 🔴 P0 | Moyenne |
| E4 | **Tableau de suivi de classe** | Vue graphique des performances de la classe par matière | Pédagogique | 🟡 P2 | Faible |
| E5 | **Messagerie interne** | Communication directe enseignant ↔ direction ↔ parents | Communication | 🟠 P1 | Moyenne |
| E6 | **Gestion des devoirs** | Publication de devoirs avec date limite, correction en ligne | Pédagogique | 🟡 P2 | Élevée |
| E7 | **Fiche de présence digitale** | Liste d'appel sur tablette, signature numérique possible | Terrain | 🟠 P1 | Faible |
| E8 | **Planning de cours** | Emploi du temps de l'enseignant avec notifications de cours | Organisation | 🟡 P2 | Moyenne |

---

### 10.3 Élèves (7 fonctionnalités)

| # | Fonctionnalité | Description | Valeur Métier | Priorité | Complexité |
|---|---|---|---|---|---|
| EL1 | **Espace élève personnalisé** | Consulter ses notes, bulletins, absences, devoirs en ligne | Engagement | 🟠 P1 | Moyenne |
| EL2 | **Historique académique complet** | Dossier scolaire complet depuis l'entrée dans l'établissement | Traçabilité | 🟡 P2 | Faible |
| EL3 | **QR Code d'identité scolaire** | Carte d'élève numérique avec QR pour les contrôles | Terrain | 🟡 P2 | Faible |
| EL4 | **Suivi des objectifs personnels** | L'élève définit des objectifs de notes, suivi de progression | Engagement | 🟡 P2 | Moyenne |
| EL5 | **Bibliothèque numérique** | Accès aux manuels scolaires numérisés et exercices | Pédagogique | 🔵 P3 | Élevée |
| EL6 | **Quiz en ligne** | Évaluations formatives avec correction immédiate | Pédagogique | 🔵 P3 | Élevée |
| EL7 | **Classement de classe** | Voir son rang dans la classe (anonymisé pour les autres) | Motivation | 🟡 P2 | Faible |

---

### 10.4 Parents (6 fonctionnalités)

| # | Fonctionnalité | Description | Valeur Métier | Priorité | Complexité |
|---|---|---|---|---|---|
| P1 | **Application mobile parent** | Suivi en temps réel : notes, absences, paiements | Engagement | 🔴 P0 | Élevée |
| P2 | **Notifications SMS/WhatsApp** | Alertes automatiques pour absences, notes, paiements | Communication | 🔴 P0 | Moyenne |
| P3 | **Paiement mobile Money** | Orange Money, MTN, Wave directement depuis l'app | Financier | 🔴 P0 | Élevée |
| P4 | **Rendez-vous en ligne** | Réserver un créneau pour rencontrer un enseignant | Communication | 🟠 P1 | Moyenne |
| P5 | **Reçus de paiement numérique** | Reçu PDF envoyé par email à chaque paiement | Administratif | 🟠 P1 | Faible |
| P6 | **Justification d'absence en ligne** | Le parent justifie l'absence depuis son téléphone | Terrain | 🟠 P1 | Faible |

---

### 10.5 Finances (5 fonctionnalités)

| # | Fonctionnalité | Description | Valeur Métier | Priorité | Complexité |
|---|---|---|---|---|---|
| F1 | **Tableau de bord financier** | Revenus du mois, impayés, prévisions, comparaison N-1 | Décisionnel | 🔴 P0 | Moyenne |
| F2 | **Plan de paiement échelonné** | Configurer 3/4/6 tranches par famille | Flexibilité | 🟠 P1 | Moyenne |
| F3 | **Relances automatiques** | SMS/Email de rappel 7j, 3j, 1j avant la date limite | Recouvrement | 🟠 P1 | Moyenne |
| F4 | **Intégration comptable** | Export vers Sage, QuickBooks via API ou CSV normalisé | Comptabilité | 🟡 P2 | Élevée |
| F5 | **Gestion des bourses** | Suivi des élèves boursiers, réductions, exonérations | Équité | 🟡 P2 | Faible |

---

### 10.6 Statistiques & Reporting (5 fonctionnalités)

| # | Fonctionnalité | Description | Valeur Métier | Priorité | Complexité |
|---|---|---|---|---|---|
| S1 | **Tableaux de bord interactifs** | Graphiques Recharts/D3 pour direction, enseignants, parents | Décisionnel | 🟠 P1 | Moyenne |
| S2 | **Rapport de performance par établissement** | Comparaison inter-établissements (pour l'admin central) | Réseau | 🟡 P2 | Moyenne |
| S3 | **Export Excel avancé** | Rapports personnalisables, filtres par période/classe/section | Administratif | 🟠 P1 | Faible |
| S4 | **Indicateurs de qualité** | Taux de réussite, taux de rétention, évolution sur 3 ans | Qualité | 🟡 P2 | Moyenne |
| S5 | **Rapport MINESEC** | Génération automatique des rapports réglementaires Cameroun | Conformité | 🔴 P0 | Élevée |

---

### 10.7 Mobile (3 fonctionnalités)

| # | Fonctionnalité | Description | Valeur Métier | Priorité | Complexité |
|---|---|---|---|---|---|
| M1 | **App React Native** | Application unifiée élève/parent/enseignant | Accessibilité | 🔴 P0 | Très élevée |
| M2 | **Mode hors ligne** | Fonctionnement sans internet, sync à la reconnexion | Terrain | 🟠 P1 | Élevée |
| M3 | **Notifications Push FCM** | Alertes temps réel sur Android/iOS | Engagement | 🟠 P1 | Moyenne |

---

### 10.8 Intelligence Artificielle (6 fonctionnalités)

| # | Fonctionnalité | Description | Valeur Métier | Priorité | Complexité |
|---|---|---|---|---|---|
| IA1 | **Prédiction de décrochage scolaire** | Algorithme ML détectant les élèves à risque 2 mois avant les résultats | Prévention | 🟡 P2 | Élevée |
| IA2 | **Recommandation de soutien** | Suggère des exercices personnalisés selon les lacunes | Pédagogique | 🔵 P3 | Très élevée |
| IA3 | **Détection d'anomalies financières** | Alerter si un pattern de fraude est détecté dans les paiements | Sécurité | 🟡 P2 | Élevée |
| IA4 | **Prévision des inscriptions** | Prévoir le nombre d'élèves pour la prochaine rentrée | Planification | 🟡 P2 | Moyenne |
| IA5 | **Assistant virtuel** | Chatbot répondant aux questions fréquentes des parents | Service | 🔵 P3 | Élevée |
| IA6 | **OCR pour les bulletins papier** | Numériser les anciens bulletins physiques via photo | Modernisation | 🔵 P3 | Élevée |

---

### 10.9 Communication (5 fonctionnalités)

| # | Fonctionnalité | Description | Valeur Métier | Priorité | Complexité |
|---|---|---|---|---|---|
| C1 | **Messagerie établissement** | Canal de communication interne avec fils de discussion | Collaboration | 🟠 P1 | Élevée |
| C2 | **Annonces et circulaires** | Publier des annonces officielles avec accusé de réception | Communication | 🟠 P1 | Faible |
| C3 | **SMS groupé par classe** | Envoyer un SMS à tous les parents d'une classe en 2 clics | Urgence | 🔴 P0 | Moyenne |
| C4 | **Réunion parents-profs virtuelle** | Intégration Zoom/Meet pour les conseils de classe en ligne | Modernité | 🔵 P3 | Élevée |
| C5 | **Sondage et enquête** | Recueillir l'avis des parents/enseignants via formulaire | Amélioration | 🔵 P3 | Faible |

---

## 11. ROADMAP 12 MOIS

### Phase 1 — Corrections Critiques (Mois 1-2)
**Durée :** 8 semaines | **Difficulté :** Moyenne | **Impact :** Stabilité & Sécurité

| Tâche | Responsable | Semaine |
|---|---|---|
| Corriger clé JWT vide par défaut + rotation | Sécurité | S1 |
| Remplacer `@Data` par `@Getter/@Setter` sur entités JPA | Dev | S1 |
| Supprimer les `findAll()` non paginés (SearchService, ParentService, StudentExportService) | Dev | S1-S2 |
| Activer Redis et configurer le cache distribué | Infra | S2 |
| Sécuriser Swagger derrière `ROLE_ADMIN` | Sécurité | S1 |
| Ajouter Rate Limiting sur `/auth/*` (Bucket4j) | Sécurité | S2 |
| Corriger la typo `montanRestant` → `montantRestant` (migration Flyway) | DB | S2 |
| Unifier les deux `UserDetailsServiceImpl` | Dev | S1 |
| Sécuriser les tokens de reset (hashing SHA-256) | Sécurité | S3 |
| Réduire `show-details=when-authorized` sur Actuator | Sécurité | S1 |
| Ajouter MDC filter (requestId, userId) | Dev | S3 |
| Écrire tests unitaires pour les services critiques (80% coverage visé) | Dev | S3-S8 |

**Impact attendu :** Système stable et sécurisé pour un établissement.

---

### Phase 2 — Optimisations (Mois 3-4)
**Durée :** 8 semaines | **Difficulté :** Moyenne | **Impact :** Performance

| Tâche | Responsable |
|---|---|
| Migrer MySQL → PostgreSQL | DB |
| Ajouter les index manquants identifiés | DB |
| Optimiser les rapports étudiants (requêtes consolidées) | Dev |
| Ajouter `@EntityGraph` / JOIN FETCH sur les relations N+1 | Dev |
| Implémenter la stratégie de cache Redis complète | Dev |
| Pool de connexions : augmenter `maximum-pool-size=50` | Infra |
| Refactoriser `GradeService` (565 lignes) en 4 services | Dev |
| Refactoriser `StudentService` en services dédiés | Dev |
| Créer des projections JPA pour les lectures légères | Dev |
| Mettre en place le monitoring (Prometheus + Grafana) | DevOps |

**Impact attendu :** Supporte 500 utilisateurs simultanés sur un établissement.

---

### Phase 3 — Scalabilité (Mois 5-6)
**Durée :** 8 semaines | **Difficulté :** Élevée | **Impact :** Multi-tenant

| Tâche | Responsable |
|---|---|
| Implémenter le multi-tenancy (schema-per-tenant PostgreSQL) | Architecte |
| Créer la table `schools`, `subscription_plans`, `licenses` | DB |
| Modifier le JWT pour inclure `schoolId` | Dev |
| Implémenter `QuotaService` | Dev |
| Créer le portail super-admin | Dev |
| Implémenter la gestion des licences | Dev |
| Containeriser le monolithe (Docker) | DevOps |
| Déployer sur Kubernetes avec HPA | DevOps |
| Configurer le CI/CD (GitHub Actions) | DevOps |
| Tests de charge (Gatling) : viser 500 utilisateurs simultanés | QA |

**Impact attendu :** 10 établissements supportés avec isolation des données.

---

### Phase 4 — Microservices (Mois 7-9)
**Durée :** 12 semaines | **Difficulté :** Très élevée | **Impact :** Scalabilité indépendante

| Tâche | Responsable |
|---|---|
| Compléter `billing-service` (hexagonal, Kafka) | Dev |
| Compléter `attendance-service` (hexagonal, Kafka) | Dev |
| Créer `auth-service` (extraire de monolithe) | Dev |
| Créer `student-service` | Dev |
| Créer `enrollment-service` | Dev |
| Créer `grade-service` | Dev |
| Créer `notification-service` (email, SMS, Push) | Dev |
| Déployer Apache Kafka | DevOps |
| Déployer API Gateway (Kong) | DevOps |
| Intégrer distributed tracing (Jaeger/OpenTelemetry) | DevOps |
| Centralisation des logs (ELK Stack) | DevOps |
| Migration progressive avec strangler fig pattern | Architecte |

**Impact attendu :** Chaque service scale indépendamment. 50 établissements supportés.

---

### Phase 5 — Cloud Native (Mois 10-12)
**Durée :** 12 semaines | **Difficulté :** Élevée | **Impact :** Production à grande échelle

| Tâche | Responsable |
|---|---|
| App React Native (parents + élèves) | Mobile |
| Paiement Mobile Money (Orange, MTN, Wave) | Dev |
| Module IA prédiction décrochage | Data Science |
| Rapport MINESEC automatisé | Dev |
| SMS groupé par classe | Dev |
| Sauvegarde automatique chiffrée | DevOps |
| Tests de charge finaux (Gatling) : 1000+ utilisateurs | QA |
| Audit de sécurité par un tiers | Sécurité |
| Documentation API complète (OpenAPI 3.1) | Dev |
| Formation équipes des établissements | Métier |
| Déploiement progressif : 10 → 50 → 100 établissements | DevOps |

**Impact attendu :** 100 établissements, 20 000 élèves, production stable.

---

## 12. RAPPORT FINAL

### Résumé Exécutif

Le projet `school-management` est un point de départ honnête pour un système scolaire mono-établissement. La décomposition en microservices est déjà amorcée (`billing-service`, `attendance-service`) avec une architecture hexagonale correcte. Cependant, le passage à 100 établissements et 20 000 élèves nécessite une refonte architecturale profonde sur 12 mois minimum.

---

### Problèmes Critiques (à traiter en priorité absolue)

1. **Clé JWT vide par défaut** — Vulnérabilité de sécurité critique
2. **`findAll()` non paginés** — Crash mémoire garanti à grande échelle
3. **Redis désactivé** — Pas de scalabilité horizontale possible
4. **`@Data` sur entités JPA** — Risque de boucle infinie et fuites mémoire
5. **Zéro multi-tenancy** — Isolation des données impossible
6. **4 fichiers de tests** — Risque de régression à chaque déploiement
7. **Swagger public** — Exposition de l'architecture interne

---

### Améliorations Prioritaires

| Priorité | Action | Effort | Impact |
|---|---|---|---|
| 🔴 P0 | Sécuriser JWT et corriger les `findAll()` | 1 semaine | Critique |
| 🔴 P0 | Activer Redis | 2 jours | Performance |
| 🔴 P0 | Implémenter multi-tenancy | 4 semaines | Business |
| 🟠 P1 | Migrer vers PostgreSQL | 2 semaines | Performance |
| 🟠 P1 | Écrire les tests (>80% coverage) | 4 semaines | Qualité |
| 🟠 P1 | App mobile React Native | 3 mois | Business |
| 🟠 P1 | Paiement Mobile Money | 3 semaines | Business |

---

### Architecture Future

```
[Kong API Gateway]
       │
       ├──► auth-service     (JWT, roles, users)
       ├──► student-service  (élèves, parents)
       ├──► enrollment-svc   (inscriptions, classes)
       ├──► billing-service  (paiements, reçus)       ← En cours
       ├──► grade-service    (notes, bulletins)
       ├──► attendance-svc   (présences, absences)    ← En cours
       ├──► notification-svc (email, SMS, push)
       └──► reporting-svc    (dashboards, exports)

[Kafka] → Event Sourcing entre services
[PostgreSQL multi-schema] → Isolation par établissement
[Redis Cluster] → Cache distribué
[MinIO] → Documents et photos
[ELK + Jaeger] → Observabilité
[Kubernetes + HPA] → Auto-scaling
```

---

### Découpage Microservices (résumé)

| Service | Port | DB Schema | Technologie clé |
|---|---|---|---|
| `auth-service` | 8081 | `auth` | JWT, BCrypt, Redis sessions |
| `student-service` | 8082 | `school_{id}` | MinIO pour photos |
| `enrollment-service` | 8083 | `school_{id}` | Kafka events |
| `billing-service` | 8084 | `billing_{id}` | Mobile Money PSP |
| `grade-service` | 8085 | `school_{id}` | PDF generation |
| `attendance-service` | 8086 | `school_{id}` | WebSocket |
| `notification-service` | 8087 | `notifications` | FCM, Africa's Talking |
| `reporting-service` | 8088 | Read replica | Grafana, Excel export |

---

### Roadmap 12 Mois (vue condensée)

```
Mois 1-2  : STABILISATION   → Sécurité, tests, fix critiques, Redis
Mois 3-4  : OPTIMISATION    → PostgreSQL, index, cache, refactoring
Mois 5-6  : MULTI-TENANT    → Schema-per-tenant, admin central, licences
Mois 7-9  : MICROSERVICES   → 8 services indépendants + Kafka + Kong
Mois 10-12: CLOUD NATIVE    → Mobile app, IA, 100 établissements
```

---

### Conclusion

Ce projet a le potentiel de devenir une plateforme EdTech robuste pour l'Afrique francophone. Les décisions techniques de base sont correctes (Spring Boot 3, JWT, Flyway, hexagonal dans les nouveaux services). La dette technique identifiée est corrigible. Le chemin vers 100 établissements est clair mais demande discipline, investissement et une équipe structurée.

**Priorité absolue : ne pas déployer en production multi-établissements sans implémenter le multi-tenancy.** Les données d'un établissement seraient accessibles aux utilisateurs d'un autre — risque légal, réputationnel et sécuritaire majeur.

---

*Rapport généré le : 2 juin 2026*
*Version analysée : backend v1.x (Flyway V11)*
*Lignes de code analysées : ~15 000 lignes Java, 11 migrations SQL*


public class UserDetailsServiceImpl implements UserDetailsService {

    private final UsersRepository usersRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Users user = usersRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        List<GrantedAuthority> authorities = user.getAuthorities().stream().collect(Collectors.toList());

        return new AuthenticatedUserPrincipal(
                user.getId(),
                user.getUsername(),
                user.getPassword(),
                user.isEnabled(),
                authorities
        );
    }
}








# Scan backend

Date du scan: 2026-05-24

## Resume executif

- Backend Java 21 avec Spring Boot `3.5.14`, Gradle multi-projet et MySQL.
- Application principale dans `backend/src` plus deux sous-services: `billing-service` et `attendance-service`.
- 189 fichiers Java dans l'application principale, 29 dans `attendance-service`, 23 dans `billing-service`.
- Architecture principale plutot classique `controller/service/repository/model/dto`.
- Sous-services en architecture plus nette type ports/adapters.
- Points a prioriser: securiser les secrets par defaut, clarifier le partage monolithe/services, renforcer les tests, aligner Flyway/JPA/Docker et normaliser les APIs.

## Stack et configuration

- Java: toolchain Java 21.
- Framework: Spring Boot `3.5.14`.
- Build: Gradle multi-project.
- Base de donnees: MySQL, Spring Data JPA, Flyway.
- Securite: Spring Security, JWT avec JJWT.
- Mapping: MapStruct et Lombok.
- API docs: Springdoc OpenAPI.
- Observabilite: Spring Actuator, Micrometer, Prometheus.
- Exports: Apache POI pour Excel, iText pour PDF.
- Notifications: Firebase Admin SDK, WebSocket dans l'application principale.
- Tests: JUnit 5, Mockito, AssertJ, Spring Security Test, Testcontainers declare dans l'application principale.

## Squelette actuel

```text
backend/
|-- build.gradle
|-- settings.gradle
|-- gradle.properties
|-- Dockerfile
|-- services/
|   |-- README.md
|   |-- attendance-service/
|   |   |-- build.gradle
|   |   |-- Dockerfile
|   |   `-- src/
|   |       |-- main/java/com/school/attendance/
|   |       |   |-- AttendanceServiceApplication.java
|   |       |   |-- adapter/
|   |       |   |   |-- config/
|   |       |   |   |-- in/rest/
|   |       |   |   |-- in/security/
|   |       |   |   |-- out/persistence/
|   |       |   |   `-- out/school/
|   |       |   |-- application/
|   |       |   |   |-- port/in/
|   |       |   |   |-- port/out/
|   |       |   |   `-- service/
|   |       |   `-- domain/model/
|   |       |-- main/resources/
|   |       |   |-- application.yml
|   |       |   `-- db/migration/
|   |       `-- test/java/
|   `-- billing-service/
|       |-- build.gradle
|       |-- Dockerfile
|       `-- src/
|           |-- main/java/com/school/billing/
|           |   |-- BillingServiceApplication.java
|           |   |-- adapter/
|           |   |   |-- in/rest/
|           |   |   |-- in/security/
|           |   |   `-- out/persistence/
|           |   |-- application/
|           |   |   |-- port/in/
|           |   |   |-- port/out/
|           |   |   `-- service/
|           |   `-- domain/model/
|           |-- main/resources/
|           |   |-- application.yml
|           |   `-- db/migration/
|           `-- test/java/
`-- src/
    |-- main/java/com/school/
    |   |-- SchoolManagementApplication.java
    |   |-- management/
    |   |   |-- config/
    |   |   |-- controller/
    |   |   |-- dto/
    |   |   |-- enums/
    |   |   |-- mappers/
    |   |   |-- model/
    |   |   |-- repository/
    |   |   |-- security/
    |   |   `-- service/
    |   |-- service/
    |   `-- validator/
    |-- main/resources/
    |   |-- application.properties
    |   |-- Class_GBP.jpg
    |   `-- db/migration/
    `-- test/java/com/school/management/service/
```

## Modules Gradle

`settings.gradle` declare:

```text
rootProject.name = school-management
include services:billing-service
include services:attendance-service
```

Application principale:

- Groupe: `school`.
- Version: `0.0.1-SNAPSHOT`.
- Port par defaut: `8080`.
- Context path: `/api`.

Sous-services:

- `billing-service`: port `8082`, context path `/api`.
- `attendance-service`: port `8083`, context path `/api`.

## Scan des packages principaux

Application principale `com.school.management`:

| Package | Role |
|---|---|
| `config` | Securite, bootstrap admin dev |
| `controller` | Endpoints REST |
| `dto` | Objets de transfert API |
| `enums` | Types metier |
| `mappers` | MapStruct entity/DTO |
| `model` | Entites JPA |
| `repository` | Spring Data repositories |
| `security` | JWT, principal, details utilisateur |
| `service` | Logique applicative/metier |

Packages hors `management`:

- `com.school.validator`: validateurs `Student`, `Payment`, `Grade`, `Enseignant`, `Class`.
- `com.school.service`: services transverses analytics, backup, notification, push notification.

## Scan des controles REST

Application principale, base `/api`:

| Controller | Base route |
|---|---|
| `AuthController` | `/auth` |
| `UsersController` | `/users` |
| `StudentController` | `/students` |
| `TeacherController` | `/teachers` |
| `ClassRoomController` | `/classes` |
| `SubjectController` | `/subject` |
| `GradeController` | `/grades` |
| `TrimestreController` | `/trimestre` |
| `SequenceController` | `/sequence` |
| `SectionController` | `/section` |
| `ParentController` | `/parents` |
| `InscriptionController` | `/inscription` |
| `MontantController` | `/montant` |
| `ReportController` | `/reports` |
| `SettingsController` | `/settings` |
| `SupportController` | `/support` |
| `AttendanceController` | `/attendance` |
| `AnneeScolaireController` | `/annees-scolaires` |
| `HomeController` | `/` et `/health` |
| `CustomErrorController` | `/error` |

Sous-services:

| Service | Controller | Base route |
|---|---|---|
| `billing-service` | `PaymentController` | `/api/payments` |
| `attendance-service` | `AttendanceController` | `/api/attendance` |

## Scan des composants backend

Repartition globale observee dans backend principal et sous-services:

| Type | Nombre |
|---|---:|
| DTO/API objects | 53 |
| Services | 31 |
| Repositories | 30 |
| Entities/Models | 35 |
| Controllers | 22 |
| Mappers | 17 |
| Autres Java | 53 |

Services principaux detectes:

- `AuthentificationService`, `UsersService`, `StudentService`, `TeacherService`, `ClassRoomService`.
- `SubjectService`, `GradeService`, `TrimestreService`, `SequenceService`, `SectionService`.
- `ParentService`, `InscriptionStudentService`, `PaymentService`, `MontantService`.
- `ReportService`, `PdfGenerationService`, `NotificationService`, `SupportService`, `SettingsService`.
- `AnneeScolaireService`, `AffectationService`, `LogActiviteService`.

## Donnees et migrations

Application principale:

- `V1__baseline_school_management.sql`.
- `V2__create_settings_support_tables.sql`.
- `V3__create_refresh_tokens.sql`.
- `V4__add_password_reset_expiration.sql`.
- `V5__create_absences_table.sql`.

Sous-services:

- `attendance-service`: `V1__create_attendance_schema.sql`, `V2__migrate_legacy_absences.sql`.
- `billing-service`: `V1__create_billing_schema.sql`.

Configuration actuelle:

- Application principale: `spring.jpa.hibernate.ddl-auto=${JPA_DDL_AUTO:validate}` et Flyway active par defaut.
- Docker compose: `JPA_DDL_AUTO` vaut `update` par defaut et `FLYWAY_ENABLED` vaut `false` par defaut pour `spring-api`.
- Sous-services: `ddl-auto: validate` et Flyway active.

## Securite

- Authentification JWT stateless.
- `authInterceptor` cote frontend et filtres JWT cote backend.
- Endpoints publics principaux: `/auth/**`, reset password, Swagger, health/info actuator, erreurs.
- CORS limite a `http://localhost:4200` et `http://127.0.0.1:4200`.
- `DevAdminBootstrap` cree un admin de developpement si active et si aucun utilisateur n'existe.

Point critique:

- Des valeurs par defaut sensibles existent dans `DevAdminBootstrap`, `application.properties`, `application.yml` et `docker-compose.yml`: secret JWT, email admin, mot de passe admin. Elles doivent etre retirees des valeurs par defaut partagees.

## Deploiement local Docker

`docker-compose.yml` declare:

- `mysql-db`: MySQL 8.4.
- `spring-api`: application principale sur `8080`.
- `billing-service`: service paiement sur `8082`.
- `attendance-service`: service presence sur `8083`.
- `angular-app`: frontend Nginx sur `4200`.

Nginx frontend proxifie:

- `/api/` vers `spring-api:8080/api/`.
- `/billing-api/` vers `billing-service:8082/api/`.
- `/attendance-api/` vers `attendance-service:8083/api/`.

## Tests detectes

- Application principale: 4 tests service.
  - `AuthentificationServiceTest`.
  - `SettingsServiceTest`.
  - `SupportServiceTest`.
  - `UsersServiceTest`.
- Sous-services: 2 tests application.
  - `PaymentApplicationServiceTest`.
  - `AttendanceApplicationServiceTest`.

## Points forts

- Stack recente: Java 21 et Spring Boot 3.5.
- Migrations Flyway presentes.
- Observabilite deja prevue avec Actuator et Prometheus.
- Sous-services avec architecture ports/adapters claire.
- Docker compose couvre toute la chaine localement.
- MapStruct et DTOs separent deja une partie des modeles API et JPA.
- `open-in-view=false`, bon choix pour eviter les chargements paresseux non controles dans la vue.

## Points de friction detectes

- L'application principale et `attendance-service` exposent tous deux une API attendance, ce qui rend la responsabilite fonctionnelle ambigu.
- Architecture differente entre monolithe principal et sous-services.
- Nommage API inegal: `/subject` vs `/students`, `/trimestre`, `/montant`, `/annees-scolaires`.
- Secrets et comptes par defaut presents dans les fichiers versionnes.
- CORS code en dur dans plusieurs `SecurityConfig`.
- Configuration base de donnees differente entre local compose et configuration applicative: `update` + Flyway off dans compose, `validate` + Flyway on dans l'app.
- Peu de tests pour le nombre de controllers, services, repositories et regles de securite.
- Plusieurs services transverses vivent hors `com.school.management`, ce qui peut compliquer le component scanning et la lisibilite.

## Ameliorations proposees

### Priorite P0

1. Retirer les secrets des valeurs par defaut versionnees: JWT secret, email admin, mot de passe admin.
2. Desactiver le bootstrap admin par defaut hors profil `dev`, et exiger des variables d'environnement explicites.
3. Aligner Docker et l'application sur Flyway: eviter `ddl-auto=update` et `FLYWAY_ENABLED=false` comme valeurs par defaut partagees.
4. Clarifier la source de verite attendance: monolithe, service dedie, ou phase de migration avec routes legacy documentees.
5. Externaliser CORS dans la configuration par environnement.

### Priorite P1

1. Introduire un format d'erreur API unifie pour tous les controllers et sous-services.
2. Ajouter une version d'API stable, par exemple `/api/v1`, avant d'etendre davantage les routes.
3. Standardiser les routes REST: pluriel pour les collections, conventions CRUD, pagination et filtres.
4. Ajouter des tests `@WebMvcTest` pour les controllers critiques et des tests securite pour roles/JWT.
5. Ajouter des tests repositories avec Testcontainers pour les requetes JPA importantes.
6. Mutualiser les dependances Gradle communes des sous-services pour eviter la duplication.

### Priorite P2

1. Migrer progressivement l'application principale vers le meme modele ports/adapters que les sous-services, domaine par domaine.
2. Ajouter des correlation IDs et logs structures pour suivre une requete entre frontend, spring-api et sous-services.
3. Completer la documentation OpenAPI avec exemples de payloads et codes d'erreur.
4. Ajouter des health checks applicatifs pour dependances externes: DB, services internes, Firebase si utilise.
5. Mettre en place des tests contractuels entre frontend et backend sur les DTOs critiques.

## Commandes utiles

```bash
./gradlew test
./gradlew bootJar
./gradlew :services:billing-service:test
./gradlew :services:attendance-service:test
docker compose up --build
```

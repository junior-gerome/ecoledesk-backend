# Création d'une nouvelle base depuis les entités — méthode Feynman

## Résumé en une idée

Imagine que les entités Java sont le plan d'une école et que MySQL est le terrain. Hibernate lisait le plan et vérifiait que chaque salle existait. La salle `absences` manquait ; il a donc refusé de démarrer. Au lieu de recréer les anciennes migrations, nous avons demandé à Hibernate de dessiner un nouveau plan SQL à partir des entités actuelles, puis nous l'avons confié à Flyway comme nouvelle fondation.

## Le problème rencontré

L'application ne démarrait pas avec :

```text
Schema-validation: missing table [absences]
```

Les erreurs `userAccountRepository`, `EntityManagerFactory` et Tomcat étaient des conséquences en cascade :

1. Hibernate valide les tables au démarrage.
2. La table `absences` est absente.
3. Hibernate ne peut pas créer l'`EntityManagerFactory`.
4. Les repositories JPA ne peuvent pas être créés.
5. Le filtre JWT dépend d'un repository ; Tomcat ne peut donc pas démarrer.

L'entité `Absence` demande explicitement la table `absences` :

```java
@Table(name = "absences")
```

La configuration `spring.jpa.hibernate.ddl-auto=validate` explique le comportement : `validate` contrôle le schéma, mais ne crée aucune table.

## Blocage principal

Les anciennes migrations Flyway étaient supprimées volontairement pour éviter des doublons et incompatibilités. Il ne fallait donc pas les restaurer.

Un second blocage existait : une migration de création complète appliquée sur l'ancienne base `bd_gsbp` tenterait de recréer les tables déjà présentes. Elle provoquerait des erreurs de type « table already exists ».

## Solution choisie

Nous avons créé une nouvelle baseline Flyway depuis l'état actuel des entités :

```text
Entités Java actuelles
        ↓
Hibernate génère le SQL
        ↓
build/schema-current.sql
        ↓
V1__baseline_current_schema.sql
        ↓
Nouvelle base MySQL vide
        ↓
Flyway crée les tables
        ↓
Hibernate valide le résultat
```

Cette solution est propre car Flyway devient à nouveau la mémoire officielle du schéma.

## Étapes réalisées

### 1. Identifier la vraie cause

Nous avons lu la dernière cause (`Caused by`) du log plutôt que la première erreur affichée. Elle indiquait exactement la table absente : `absences`.

### 2. Vérifier l'entité concernée

Le fichier `src/main/java/com/school/platform/attendance/domain/model/Absence.java` a confirmé :

- table : `absences` ;
- clé primaire : `id` ;
- relation : `student_id` vers `students` ;
- colonnes : `date`, `status`, `hours`, `justified`, `justification_note`, `updated_at`.

### 3. Ne pas restaurer les anciennes migrations

Git indiquait que 22 migrations existaient dans l'historique, mais qu'elles étaient supprimées localement. Elles n'ont pas été restaurées, conformément au choix de repartir sur le modèle actuel.

### 4. Ajouter un profil d'export sans danger

Le fichier [application-schema-export.properties](../src/main/resources/application-schema-export.properties) a été créé. Ses propriétés importantes sont :

```properties
spring.jpa.hibernate.ddl-auto=none
spring.jpa.properties.jakarta.persistence.schema-generation.database.action=none
spring.jpa.properties.jakarta.persistence.schema-generation.scripts.action=create
spring.jpa.properties.jakarta.persistence.schema-generation.scripts.create-target=build/schema-current.sql
spring.flyway.enabled=false
```

Explication simple :

- `database.action=none` : Hibernate n'écrit rien dans MySQL ;
- `scripts.action=create` : Hibernate écrit le SQL dans un fichier ;
- `create-target` : indique le fichier produit ;
- `spring.flyway.enabled=false` : aucun script Flyway n'est lancé durant cet export.

### 5. Générer le schéma

La commande suivante a généré le fichier SQL :

```powershell
.\gradlew.bat bootRun --args="--spring.profiles.active=schema-export --spring.main.web-application-type=none"
```

Le résultat est `build/schema-current.sql` (28 Ko). Il contient 42 tables, leurs clés primaires, contraintes uniques et clés étrangères. La table `absences` et sa relation vers `students` y sont présentes.

Le processus est resté actif à cause de composants applicatifs en arrière-plan. Il a été arrêté après la génération du fichier ; la génération avait déjà été effectuée.

### 6. Créer la baseline Flyway

Le SQL généré a été placé dans :

- [V1__baseline_current_schema.sql](../src/main/resources/db/migration/V1__baseline_current_schema.sql)

C'est la première migration de la nouvelle histoire Flyway. Elle est destinée à une base vide et crée les 42 tables.

### 7. Vérifier le projet

La compilation suivante réussit :

```powershell
.\gradlew.bat compileJava --console=plain
```

## Créer réellement la nouvelle base

La nouvelle migration est prête, mais **la base `bd_gsbp_v2` n'a pas été créée automatiquement pendant cette intervention**. Cette séparation évite toute modification accidentelle de `bd_gsbp`.

Pour créer une nouvelle base vide et la remplir avec la baseline :

```powershell
$env:DB_URL = 'jdbc:mysql://localhost:3306/bd_gsbp_v2?createDatabaseIfNotExist=true&serverTimezone=UTC&useSSL=false'
$env:DB_USERNAME = 'root'
$env:DB_PASSWORD = 'votre_mot_de_passe'
.\gradlew.bat bootRun
```

Ce qui se passe :

1. MySQL crée `bd_gsbp_v2` si elle n'existe pas.
2. Flyway lit `V1__baseline_current_schema.sql`.
3. Flyway crée toutes les tables de la baseline.
4. Hibernate exécute `validate`.
5. L'application démarre si le schéma correspond aux entités.

Ne lancez pas cette baseline contre `bd_gsbp`, car les tables déjà présentes entreraient en conflit avec les instructions `CREATE TABLE`.

## Ce qui reste à faire

1. Créer et tester `bd_gsbp_v2` avec la commande ci-dessus.
2. Vérifier le démarrage complet de l'application.
3. Exporter ou migrer les données de `bd_gsbp` seulement si elles sont nécessaires.
4. Pour toute évolution future, créer une nouvelle migration `V2`, `V3`, etc. ; ne modifiez jamais `V1` après son application dans un environnement partagé.

## Résumé ultra-simple

La table `absences` manquait, donc Hibernate a bloqué le démarrage. Les anciennes migrations étant supprimées volontairement, nous avons généré un nouveau SQL depuis les entités actuelles. Ce SQL est devenu la migration Flyway `V1`. Il faut maintenant l'appliquer uniquement sur une nouvelle base vide, telle que `bd_gsbp_v2`.
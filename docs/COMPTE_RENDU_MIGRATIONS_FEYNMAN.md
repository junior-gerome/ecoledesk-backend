# Compte rendu — reprise des migrations Flyway (méthode Feynman)

## Résultat en une phrase

Le blocage ne venait ni de `UserAccountRepository`, ni de JWT : Hibernate refusait de démarrer car l'entité `Absence` attendait une table `absences` qui n'existait pas dans la base.

## L'explication simple

Imagine que les entités Java sont le plan d'une école et que MySQL est le bâtiment réel. `spring.jpa.hibernate.ddl-auto=validate` joue le rôle d'un inspecteur : il compare le plan au bâtiment, mais ne construit rien. Il a trouvé une salle `absences` indiquée sur le plan, mais absente du bâtiment ; il a donc arrêté le démarrage.

Flyway est le carnet de chantier : chaque migration SQL est une étape numérotée qui construit le bâtiment. Les anciennes étapes avaient été supprimées volontairement à cause de doublons et d'incompatibilités. Il fallait donc repartir d'un **nouveau plan complet**, et non réutiliser une seule ancienne étape.

## Diagnostic de départ

Le dernier `Caused by` du journal était :

```text
Schema-validation: missing table [absences]
```

La cause remonte ainsi :

1. Hibernate ne peut pas créer `entityManagerFactory`.
2. Les repositories JPA ne peuvent donc pas recevoir leur `EntityManager`.
3. `UserDetailsServiceImpl`, puis le filtre JWT, échouent.
4. Tomcat ne démarre pas.

Les erreurs sur JWT, `userAccountRepository` et Tomcat étaient des conséquences, pas la cause racine.

## Étapes réalisées

1. J'ai lu la pile d'erreurs jusqu'au dernier `Caused by`.
2. J'ai vérifié l'entité `Absence` : elle contient `@Table(name = "absences")`.
3. J'ai vérifié le dossier `src/main/resources/db/migration` : les anciennes migrations étaient supprimées dans Git.
4. J'ai vérifié l'historique Git : l'ancienne migration `V5__create_absences_table.sql` existait bien dans le dépôt, mais elle ne devait pas être restaurée sans votre accord, car vous aviez supprimé l'ancien jeu de migrations.
5. J'ai créé le profil [application-schema-export.properties](../src/main/resources/application-schema-export.properties).
6. Ce profil demande à Hibernate de produire le SQL à partir des entités, sans écrire dans MySQL :

```properties
spring.jpa.hibernate.ddl-auto=none
spring.jpa.properties.jakarta.persistence.schema-generation.database.action=none
spring.jpa.properties.jakarta.persistence.schema-generation.scripts.action=create
spring.jpa.properties.jakarta.persistence.schema-generation.scripts.create-target=build/schema-current.sql
spring.flyway.enabled=false
```

7. J'ai lancé l'application avec le profil `schema-export` ; Hibernate a créé `build/schema-current.sql`.
8. J'ai contrôlé que ce fichier contient `create table absences` et sa clé étrangère vers `students`.
9. J'ai transformé ce script complet en nouvelle baseline Flyway : [V1__baseline_current_schema.sql](../src/main/resources/db/migration/V1__baseline_current_schema.sql).
10. Le script contient 42 tables et leurs contraintes, puis la compilation Java a été vérifiée avec succès.

## Blocages rencontrés et résolution

| Blocage | Pourquoi | Résolution |
|---|---|---|
| Erreur sur `userAccountRepository` | C'était une conséquence d'un `EntityManagerFactory` absent. | Lecture du dernier `Caused by` pour trouver la vraie erreur. |
| Table `absences` absente | `validate` vérifie le schéma sans le créer. | Génération d'une baseline SQL depuis les entités actuelles. |
| Anciennes migrations supprimées | Les restaurer aurait réintroduit les doublons supprimés. | Elles ont été laissées supprimées ; une nouvelle migration `V1` a été créée. |
| Application d'une baseline sur `bd_gsbp` existante | La base contient déjà une partie des tables ; `CREATE TABLE` provoquerait des doublons. | La baseline est destinée à une base MySQL neuve et vide. |
| Processus d'export encore actif | Des composants Spring gardaient des threads actifs après la génération. | L'export a été arrêté après la création de `build/schema-current.sql`. |

## Comment créer réellement la nouvelle base

Je n'ai **pas** créé de base MySQL ni modifié `bd_gsbp`. J'ai créé le script qui permettra à Flyway de construire une base propre.

Utilisez une nouvelle base, par exemple `bd_gsbp_v2` :

```powershell
$env:DB_URL = 'jdbc:mysql://localhost:3306/bd_gsbp_v2?createDatabaseIfNotExist=true&serverTimezone=UTC&useSSL=false'
$env:DB_USERNAME = 'root'
$env:DB_PASSWORD = 'votre_mot_de_passe'
.\gradlew.bat bootRun
```

Au démarrage :

1. MySQL crée `bd_gsbp_v2` si elle n'existe pas.
2. Flyway détecte une base vide.
3. Flyway exécute `V1__baseline_current_schema.sql`.
4. Les 42 tables sont créées, dont `absences`.
5. Hibernate valide le résultat avec `ddl-auto=validate`.
6. L'application peut démarrer.

## Vérifications après démarrage

Dans MySQL :

```sql
USE bd_gsbp_v2;
SHOW TABLES;
DESCRIBE absences;
SELECT version, description, success FROM flyway_schema_history;
```

Vous devez voir `absences`, la migration `V1 - baseline current schema` et `success = 1`.

## Ce qu'il ne faut pas faire

- Ne remettez pas les anciennes migrations si elles contiennent les doublons que vous avez supprimés.
- Ne lancez pas `V1__baseline_current_schema.sql` sur `bd_gsbp` existante.
- Ne laissez pas `spring.jpa.hibernate.ddl-auto=update` comme solution permanente.
- Ne supprimez pas `flyway_schema_history` sur une base contenant des données sans sauvegarde et stratégie de migration.

## Résumé ultra-simple

Le programme bloquait parce qu'une table demandée par une entité Java n'existait pas. J'ai demandé à Hibernate de dessiner toutes les tables attendues dans un fichier SQL. Ce fichier est devenu une nouvelle migration Flyway `V1`. Il faut maintenant lancer cette migration sur une nouvelle base vide, pas sur l'ancienne base partielle. Après cela, Hibernate pourra valider le schéma et Spring Boot démarrera.
# Rapport de correction JPA / schema

## Cause racine

Le demarrage echouait parce que le mapping embarque `Person.Address` attendait `persons.address_complement` alors que cette colonne n'existait pas. Le meme audit a revele la colonne `persons.region`, egalement attendue par le mapping et absente du schema precedent.

## Correction livree

- `spring.jpa.hibernate.ddl-auto` reste a `validate`.
- La migration `V21__reconcile_person_address_and_student_guardians.sql` ajoute conditionnellement `persons.region VARCHAR(100)` et `persons.address_complement VARCHAR(255)`.
- V21 cree `student_guardians`, ses index, sa contrainte d'unicite et ses cles etrangeres vers `students` et `persons`.
- V21 copie les relations existantes de `student_parents` vers `student_guardians` lorsque la `Person` correspondante existe. Elle ne supprime aucune donnee metier.
- Le modele JPA comprend `BaseEntity`, `Person`, `Role`, `Permission`, `UserAccount`, `StaffMember`, `StaffAssignment`, `StaffPosition`, `EmployeeNumber` et `StudentGuardian` avec des mappings explicites. Les derniers adaptateurs `Users` ont ete remplaces par `UserAccountManagementService`, `UserAccountSummaryDTO` et `UserManagementController`.

## Verification executee

| Verification | Resultat |
|---|---|
| `gradlew clean` | Succes |
| `gradlew compileJava` | Succes |
| `gradlew test` | Succes |
| `gradlew bootRun` | Succes : Flyway valide les 22 migrations, schema deja en v21, EntityManagerFactory initialise et Tomcat demarre sur le port 8080 |
| `GET /api/auth/health` | `200 OK` avec `status: OK` |

Le premier demarrage a applique V1 a V21. Le demarrage final du code renomme confirme que le schema est deja en v21, que `ddl-auto=validate` passe, que l'EntityManagerFactory est initialise et que `SchoolPlatformApplication` demarre. Aucun artefact compile des anciennes classes `Users`, `Employee` ou `Position` ne subsiste.

## Donnees et rollback

Les deux nouvelles colonnes sont nullable afin de conserver les lignes existantes. Le rollback applicatif consiste a revenir au code anterieur qui ne lit pas ces colonnes; un rollback SQL eventuel doit etre fait manuellement apres sauvegarde et validation metier. Les migrations Flyway ne sont pas annulees automatiquement.

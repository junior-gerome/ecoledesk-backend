# Audit du backend — 29 juillet 2026

## Périmètre et méthode

Périmètre audité : backend Spring Boot (`src/main/java`), configuration, migrations Flyway, tests et état de l’arbre de travail.

Méthode : revue statique ciblée, recherche des dépendances et exécution Gradle. Cet audit ne modifie pas le code métier.

| Vérification | Résultat |
| --- | --- |
| `./gradlew.bat compileJava` | Réussi. |
| `./gradlew.bat test` | Les 8 rapports JUnit générés totalisent **19 tests**, `0` failure et `0` error. Le processus Gradle ne s’est toutefois pas terminé avant le délai de 120 s : le résultat ne remplace pas une exécution CI complète. |
| Migrations Flyway | Revue statique de `V1` à `V5`; aucun test exécuté sur une base neuve et une copie de base existante. |
| État Git | Arbre très modifié : suppressions massives de migrations/services et ajouts non suivis. Les constats tiennent compte de cet état, sans l’altérer. |

## Résumé exécutif

Le backend compile et le refactoring `Parent` vers `Guardian` est intégré au code Java. Les risques principaux restent la sûreté des migrations en production, le calcul de paiements cumulés, l’intégrité du pointage et la fiabilité des bulletins. Avant toute mise en production, traiter les constats P0 puis valider les migrations et les parcours critiques sur MySQL réel.

| Priorité | Nombre | Sujet |
| --- | ---: | --- |
| P0 | 4 | Migrations, paiements, présence, bulletins |
| P1 | 5 | Autorisations métier, suppression Guardian, secrets, tests, contrats techniques |
| P2 | 2 | Fichiers et dette technique |

## Constats P0 — à traiter avant production

### P0-1 — Historique Flyway instable et réparation automatique

**Preuve :** [application.properties](../src/main/resources/application.properties) active `baseline-on-migrate` (ligne 22) et `repair-on-migrate` (ligne 24). En parallèle, l’arbre Git contient la suppression d’une ancienne chaîne de migrations et l’ajout d’une nouvelle baseline `V1__baseline_current_schema.sql` suivie de migrations `V2` à `V5`.

**Risque :** une application reliée à une base déjà migrée peut masquer une divergence de checksums ou baseliner une base au mauvais état. Une réparation Flyway ne doit jamais être une conséquence automatique d’un démarrage applicatif.

**Action :** désactiver `spring.flyway.repair-on-migrate`; figer l’historique déjà déployé; tester séparément le parcours « base neuve » et une migration sur une copie anonymisée de production avant livraison.

### P0-2 — Le solde de paiement est recalculé par ligne, sans tenir compte des paiements précédents

**Preuve :** dans [PaymentService.java](../src/main/java/com/school/platform/billing/application/PaymentService.java), `applyRequest` calcule `payableAmount` (ligne 284), interdit seulement qu’un **paiement courant** le dépasse (ligne 285), puis stocke `payableAmount - amount` dans cette ligne (ligne 307). Le verrou `findByIdForUpdate` (ligne 333) ne verrouille qu’un paiement existant, pas l’ensemble des paiements de l’élève et du type concerné.

**Risque :** deux versements partiels ou complets peuvent être créés pour la même créance, chacun considérant le montant total comme disponible. Les rapports additionnent alors des restes incohérents.

**Action :** créer une créance/échéance canonique par inscription et type de frais, verrouiller cette créance, agréger les écritures non annulées et empêcher toute somme cumulée supérieure au solde. Ajouter une contrainte et des tests de concurrence, de remise, d’annulation et de second versement.

### P0-3 — Le pointage accepte un élève hors de la classe demandée

**Preuve :** [AttendanceServiceImpl.java](../src/main/java/com/school/platform/attendance/application/AttendanceServiceImpl.java) valide `classId` dans `saveDaily` (ligne 64), mais transmet seulement l’entrée et la date à `persistEntry` (ligne 77). Cette méthode charge l’élève directement par son identifiant (ligne 151) sans vérifier son inscription à cette classe ni à l’année académique demandée.

**Risque :** un utilisateur autorisé sur une classe peut inscrire une présence ou une absence pour un élève d’une autre classe; l’intégrité métier et les statistiques sont alors corrompues.

**Action :** charger une seule fois la cohorte confirmée `(classId, academicYearId)` et refuser les identifiants hors cohorte. Ajouter une contrainte d’unicité `(student_id, date)` et des tests d’autorisation/validation inter-classe.

### P0-4 — Les bulletins et classements ne sont pas filtrés avec un contexte scolaire complet

**Preuve :** [StudentReportService.java](../src/main/java/com/school/platform/reporting/application/StudentReportService.java) charge toutes les notes de l’élève par `findByStudentId` (ligne 43), ne filtre que le champ/label `period`, calcule une moyenne simple non pondérée (ligne 58), compte toutes les absences historiques (ligne 53) et calcule le rang uniquement avec la classe de la dernière note (ligne 84).

**Risque :** un bulletin peut mélanger plusieurs années, classes ou statuts de note, afficher un nombre d’absences hors période et produire un rang erroné.

**Action :** définir une projection de bulletin avec paramètres obligatoires `studentId`, année, classe et période; filtrer les notes validées, appliquer les coefficients et compter les absences dans l’intervalle. Couvrir le calcul avec des jeux de données de référence.

## Constats P1 — à planifier dans le prochain cycle

### P1-1 — Les autorisations restent principalement fondées sur le rôle

**Preuve :** [SecurityConfig.java](../src/main/java/com/school/platform/identityaccess/infrastructure/security/SecurityConfig.java) impose l’authentification globale (ligne 75) et les contrôleurs ajoutent des rôles, mais `AttendanceServiceImpl` ne contrôle pas l’affectation enseignant–classe avant d’écrire. Le problème P0-3 confirme l’absence de politique d’accès métier dans le cas d’usage.

**Risque :** accès horizontal à des élèves/classes non attribués malgré un rôle valide.

**Action :** créer des politiques/cas d’usage d’autorisation basés sur le principal, l’affectation, la classe et l’année; ne plus accepter l’identité de l’opérateur via des paramètres client.

### P1-2 — Suppression d’un Guardian probablement bloquée lorsqu’il est relié à un élève

**Preuve :** [GuardianService.java](../src/main/java/com/school/platform/enrollment/application/GuardianService.java) appelle directement `guardianRepository.delete` (ligne 57). À l’inverse, [Guardian.java](../src/main/java/com/school/platform/enrollment/domain/model/Guardian.java) expose la collection `studentGuardians` sans cascade de suppression ni `orphanRemoval` (ligne 33), et [StudentGuardian.java](../src/main/java/com/school/platform/enrollment/domain/model/StudentGuardian.java) impose une FK non nulle vers `guardian`.

**Risque :** erreur d’intégrité référentielle, ou comportement de suppression non explicite, dès qu’un responsable légal est associé à un élève.

**Action :** choisir la règle métier : refuser la suppression tant que des liens existent, désactiver le profil, ou supprimer explicitement les liens dans une transaction. Ajouter un test d’intégration JPA couvrant le choix retenu.

### P1-3 — Secret JWT et compte MySQL local dangereux par défaut

**Preuve :** [application.properties](../src/main/resources/application.properties) fournit un secret JWT de démonstration (ligne 48) et `root` comme utilisateur MySQL par défaut (ligne 3). `SecurityConfig` vérifie les origines CORS en production, mais aucune vérification équivalente de la valeur effective du secret JWT n’est visible.

**Risque :** démarrage involontaire d’un environnement exposé avec un secret connu ou un compte base de données trop privilégié.

**Action :** faire échouer le démarrage hors profil local si `JWT_SECRET` est absent/faible ou si les identifiants par défaut sont utilisés; utiliser un compte MySQL applicatif à privilèges minimaux.

### P1-4 — Couverture de test insuffisante et exécution Gradle non déterministe

**Preuve :** les rapports XML générés couvrent 19 tests : architecture (4), identité/sécurité (9), billing domaine (2), settings (2) et support (2). Aucun test n’exerce les migrations, le flux Guardian, les paiements cumulés, le pointage par classe ou les bulletins. La commande `test` n’a pas rendu la main en 120 s malgré la génération des rapports.

**Risque :** les parcours les plus risqués ne sont pas protégés; un worker/processus non fermé peut bloquer CI et développement.

**Action :** identifier le processus ou thread qui empêche la fin de Gradle (`--info`, dump de threads); ajouter Testcontainers MySQL pour les migrations et tests d’intégration des parcours P0.

### P1-5 — La migration Guardian doit être validée sur données historiques

**Preuve :** [V4__replace_parent_with_guardian.sql](../src/main/resources/db/migration/V4__replace_parent_with_guardian.sql) renomme `parents` en `guardians`, reconstruit les liens `student_guardians` et supprime ensuite `student_parents`. Elle dépend de noms de contraintes historiques précis (`FK2t9tc6xy5covs6wqevyt842r3`, `uk_student_guardians_student_person`).

**Risque :** échec de migration si une base existante a une contrainte nommée différemment, ou perte de liens si les données historiques ne correspondent pas exactement aux hypothèses du script.

**Action :** exécuter la migration sur un dump restauré; vérifier avant/après les comptes `parents`, `guardians`, `student_parents`, `student_guardians` et les doublons `(student_id, guardian_id)`; documenter une procédure de rollback sauvegardée.

## Constats P2 — amélioration recommandée

### P2-1 — Stockage de fichiers : validation limitée à l’extension et chargement intégral en mémoire

**Preuve :** [FileStorageService.java](../src/main/java/com/school/platform/document/application/FileStorageService.java) autorise les fichiers selon leur extension (lignes 30, 82–95) puis les lit avec `Files.readAllBytes` (ligne 67).

**Risque :** un contenu non conforme peut être téléversé avec une extension acceptée; un téléchargement volumineux consomme la mémoire du processus.

**Action :** détecter le type MIME/contenu, appliquer des limites de lecture et diffuser le fichier en streaming avec un type de réponse approprié.

### P2-2 — Contrats API et vocabulaire encore en transition

**Preuve :** le nouveau modèle `Guardian` est présent, mais les composants de notification et de stockage contiennent encore des termes/tableaux `parent` (`PushNotificationService`, répertoires `photos/parents`). Le service push utilise en outre des `System.out.println` et des tables SQL legacy.

**Risque :** dette de compatibilité, documentation trompeuse et erreurs lorsque ces chemins ou requêtes seront réellement utilisés.

**Action :** établir un plan de compatibilité API/documenté, renommer les contrats persistants après migration, et soit terminer le service push avec une vraie stratégie de livraison, soit le retirer du produit.

## Plan de remédiation

1. Geler et tester la chaîne Flyway sur base neuve et base existante; désactiver la réparation automatique.
2. Corriger la comptabilité par créance et ajout de tests de paiements cumulés/concurrents.
3. Bloquer les pointages hors cohorte et reconstruire le calcul de bulletin autour d’un contexte académique explicite.
4. Ajouter les autorisations métier et la règle explicite de suppression/désactivation d’un Guardian.
5. Stabiliser l’exécution des tests, puis imposer compilation, tests d’intégration et validation de migration en CI.

## Critères de sortie

- `compileJava` et `test` se terminent avec un code retour nul dans la CI.
- Les migrations réussissent sur une base neuve et une copie de données existantes, avec contrôles de volumétrie avant/après.
- Les cas P0 disposent de tests automatisés et d’un contrôle transactionnel explicite.
- Aucune configuration non locale ne peut démarrer avec le secret JWT ou le compte MySQL par défaut.

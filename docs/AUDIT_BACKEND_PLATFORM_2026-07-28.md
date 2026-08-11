# Audit technique et métier du backend `platform`

**Date :** 28 juillet 2026  
**Périmètre :** `src/main/java/com/school/platform`, configuration Gradle, migrations Flyway et tests.  
**Nature :** revue statique du code présent dans l'arbre de travail. Aucun changement fonctionnel n'a été appliqué par cet audit.

## Résumé exécutif

Le projet est un monolithe Spring Boot organisé par domaines, mais les frontières des modules ne sont pas réellement protégées : plusieurs modules importent directement les entités et repositories d'autres modules. Une refonte importante est en cours dans l'arbre Git (anciens services et migrations supprimés, nouveaux modèles ajoutés) ; elle n'est pas stabilisée et doit être isolée avant une mise en production.

Les risques les plus importants sont les suivants :

| Priorité | Constat | Impact |
| --- | --- | --- |
| P0 | `PaymentService` calcule `montantRestant` par ligne de paiement, sans agréger les versements précédents. | Double encaissement possible et solde client erroné. |
| P0 | Les calculs de bulletins/rapports ne filtrent pas systématiquement l'année, la classe courante ni le statut des notes. | Bulletins et classements potentiellement faux. |
| P0 | Les migrations Flyway historiques ont été remplacées par une baseline `V1` alors que `repair-on-migrate=true`. | Risque de casser l'historique de production ou de valider un schéma incohérent. |
| P1 | Les enseignants authentifiés peuvent appeler des endpoints de présence, notes et rapports sans contrôle d'affectation à la classe. | Accès horizontal à des données d'autres classes. |
| P1 | Des modules ou composants sont incomplets/non appelés : staff, document métier, notifications push, sauvegarde, audit applicatif. | Dette de maintenance et comportements trompeurs. |
| P1 | Les tableaux de bord utilisent du SQL direct et du calcul en contrôleur, indépendamment des services métier. | Règles dupliquées, requêtes fragiles et résultats divergents. |

## Méthode et limites

- Lecture des contrôleurs, services, entités, repositories, configuration et scripts de migration.
- Recherche des composants sans consommateurs, des doublons de socle et des marqueurs de code inachevé.
- Lecture de l'état Git : il contenait déjà de nombreuses suppressions/modifications non commitées. Elles sont signalées mais n'ont pas été altérées.
- La commande `./gradlew.bat test --no-daemon` a dépassé 64 secondes sans résultat exploitable ; le build complet reste à exécuter dans la CI ou localement avec un délai suffisant.
- Les tests présents sont peu nombreux (8 fichiers) et couvrent surtout identité, settings, support et le plan de paiement ; ils ne couvrent pas les parcours P0 ci-dessous.

## Constats transverses

### Architecture et dépendances

- Le test `ModularMonolithArchitectureTest` ne vérifie que la disparition de `com.school.gestionuser`, le scan Spring et deux beans identité. Il n'impose aucune règle de dépendance entre modules.
- `search`, `reporting` et `attendance` dépendent directement d'entités et repositories de plusieurs domaines. Exemples : `SearchService`, `ReportController`, `StudentReportService` et `AttendanceServiceImpl`.
- Les DTO exposent parfois des entités JPA ou des structures `Map<String,Object>` (`SearchService`, `PaymentService`, `ReportController`). Cela rend le contrat API instable et couple le web au schéma.
- Les règles métier sont réparties entre contrôleurs, services, entités, SQL Jdbc et PDF. Une règle (statut d'un paiement, moyenne, présence) peut donc avoir plusieurs implémentations différentes.

### Code à nettoyer / code mort confirmé

Les éléments suivants ne sont référencés que par leur propre déclaration dans le code Java analysé, ou sont hors du `sourceSet` Java principal :

- Résolu : le doublon hors sourceSet `src/main/SchoolManagementApplication.java` a été supprimé ; l'entrée Spring officielle est `com.school.platform.SchoolPlatformApplication`.
- `document/Document.java` et `DocumentRepository` : le contrôleur de fichiers écrit sur disque, jamais dans cette entité/repository.
- `staff/StaffMember`, `StaffAssignment` et leurs repositories : aucun service ni contrôleur ; module incomplet à supprimer ou à terminer.
- `notification/infrastructure/push/PushNotificationService` : aucun consommateur; simulation via `System.out.println`, SQL legacy et texte encodé incorrectement.
- `reporting/LogActiviteService`, `PredictiveAnalyticsService`, `shared/infrastructure/backup/BackupService` : aucun consommateur détecté.
- Socle dupliqué : `shared/domain/legacy/BaseEntity`, `shared/domain/valueobject/Address`, plusieurs hiérarchies d'exceptions (`exception`, `exception/shared`, `exception/compat`) et des réponses web `compat` dupliquées.
- Dépendances probablement inutilisées ou à confirmer : Firebase Admin, iText 5 en plus d'iText 7, Testcontainers sans test d'intégration détecté.

### Qualité commune

- Plusieurs fichiers contiennent du texte mal encodé (`Ã©`, `Ã¨`) : notamment `NotificationService`, `PushNotificationService`, `Grade`, `PersonServiceImpl`, `AnalyticsService` et des migrations. Uniformiser en UTF-8 et ajouter un contrôle d'encodage en CI.
- Des services attrapent `Exception` et loggent seulement le message (`NotificationService`, `ReportService`). Les erreurs métier sont alors masquées ou reconditionnées sans cause exploitable.
- Le code mélange français et anglais : classes, statuts, champs et messages. Définir un langage de code unique; conserver les traductions dans l'API/UI.
- Une configuration de développement dangereuse est par défaut : secret JWT de démonstration et URL MySQL root. Faire échouer le démarrage hors profil local si les secrets ne sont pas fournis.

## Audit par module

### `academic` — P1

**À refactoriser**

- `GradeCommandService` et `GradeCalculationService` ne sont que des façades transactionnelles de `GradeService`. Les fusionner ou remplacer `GradeService` par de vrais cas d'usage command/query.
- `AcademicYearServiceImp` ne valide ni `dateDebut < dateFin`, ni l'unicité du libellé, ni l'inactivation de l'année active. `activateAcademicYear` doit être protégé par une contrainte DB garantissant une seule année active et par un verrou cohérent.
- Le modèle `Grade` porte à la fois `sequence`, `trimestre` et la chaîne libre `period`; choisir une source de vérité temporelle. Les notes doivent avoir un identifiant métier/une contrainte d'unicité adaptée (élève, matière, évaluation, période).
- Les invariants de note (appartenance de l'élève à la classe, matière réellement enseignée, période de l'année active, statut `DRAFT → VALIDATED → LOCKED`) doivent être dans un agrégat/service métier et vérifiés avant persistance.

**Nettoyage**

- Les commentaires de l'ancien identifiant dans `Grade` et les noms `AcademicYearServiceImp`, `statutCode` (booléen) sont des traces de transition.
- Les entités `Teacher` académiques et `StaffMember` coexistent sans frontière claire. Décider si un enseignant est un staff avec une affectation ou une entité autonome.

### `attendance` — P1

- `AttendanceServiceImpl.saveDaily` reçoit une `classId` mais ne vérifie pas que chaque élève envoyé appartient à cette classe et à l'année indiquée. C'est une faille d'intégrité fonctionnelle.
- L'autorisation des endpoints se limite au rôle; un enseignant peut consulter/modifier toute classe. Introduire une politique d'accès fondée sur l'affectation enseignant–classe–matière.
- `Absence.status` est une chaîne libre alors qu'elle représente un ensemble fermé (`PRESENT`, `ABSENT`, `LATE`, `EXCUSED`). Employer un enum et une contrainte d'unicité `(student_id, date)` en base.
- `getRecords`, `getSummary` et `saveDaily` effectuent des recherches d'inscription par élève (N+1). Charger la cohorte et ses inscriptions en une requête/une projection.
- Le repli silencieux sur une cohorte sans année dans `findRoster` peut afficher ou modifier des données d'une autre année. Le supprimer ou le rendre explicite.

### `billing` — P0

- Dans `PaymentService.applyRequest`, `montantRestant = montant attendu - montant de la ligne courante`. Les paiements précédents, annulations et paiements partiels ne sont jamais agrégés. Un élève peut ainsi payer plusieurs fois le montant complet d'un même poste.
- `Paiement` est un ancien modèle de paiement et `TuitionPaymentPlan` est un nouveau modèle parallèle. Les échéances ne sont jamais alimentées par les paiements, ne possèdent pas de commande d'encaissement et ne passent pas à `PAID/PARTIALLY_PAID/OVERDUE`.
- La remise et le montant attendu sont stockés/réévalués par paiement, au lieu d'être portés par une créance/plan. Une modification d'un paiement historique peut changer le solde sans journalisation.
- La génération de numéro de reçu est contrôlée applicativement mais aucune contrainte unique sur `paiements.receipt_number` n'apparaît dans la baseline. Conserver le contrôle applicatif et ajouter l'unicité DB.
- `PreEnrollmentFeePaymentService` vérifie un paiement sans audit de l'opérateur ni date de vérification; les références de transaction doivent aussi être contraintes en base.

**Cible recommandée** : introduire une créance (`FeeInvoice`/`StudentAccount`) par inscription et poste tarifaire, des écritures de paiement immuables, puis calculer le solde par agrégation transactionnelle. Faire migrer `Paiement` et `PaymentInstallment` vers ce modèle avant d'ajouter des fonctions financières.

### `document` — P1

- `FileStorageService` sécurise bien les chemins relatifs et les répertoires autorisés, mais valide uniquement l'extension, pas le MIME réel ni le contenu.
- `FileUploadController` ne lie pas le fichier à un élève/dossier ni au propriétaire qui le téléverse; `Document`/`DocumentRepository` restent inutilisés. Choisir un seul flux : métadonnées persistées + stockage objet/disque, ou supprimer le modèle inutilisé.
- Les rôles peuvent télécharger/supprimer des fichiers sans contrôle d'appartenance. Ajouter l'autorisation métier et une traçabilité de téléchargement/suppression.
- Le contrôleur lit tout le fichier en mémoire et renvoie systématiquement `application/octet-stream`; utiliser le streaming, le type détecté et une limite de téléchargement.

### `enrollment` — P1

- La préinscription est une bonne ébauche d'automate, mais les identifiants `reviewedBy` viennent des paramètres de requête/service, pas du principal authentifié. Les dériver du contexte de sécurité et auditer la décision.
- `createFromApprovedPreEnrollment` crée un nouvel élève et des personnes sans stratégie de dédoublonnage robuste. La recherche d'un tuteur seulement par e-mail peut fusionner à tort ou dupliquer une personne sans e-mail.
- Les validations de `CreatePreEnrollmentRequest` et de tuteurs doivent être explicites au bord API; plusieurs erreurs remontent comme `IllegalStateException`.
- Prévoir les contraintes DB : une inscription active par élève/année, identifiants métier uniques, et règles de capacité de classe avec verrou/transaction.
- L'ancien parcours `InscriptionStudent*` est supprimé tandis que le parcours `PreEnrollment/Enrollment` est ajouté. Supprimer toutes les références côté clients/documentation uniquement après migration des données et tests de non-régression.

### `identityaccess` — P1

- `AuthentificationService.profile` choisit le « premier » rôle d'un compte. Pour un compte multi-rôles, le rôle retourné est non déterministe et ne représente pas l'ensemble de ses droits.
- `PersonServiceImpl.update` ne vérifie pas qu'un nouvel e-mail est disponible pour une autre personne. Il utilise aussi les exceptions `compat`, ce qui maintient une seconde hiérarchie d'erreurs.
- Les VOs `Email`, `PhoneNumber`, `BirthDate`, `Address` sont contournés par des accesseurs scalar de compatibilité; terminer la migration des DTO/mappers puis réduire cette couche.
- Les règles de sécurité globales authentifient les requêtes mais la plupart des contrôles fins (propriétaire/tuteur/enseignant affecté) n'existent pas.
- Le secret JWT par défaut est acceptable uniquement en local; interdire cette valeur par défaut en environnement non local.

### `notification` — P1

- Deux services portent le nom `NotificationService` dans des packages différents, avec deux modèles de livraison (WebSocket et push SQL direct) sans contrat commun.
- Le push n'envoie aucune notification : il imprime les tokens à la console. Ses requêtes utilisent des tables/colonnes non vérifiées dans la baseline (`parent_devices`, `enseignant_devices`, `student.parent_id`, `student.classe_id`).
- `NotificationService` avale les exceptions de publication/persistance; le demandeur ne peut pas savoir si une notification a échoué. Mettre en place une outbox et un statut de livraison.
- Harmoniser encodage, vocabulaire et destinataires : l'identité should être `UserAccount/Person`, non un mélange d'identifiants élève/classe.

### `reporting` — P0

- `StudentReportService` calcule une moyenne simple sur toutes les notes de l'élève, sans filtrage systématique par année, classe, statut de note ou coefficient. `countByStudentId` compte les enregistrements de présence, y compris `PRESENT`, comme jours d'absence.
- `PdfGenerationService.addGradesTable` répète la même note dans les colonnes « Séquence 1 », « Séquence 2 » et « Trimestre ». Le bulletin est factuellement faux quand plusieurs évaluations existent.
- `ReportController` contient une logique de rapport complète : appels repository, N+1 par inscription, classement recalculé dans une boucle O(n²), taux de présence arbitraire `100 - absences * 5`, et aucune filtration cohérente par année/période/statut.
- Les rapports financiers incluent les paiements annulés et additionnent les restes par ligne, ce qui propage le défaut P0 de billing.
- `AnalyticsService` duplique les calculs via SQL brut, repose sur les noms exacts de tables et est mis en cache sans invalidation. Les indicateurs peuvent être durablement obsolètes après un paiement, une note ou une présence.
- La génération ZIP de bulletins maintient tous les PDF en mémoire; limiter la taille, streamer le flux et autoriser la classe demandée.

### `search` — P1

- `SearchService` effectue une recherche en mémoire après avoir chargé des pages arbitraires (`50` ou `500`) et retourne des `Map<String,Object>`. Les résultats deviennent incomplets dès que les données dépassent la première page.
- Il dépend directement de cinq domaines et de leurs repositories; en faire une projection de lecture dédiée (SQL/QueryDSL) ou des ports de requête par domaine.
- Les filtres de section implémentent une heuristique implicite (« francophone » signifie aussi tout ce qui n'est pas anglophone) ; cette règle doit devenir une valeur métier explicite.
- Les erreurs de parsing JSON/date/nombre sont silencieusement converties en `null`; retourner une validation 400 pour les critères invalides.

### `settings` — P2

- Le singleton est simple et correctement protégé par rôle. Ajouter une version optimiste ou un ETag : deux administrateurs peuvent écraser leurs changements.
- Les préférences sécurité (longueur de mot de passe, 2FA) sont stockées mais l'audit doit vérifier qu'elles sont réellement appliquées par `identityaccess`; sinon elles constituent une configuration trompeuse.

### `staff` — P1

- Le module ne comporte que des entités et repositories sans cas d'usage ni API : c'est du code mort/incomplet tant qu'il n'est pas intégré.
- `StaffAssignment` ne protège pas les recouvrements de périodes et n'est pas relié aux affectations académiques. Définir le modèle canonique d'un enseignant avant d'ajouter ces interfaces.

### `support` — P2

- Le centre d'aide est entièrement codé en dur dans `SupportService`; le déplacer vers un contenu versionné ou administrable si le besoin est réel.
- Les tickets ne portent pas l'auteur authentifié et aucun workflow de traitement/consultation n'est exposé. Soit compléter ce domaine, soit traiter le formulaire comme une simple boîte de contact externe.

### `shared` / infrastructure — P1

- La couche `shared` est devenue un dépôt de compatibilité : exceptions et réponses HTTP dupliquées, base entity legacy et VO d'adresse dupliqué. Définir une seule API d'erreur (par exemple RFC 9457 `ProblemDetail`) et une seule hiérarchie d'exceptions.
- `FlywayRepairTool` et `spring.flyway.repair-on-migrate=true` rendent la réparation de l'historique automatique. La réparation doit être une opération manuelle, journalisée et explicitement approuvée.
- `V1__baseline_current_schema.sql` annonce lui-même une application « uniquement sur une base neuve ». Ne pas l'utiliser pour remplacer l'historique d'une base existante. Restaurer/figer l'historique réel, ou préparer une migration de transition validée sur une copie de production.
- Le package `platform/infrastructure` est vide; supprimer ce niveau ou y placer seulement des adaptateurs réellement partagés.

## Plan de remédiation priorisé

1. **Geler la refonte en cours et sécuriser le schéma.** Faire un commit/une branche dédiée, restaurer une chaîne Flyway déterministe, désactiver `repair-on-migrate`, tester une base neuve et une copie de base existante.
2. **Corriger la comptabilité.** Définir le modèle unique de créance/échéance/paiement, migrer les données, interdire les surpaiements par agrégation verrouillée, rendre les écritures et annulations auditables.
3. **Fiabiliser les règles scolaires.** Centraliser année active, cohorte, affectation et statut des notes; réécrire bulletin/rang/moyennes/assiduité à partir de projections filtrées et testées.
4. **Ajouter l'autorisation métier.** Transformer les contrôles rôle-only en politiques d'accès à la classe, l'élève, le tuteur et le fichier.
5. **Réduire la dette.** Retirer ou terminer les modules morts, supprimer la compatibilité obsolète après migration, uniformiser exceptions/DTO/encodage.
6. **Rendre l'architecture vérifiable.** Ajouter ArchUnit ou des tests d'architecture interdisant l'accès aux repositories d'un autre module; exposer des ports/query services plutôt que les entités.
7. **Construire le filet de tests.** Tests d'intégration MySQL/Testcontainers pour migrations, paiements cumulés/annulés, transitions de préinscription, autorisations par affectation, calcul de bulletin et concurrence.

## Critères de sortie avant production

- Build, analyse statique et suite de tests exécutés en CI avec succès.
- Migration Flyway répétable sur base neuve et scénario de mise à niveau validé.
- Tests couvrant au minimum tous les parcours P0 et les règles d'accès horizontal.
- Un seul modèle financier et un seul calcul de solde exploités par l'API et les rapports.
- Les composants non utilisés sont supprimés ou explicitement placés derrière une fonctionnalité livrable.
- Les contrats API utilisent des DTO typés et les rapports sont vérifiés sur des jeux de données métiers de référence.

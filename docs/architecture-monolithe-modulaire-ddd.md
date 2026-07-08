# Architecture cible: monolithe modulaire DDD

## Decision

Le backend reste un monolithe modulaire. Le decoupage microservices est reporte tant que les frontieres metier, la securite, les migrations et les invariants ne sont pas stabilises.

## Contextes bornes

- `identityaccess`: utilisateurs, profils, roles, permissions, authentification, reset password.
- `enrollment`: preinscriptions, inscriptions, eleves, parents, capacites de classe.
- `academic`: classes, matieres, sequences, trimestres, affectations, notes.
- `billing`: montants, paiements, regles financieres.
- `attendance`: presences et absences.
- `reporting`: tableaux de bord, bulletins, exports, audit metier.
- `notification`: notifications applicatives.
- `shared`: exceptions, reponses API, audit technique transverse, helpers sans logique metier propre.

Chaque contexte doit tendre vers la structure:

- `domain`: entites, value objects, invariants, politiques metier.
- `application`: cas d'usage, transactions, DTO applicatifs.
- `infrastructure`: persistance, integration technique, adapters sortants.
- `web` ou `adapter/in/rest`: controleurs HTTP et DTO d'entree/sortie REST.

## Regles immediates

- Le modele identite canonique est `Users` avec `UsersProfil`.
- Le modele legacy `UserAccount` est conserve pour compatibilite, mais son API `/user-accounts` est desactivee par defaut via `school.identity.legacy-user-account.enabled=false`.
- Le package `com.school.gestionuser` reste hors build principal. Il ne doit pas etre importe par `com.school.platform`.
- Les controles HTTP restent dans les controleurs; les transactions et cas d'usage restent dans les services applicatifs; les repositories Spring Data restent cote infrastructure.
- Les migrations Flyway sont la source de verite pour la base. Toute correction d'invariant doit avoir une migration idempotente quand elle touche une base deja deployee.

## Workflow actuellement stabilise

- Preinscription: `EN_ATTENTE -> VALIDEE | REFUSEE | ANNULEE`.
- Notes: `DRAFT -> VALIDATED -> LOCKED`, avec deverrouillage admin justifie.
- Les preinscriptions `REFUSEE` et `ANNULEE` ne comptent plus comme inscriptions actives pour les doublons et la capacite.

## Garde-fous

Le test `ModularMonolithArchitectureTest` verifie:

- le scan Spring limite a `com.school.platform`;
- l'exclusion build du package legacy `com.school.gestionuser`;
- l'absence de dependance de `com.school.platform` vers `com.school.gestionuser`;
- le feature flag obligatoire sur l'API legacy `UserAccount`.

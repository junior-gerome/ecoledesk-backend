# Architecture cible - monolithe modulaire DDD

Le backend est reconstruit comme un monolithe modulaire centre sur le package `com.school.platform`.
Le package `com.school.gestionuser` est conserve comme reference legacy hors build applicatif et ne doit pas recevoir de nouvelle fonctionnalite.

## Bounded contexts actifs

- `identityaccess` : utilisateurs, profils, roles, permissions, authentification, tokens et audit d'authentification.
- `academic` : annees scolaires, classes, matieres, sequences, trimestres, notes et affectations.
- `enrollment` : eleves, parents, preinscriptions et inscriptions.
- `billing` : montants, paiements et regles de validation financiere.
- `attendance` : absences et presence.
- `document` : fichiers et pieces justificatives.
- `notification` : notifications applicatives et push.
- `reporting` : tableaux de bord, rapports et audit metier transverse.
- `shared` : primitives partagees, exceptions, reponses API, configuration technique commune.

## Regles de couches

Chaque contexte suit la structure suivante :

```text
context
  domain          # entites, value objects, politiques, invariants
  application     # cas d'usage, transactions, DTO applicatifs, ports
  infrastructure  # persistence, integrations externes, configuration technique locale
  web             # controleurs REST, DTO HTTP, securite HTTP locale si necessaire
```

Regles :

- `web` appelle `application`, jamais directement `infrastructure`.
- `application` orchestre les cas d'usage et porte les transactions.
- `domain` ne depend pas de `web`, de Spring MVC, ni de DTO HTTP.
- `infrastructure` implemente les ports et repositories techniques.
- Les exceptions exposees en REST passent par `shared.web.GlobalExceptionHandler` ou par les handlers securite dedies.
- Les migrations Flyway restent le contrat de schema ; `spring.jpa.hibernate.ddl-auto` reste a `validate`.

## Identite unifiee

Le modele operationnel d'authentification est `identityaccess.domain.model.Users` avec `UsersProfil`, `Role` et `Permission`.
`UserAccount` est conserve uniquement pour compatibilite/migration tant que les endpoints admin correspondants existent ; aucune nouvelle logique d'authentification ne doit l'utiliser.

## Securite et observabilite

- JWT access token stateless.
- Refresh tokens persistes uniquement sous forme de hash SHA-256 et rotates a chaque usage.
- Reutilisation d'un refresh token deja remplace : revocation des sessions actives de l'utilisateur et audit `REFRESH_REUSE`.
- Password reset token stocke uniquement sous forme de hash.
- Logs de securite sans query string, sans token et sans lien de reset en production.
- Actuator health details desactives par defaut et en production.

## Base de donnees

- Toute evolution passe par une migration `Vxx__description.sql`.
- Les index suivent les cas d'usage de lecture : utilisateur, token actif, dates d'audit, FK de recherche.
- Les contraintes metier critiques doivent etre portees a la fois par le domaine et par la base quand c'est possible.

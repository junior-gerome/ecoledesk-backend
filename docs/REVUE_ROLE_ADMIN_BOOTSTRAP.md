# Comprendre et corriger « Le rôle ADMIN doit être initialisé avant le bootstrap »

## 1. Le problème, avec une image simple

Pense à l'application comme à une école :

- le **rôle** est un badge d'accès, par exemple `ADMIN` ;
- le **compte administrateur** est la première personne à qui l'on donne ce badge ;
- `DevAdminBootstrap` est l'assistant qui crée cette première personne au démarrage.

L'assistant sait créer une personne, mais il ne fabrique pas les badges. Au démarrage, il cherche donc le badge `ADMIN`. Comme ce badge n'existe pas encore dans la base, il s'arrête volontairement avec :

```text
Le role ADMIN doit etre initialise avant le bootstrap
```

Ce n'est pas un problème de JWT, de Tomcat ou de repository. La base démarre correctement, mais il manque une donnée de sécurité obligatoire.

## 2. Ce que fait exactement `DevAdminBootstrap`

Dans `DevAdminBootstrap.java`, ce code s'exécute seulement si :

```properties
app.bootstrap.admin.enabled=true
```

Il réalise les actions suivantes :

1. Vérifie que l'email et le mot de passe administrateur sont renseignés.
2. Vérifie qu'un compte avec cet email n'existe pas déjà.
3. Cherche le rôle dont le code est `ADMIN`.
4. Crée le compte administrateur.
5. Lui attache le rôle `ADMIN`.

L'étape 3 bloque actuellement :

```java
Role admin = roleRepository.findByCodeIgnoreCase("ADMIN")
    .orElseThrow(() -> new IllegalStateException(
        "Le role ADMIN doit etre initialise avant le bootstrap"));
```

Le mot `IgnoreCase` signifie que `admin`, `Admin` et `ADMIN` peuvent être trouvés. Malgré cela, il faut bien qu'une ligne existe dans la table `roles`.

## 3. Revue simple de l'entité `Role`

`Role.java` représente un badge de sécurité stocké dans la table `roles`.

| Champ | Rôle | Exemple pour l'administrateur |
|---|---|---|
| `code` | Nom technique utilisé par Spring Security. Il doit être unique. | `ADMIN` |
| `label` | Nom affiché à un utilisateur. | `Administrateur` |
| `description` | Explication humaine du rôle. | `Administration complète` |
| `active` | Indique si le rôle est utilisable. | `true` |
| `permissions` | Droits détaillés éventuellement associés. | lecture, écriture, etc. |

Le code correct dans la base est **`ADMIN`**, et non `ROLE_ADMIN`.

Pourquoi ? `UserAccount` ajoute lui-même le préfixe `ROLE_` lorsqu'il transforme le rôle en autorité Spring Security. Ainsi :

```text
base de données : ADMIN
Spring Security : ROLE_ADMIN
contrôleur      : hasRole('ADMIN')
```

Ces trois éléments sont donc cohérents dans le code actuel.

## 4. Pourquoi le problème apparaît après la nouvelle baseline

La migration [V1__baseline_current_schema.sql](../src/main/resources/db/migration/V1__baseline_current_schema.sql) crée la structure : tables `roles`, `user_accounts`, `user_roles`, etc.

Mais créer une table est comme construire un classeur vide : il n'y a encore aucun badge dans le classeur. La migration V1 contient des `CREATE TABLE`, pas des `INSERT INTO roles`.

Conséquence : une base neuve possède bien la table `roles`, mais elle ne possède pas encore le rôle `ADMIN`.

## 5. Ce qu'il faut faire, étape par étape

### Étape 1 — Ne pas modifier `DevAdminBootstrap`

Conserve sa responsabilité actuelle : créer le premier **compte** administrateur, pas les données de référence.

Impact : le code reste simple et le démarrage échoue clairement si les données de sécurité sont incohérentes.

### Étape 2 — Ajouter une migration de données `V2`

Crée le fichier :

```text
src/main/resources/db/migration/V2__seed_initial_security_roles.sql
```

Il doit créer les rôles nécessaires avant le bootstrap :

```sql
INSERT INTO roles (active, created_at, updated_at, code, label, description)
SELECT TRUE, CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6),
       'ADMIN', 'Administrateur', 'Administration complète de la plateforme'
WHERE NOT EXISTS (
    SELECT 1 FROM roles WHERE UPPER(code) = 'ADMIN'
);

INSERT INTO roles (active, created_at, updated_at, code, label, description)
SELECT TRUE, CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6),
       'AGENT', 'Agent', 'Gestion administrative'
WHERE NOT EXISTS (
    SELECT 1 FROM roles WHERE UPPER(code) = 'AGENT'
);

INSERT INTO roles (active, created_at, updated_at, code, label, description)
SELECT TRUE, CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6),
       'ENSEIGNANT', 'Enseignant', 'Accès enseignant'
WHERE NOT EXISTS (
    SELECT 1 FROM roles WHERE UPPER(code) = 'ENSEIGNANT'
);
```

Pourquoi une migration Flyway ? Parce que Flyway s'exécute avant le bootstrap applicatif. Chaque nouvelle base reçoit les mêmes rôles, une seule fois, avec un historique vérifiable.

Impact : `ADMIN` existe toujours avant que `DevAdminBootstrap` le cherche.

### Étape 3 — Créer une base neuve

La baseline V1 et la migration V2 sont prévues pour une base vide, par exemple `bd_gsbp_v2` :

```powershell
$env:DB_URL = 'jdbc:mysql://localhost:3306/bd_gsbp_v2?createDatabaseIfNotExist=true&serverTimezone=UTC&useSSL=false'
$env:DB_USERNAME = 'root'
$env:DB_PASSWORD = 'votre_mot_de_passe'
```

Impact : Flyway applique dans l'ordre :

```text
V1 : crée les tables
V2 : ajoute ADMIN, AGENT et ENSEIGNANT
```

Ne lance pas V1 sur l'ancienne base partielle : les tables déjà présentes créeraient des doublons.

### Étape 4 — Configurer le premier administrateur

Pour une installation locale contrôlée :

```powershell
$env:APP_BOOTSTRAP_ADMIN_ENABLED = 'true'
$env:APP_BOOTSTRAP_ADMIN_EMAIL = 'admin@ecole.local'
$env:APP_BOOTSTRAP_ADMIN_PASSWORD = 'un-mot-de-passe-fort'
.\gradlew.bat bootRun
```

Impact : après V1 et V2, le bootstrap trouve `ADMIN`, crée le compte et l'associe à ce rôle.

### Étape 5 — Désactiver ensuite le bootstrap

Après la création du compte :

```powershell
$env:APP_BOOTSTRAP_ADMIN_ENABLED = 'false'
```

Impact : le mot de passe initial n'est plus utilisé au démarrage et aucun compte surprise ne peut être créé.

## 6. Vérifier que tout a fonctionné

Dans MySQL :

```sql
USE bd_gsbp_v2;
SELECT code, label, active FROM roles;
SELECT version, description, success FROM flyway_schema_history;
```

Résultat attendu :

- `ADMIN`, `AGENT` et `ENSEIGNANT` sont présents et actifs ;
- Flyway indique `V1` et `V2` avec `success = 1` ;
- l'application démarre sans l'exception ;
- le compte configuré existe dans `user_accounts` et est relié à `ADMIN` via `user_roles`.

## 7. Résumé à retenir

La table `roles` est le classeur des badges de sécurité. `DevAdminBootstrap` peut créer l'administrateur, mais il ne peut pas lui donner un badge qui n'existe pas. La migration V1 construit le classeur ; une migration V2 doit y placer les badges initiaux. Ensuite seulement, le bootstrap peut créer le premier administrateur de manière fiable.
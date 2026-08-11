# Commandes PowerShell — Spring Boot et développement

Exécuter ces commandes depuis la racine du backend :

```powershell
cd C:\Projects\Primary-School\backend
```

## Démarrer en développement avec le premier administrateur

À utiliser uniquement sur une base de développement neuve contenant les migrations `V1` et `V2` ; `V2` doit avoir créé le rôle `ADMIN`.

```powershell
# Active le profil de développement Spring.
$env:SPRING_PROFILES_ACTIVE = 'dev'

# Autorise le bootstrap à créer le premier compte administrateur.
$env:APP_BOOTSTRAP_ADMIN_ENABLED = 'true'

# Définit l'identifiant du premier administrateur.
$env:APP_BOOTSTRAP_ADMIN_EMAIL = 'admin@school.local'

# Définit son mot de passe initial. Ne jamais le déposer dans Git.
$env:APP_BOOTSTRAP_ADMIN_PASSWORD = 'Admin@123456'

# Lance Spring Boot avec le Wrapper Gradle et affiche le rapport français si un diagnostic est trouvé.
.\compiler-fr.ps1 -Action run
```

Après la création du compte, désactivez le bootstrap dans ce terminal :

```powershell
# Empêche toute nouvelle tentative de création automatique du compte administrateur.
$env:APP_BOOTSTRAP_ADMIN_ENABLED = 'false'
```

## Choisir la base MySQL de développement

```powershell
# Utilise une base neuve ; MySQL la crée si elle n'existe pas.
$env:DB_URL = 'jdbc:mysql://localhost:3306/bd_gsbp_v2?createDatabaseIfNotExist=true&serverTimezone=UTC&useSSL=false'

# Identifiants MySQL locaux : adaptez-les à votre installation.
$env:DB_USERNAME = 'root'
$env:DB_PASSWORD = 'votre_mot_de_passe_mysql'
```

## Compiler, tester et construire

```powershell
# Compile uniquement le code Java de production.
.\compiler-fr.ps1 -Action compile

# Exécute les tests du projet.
.\compiler-fr.ps1 -Action test

# Compile, teste et construit l'artefact Spring Boot.
.\compiler-fr.ps1 -Action build

# Supprime le dossier build généré par Gradle.
.\compiler-fr.ps1 -Action clean
```

## Commandes Gradle directes

```powershell
# Lance Spring Boot sans le rapport français.
.\gradlew.bat bootRun

# Liste les tâches disponibles dans le projet.
.\gradlew.bat tasks

# Exécute les tests avec une sortie Gradle simple.
.\gradlew.bat test --console=plain
```

## Générer le SQL depuis les entités JPA

```powershell
# Active le profil qui écrit le DDL dans build/schema-current.sql sans modifier MySQL.
.\gradlew.bat bootRun --args="--spring.profiles.active=schema-export --spring.main.web-application-type=none"
```

Le fichier produit sert à réviser la structure avant de créer une nouvelle migration Flyway.

## Rappel de sécurité

Les variables `$env:` ne vivent que dans le terminal PowerShell courant. Fermez le terminal ou retirez-les après usage. Ne mettez jamais les mots de passe de production dans `application.properties`, dans un fichier suivi par Git ou dans une capture d'écran.
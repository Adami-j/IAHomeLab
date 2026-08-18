# Initial User Setup

Ce document décrit comment IAHomeLab crée le premier compte administrateur.

L'application peut être lancée directement depuis Java / IntelliJ ou via Docker.

## Principe

IAHomeLab ne contient aucun mot de passe administrateur en dur.

Au démarrage :

```text
application démarre
        ↓
un ADMIN existe déjà ?
   ├── oui → ne rien faire
   └── non
        ↓
lire les variables d'environnement
        ↓
créer AppUser ADMIN
        ↓
créer UserIdentity LOCAL
        ↓
encoder le mot de passe
```

Si aucun administrateur n'existe et que les variables obligatoires sont absentes, l'application refuse de démarrer.

## Variables d'environnement

Variables utilisées :

```text
IAHL_INITIAL_USERNAME
IAHL_INITIAL_PASSWORD
IAHL_INITIAL_EMAIL
```

Obligatoires :

```text
IAHL_INITIAL_USERNAME
IAHL_INITIAL_PASSWORD
```

Optionnelle :

```text
IAHL_INITIAL_EMAIL
```

## Configuration Spring

Le bootstrap est contrôlé par :

```properties
iahl.initial-admin.enabled=true
```

En test :

```properties
iahl.initial-admin.enabled=false
```

Ainsi, les tests créent eux-mêmes leurs utilisateurs.

## Lancement direct

### IntelliJ

Dans :

```text
Run
→ Edit Configurations
→ Environment variables
```

Ajouter par exemple :

```text
IAHL_INITIAL_USERNAME=admin
IAHL_INITIAL_PASSWORD=mot-de-passe-local
IAHL_INITIAL_EMAIL=admin@local
```

Puis lancer l'application.

Au premier démarrage, l'administrateur est créé.

Aux démarrages suivants, si un `ADMIN` existe déjà en base, aucune nouvelle création n'est effectuée.

### Ligne de commande

PowerShell :

```powershell
$env:IAHL_INITIAL_USERNAME="admin"
$env:IAHL_INITIAL_PASSWORD="mot-de-passe-local"
$env:IAHL_INITIAL_EMAIL="admin@local"

./mvnw spring-boot:run
```

## Lancement avec Docker

Les mêmes variables sont injectées dans le container.

Exemple `docker-compose.yml` :

```yaml
services:
  iahomelab:
    environment:
      IAHL_INITIAL_USERNAME: ${IAHL_INITIAL_USERNAME}
      IAHL_INITIAL_PASSWORD: ${IAHL_INITIAL_PASSWORD}
      IAHL_INITIAL_EMAIL: ${IAHL_INITIAL_EMAIL}
```

Les valeurs peuvent venir de l'environnement de la machine ou d'un fichier `.env` local non versionné.

Exemple `.env` :

```text
IAHL_INITIAL_USERNAME=admin
IAHL_INITIAL_PASSWORD=mot-de-passe-local
IAHL_INITIAL_EMAIL=admin@local
```

Le fichier `.env` contenant des secrets doit être ignoré par Git.

## Comportement après initialisation

Les variables servent uniquement au bootstrap.

Une fois un administrateur présent :

```text
InitialAdminService
→ détecte l'ADMIN
→ ne crée rien
```

Changer ensuite les variables d'environnement ne doit pas modifier automatiquement le compte existant.

Les changements de mot de passe ou de profil devront passer par une fonctionnalité dédiée.

## Tests

Les tests d'intégration désactivent le bootstrap :

```properties
iahl.initial-admin.enabled=false
```

Les utilisateurs de test sont créés explicitement dans les classes de test.

Scénarios à tester :

```text
aucun ADMIN + variables présentes
→ création de l'admin

ADMIN existant
→ aucune création supplémentaire

aucun ADMIN + variables manquantes
→ démarrage refusé
```

## Règles à retenir

1. Aucun compte par défaut n'est créé par Flyway.
2. Aucun mot de passe n'est stocké dans les migrations SQL.
3. Le premier admin est créé à partir de variables d'environnement.
4. Le mécanisme fonctionne en lancement direct et via Docker.
5. Les tests désactivent ce bootstrap.
6. Les secrets locaux ne doivent jamais être versionnés.
7. Une fois l'admin créé, les variables d'initialisation ne modifient plus ce compte.

# Développement local

## Prérequis

- Java 17
- Docker avec Docker Compose
- Maven Wrapper fourni par le repository

Aucune installation locale de PostgreSQL n'est nécessaire lorsque `compose.yaml` est utilisé.

## Démarrer PostgreSQL

Depuis la racine du repository :

```bash
docker compose up -d postgres
```

Le compose démarre une instance PostgreSQL épinglée en version `18.3` avec la configuration attendue par `application.properties` :

```text
host: localhost
port: 5432
database: IAHomeLab
user: postgres
password: root
```

Les données sont persistées dans le volume Docker `iahomelab_postgres_data`.

Vérifier l'état :

```bash
docker compose ps
```

Le service possède un healthcheck `pg_isready` ; il doit passer en état `healthy` avant le démarrage de l'application.

## Démarrer l'application

Linux/macOS :

```bash
./mvnw spring-boot:run
```

Windows :

```bat
mvnw.cmd spring-boot:run
```

L'API est disponible par défaut sur :

```text
http://localhost:8080
```

Flyway applique automatiquement les migrations présentes dans :

```text
src/main/resources/db/migration
```

## Tests

Tests unitaires :

```bash
./mvnw test
```

Suite complète, y compris les tests d'intégration :

```bash
./mvnw verify
```

Les tests d'intégration utilisent leur propre PostgreSQL via Testcontainers et ne dépendent pas du conteneur local lancé avec Compose.

## Arrêter l'environnement

Conserver les données :

```bash
docker compose down
```

Réinitialiser également la base locale :

```bash
docker compose down -v
```

Le prochain `docker compose up -d postgres` repartira alors d'une base vide et Flyway reconstruira le schéma au démarrage de l'application.

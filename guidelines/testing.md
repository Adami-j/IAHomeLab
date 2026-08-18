# Testing Guide

Ce document décrit les conventions de test du projet **IAHomeLab**.

## Types de tests

### Tests unitaires

Convention :

```text
*Test.java
```

Ils doivent être rapides et ne pas démarrer Spring Boot, PostgreSQL ou Testcontainers.

Commande :

```bash
mvn test
```

### Tests d'intégration

Convention :

```text
*IT.java
```

Ils peuvent utiliser Spring Boot, JPA, Flyway, PostgreSQL et les API HTTP.

PostgreSQL est lancé avec **Testcontainers**.  
Le projet n'utilise pas H2 pour simuler PostgreSQL.

Commande :

```bash
mvn verify
```

## Base de données

Les tests d'intégration doivent pouvoir démarrer depuis une base PostgreSQL vide :

```text
PostgreSQL vide
    ↓
Flyway
    ↓
Spring Boot
    ↓
Tests
```

Les migrations Flyway sont la source de vérité du schéma.

Une migration déjà appliquée ne doit jamais être modifiée. Toute évolution du schéma doit créer une nouvelle migration.

## Tests API

Les endpoints importants doivent être testés de bout en bout lorsque nécessaire :

```text
HTTP
 ↓
Controller
 ↓
Service
 ↓
Repository
 ↓
PostgreSQL
```

Les tests doivent vérifier selon le cas :

- le code HTTP ;
- la réponse ;
- les validations ;
- les erreurs ;
- les autorisations ;
- les données réellement enregistrées.

## Régression

Lorsqu'un bug significatif est corrigé :

```text
Reproduire le bug
    ↓
Écrire un test qui échoue
    ↓
Corriger
    ↓
Vérifier que le test passe
```

Le test reste ensuite dans la suite afin d'éviter que le bug réapparaisse.

## Cycle d'une feature

```text
Conception
 ↓
Implémentation
 ↓
Tests unitaires
 ↓
Tests d'intégration / API
 ↓
mvn verify
 ↓
Merge
```

## Definition of Done

Une feature est terminée lorsque :

- [ ] les règles métier sont testées ;
- [ ] les migrations nécessaires sont présentes ;
- [ ] les endpoints importants sont testés ;
- [ ] les cas de sécurité pertinents sont testés ;
- [ ] les bugs corrigés disposent d'un test de non-régression lorsque pertinent ;
- [ ] `mvn verify` passe.

## Commandes

Tests unitaires :

```bash
mvn test
```

Suite complète :

```bash
mvn verify
```

Validation complète depuis un environnement propre :

```bash
mvn clean verify
```

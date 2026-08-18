# Architecture des packages

IAHomeLab utilise une organisation **par module métier**, avec quelques packages transverses partagés.

## Racine

Le code applicatif se trouve sous :

```text
fr.lab.iahomelab
```

`IaHomeLabApplication` reste à la racine afin que le component scan Spring couvre naturellement tous les modules.

## Modules métier

Chaque fonctionnalité métier possède son propre package de premier niveau. Exemple actuel :

```text
fr.lab.iahomelab
├── source
│   ├── controller
│   │   └── dto
│   ├── entity
│   ├── repository
│   └── service
├── sourceidea
│   ├── controller
│   │   └── dto
│   ├── entity
│   ├── repository
│   └── service
└── security
    ├── config
    └── ...
```

Les futurs modules (`setup`, `experiment`, `execution`, `evaluation`, etc.) suivent la même convention.

### Rôle des sous-packages

- `controller` : exposition HTTP REST et orchestration de la couche web.
- `controller.dto` : objets d'entrée/sortie de l'API. Les entités JPA ne sont pas exposées directement.
- `service` : règles métier, transactions et orchestration applicative.
- `repository` : accès aux données avec Spring Data JPA.
- `entity` : modèle persistant JPA et enums fortement liés au domaine.

## Packages transverses

Les éléments réellement partagés entre plusieurs modules vivent dans `common` :

```text
common
├── api
├── entity
└── exception
```

- `common.api` : conventions globales de l'API (`/api/v1`, annotation `ApiV1Controller`, réponses communes).
- `common.entity` : primitives de persistance partagées, notamment `BaseEntity`.
- `common.exception` : erreurs API et gestion globale des exceptions.

Une classe ne doit être placée dans `common` que si elle est effectivement utilisée par plusieurs modules. Les concepts métier restent dans leur module propriétaire.

## Dépendances recommandées

Le flux principal reste simple :

```text
Controller -> Service -> Repository -> Entity/PostgreSQL
```

Un module peut référencer un autre module lorsque le domaine le nécessite. Exemple : `sourceidea` référence `Source`, puisqu'une idée ou un claim appartient à une source.

Pour la V1, aucun découpage hexagonal supplémentaire (`domain/application/infrastructure`, ports/adapters, etc.) n'est imposé. Cette complexité ne sera introduite que lorsqu'un besoin concret la justifiera.

## API

Les controllers applicatifs utilisent `@ApiV1Controller`. Le préfixe `/api/v1` est appliqué globalement ; les controllers déclarent uniquement leur chemin métier :

```java
@ApiV1Controller
@RequestMapping("/sources")
class SourceController {
}
```

Résultat :

```text
/api/v1/sources
```

## Tests

Les tests reproduisent autant que possible la structure du code principal.

Convention Maven :

- `*Test` : tests unitaires exécutés par Surefire avec `mvn test`.
- `*IT` : tests d'intégration exécutés par Failsafe avec `mvn verify`.
- Les tests d'intégration de persistance utilisent PostgreSQL via Testcontainers ; H2 n'est pas utilisé.

Voir aussi `guidelines/docs/testing.md`.

## Règles de conception

1. Préférer un module métier clair à un gros package technique global.
2. Ne pas exposer une entité JPA directement depuis un controller.
3. Garder les transactions dans la couche service.
4. Mettre les conventions réellement transverses dans `common`.
5. Éviter les abstractions anticipées tant qu'elles n'apportent pas de bénéfice concret au projet.

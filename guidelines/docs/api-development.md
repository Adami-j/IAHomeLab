# API Development Guide

Ce document décrit les conventions utilisées pour développer les API REST de **IAHomeLab**.

## Structure

Les éléments communs sont placés dans :

```text
fr.lab.iahomelab.common
├── api
│   ├── ApiController.java
│   └── ApiPaths.java
├── entity
│   └── BaseEntity.java
└── exception
    ├── ApiError.java
    ├── GlobalExceptionHandler.java
    └── ResourceNotFoundException.java
```

Chaque domaine conserve ensuite ses propres packages :

```text
research
├── entity
├── repository
├── service
└── controller
    └── dto
```

## Versionnement des API

Toutes les API publiques utilisent le préfixe :

```text
/api/v1
```

Le préfixe est centralisé dans `ApiPaths`.

Exemple :

```java
@ApiController
@RequestMapping(ApiPaths.API_V1 + "/sources")
public class SourceController {
}
```

## Controllers

Les controllers utilisent `@ApiController` pour éviter de répéter la configuration REST commune.

Les controllers doivent rester fins : la logique métier appartient aux services.

Éviter les `CrudController<T, ID>` génériques. Les endpoints restent explicites afin de pouvoir évoluer facilement lorsque des règles métier spécifiques apparaissent.

## DTO

Une entité JPA ne doit jamais être exposée directement par un controller.

Utiliser des DTO dédiés, par exemple :

```text
CreateSourceRequest
UpdateSourceRequest
SourceResponse
```

Flux attendu :

```text
Request DTO
    ↓
Controller
    ↓
Service
    ↓
Repository / Entity
```

Puis :

```text
Entity
  ↓
Service
  ↓
Response DTO
```

Cela évite de coupler directement le contrat HTTP au schéma de base de données.

## Validation

Les validations sont placées sur les DTO d'entrée.

Exemple :

```java
public record CreateSourceRequest(
        @NotBlank String title,
        @NotBlank String type,
        String url
) {
}
```

Dans le controller :

```java
@Valid @RequestBody CreateSourceRequest request
```

## Convention HTTP

```text
GET    /api/v1/resources        → 200 OK
GET    /api/v1/resources/{id}   → 200 OK / 404 Not Found
POST   /api/v1/resources        → 201 Created
PUT    /api/v1/resources/{id}   → 200 OK / 404 Not Found
DELETE /api/v1/resources/{id}   → 204 No Content / 404 Not Found
```

Codes d'erreur principaux :

```text
400 → requête invalide
401 → non authentifié
403 → accès interdit
404 → ressource inexistante
409 → conflit métier
500 → erreur interne
```

## Exceptions

Les controllers et services lèvent des exceptions métier au lieu de construire eux-mêmes les réponses HTTP.

Exemple :

```java
throw new ResourceNotFoundException("Source not found");
```

`GlobalExceptionHandler` transforme ensuite l'exception en réponse HTTP uniforme :

```json
{
  "code": "RESOURCE_NOT_FOUND",
  "message": "Source not found",
  "timestamp": "2026-08-18T12:10:00+02:00"
}
```

Voir aussi `docs/exceptions.md`.

## BaseEntity

Les entités JPA peuvent hériter de `BaseEntity` pour partager les champs techniques communs :

```text
id
createdAt
updatedAt
```

`BaseEntity` est une `@MappedSuperclass`.

La génération des identifiants et la gestion des timestamps doivent rester cohérentes avec PostgreSQL et les migrations Flyway.

## Modèle de controller

```java
@ApiController
@RequestMapping(ApiPaths.API_V1 + "/sources")
@RequiredArgsConstructor
public class SourceController {

    private final SourceService sourceService;

    @PostMapping
    public ResponseEntity<SourceResponse> create(
            @Valid @RequestBody CreateSourceRequest request
    ) {
        SourceResponse response = sourceService.create(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/{id}")
    public SourceResponse findById(@PathVariable UUID id) {
        return sourceService.findById(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        sourceService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
```

## Tests API

Les endpoints importants doivent être couverts par des tests d'intégration :

```text
HTTP
 ↓
Controller
 ↓
Service
 ↓
Repository
 ↓
PostgreSQL Testcontainers
```

Convention :

```text
*IT.java
```

Avant merge :

```bash
mvn verify
```

Voir aussi `docs/testing.md`.

## Règles à retenir

1. Toutes les API publiques sont sous `/api/v1`.
2. Les controllers utilisent `@ApiController`.
3. Une entité JPA n'est jamais exposée directement.
4. Les entrées et sorties HTTP utilisent des DTO.
5. Les validations sont placées sur les DTO d'entrée.
6. Les erreurs sont centralisées dans `GlobalExceptionHandler`.
7. La logique métier reste dans les services.
8. Les endpoints importants sont couverts par des tests d'intégration.
9. Les abstractions CRUD génériques sont évitées.
10. `mvn verify` doit être vert avant merge.

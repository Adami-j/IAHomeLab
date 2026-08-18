# Gestion des exceptions

IAHomeLab utilise une gestion centralisée des erreurs REST avec `@RestControllerAdvice`.

L'objectif est que toutes les erreurs de l'API retournent un format cohérent.

## Structure

Les classes communes sont placées dans :

```text
fr.lab.iahomelab.common.exception
├── ApiError.java
├── GlobalExceptionHandler.java
└── ResourceNotFoundException.java
```

## Format d'une erreur API

Les erreurs REST utilisent `ApiError` :

```json
{
  "code": "RESOURCE_NOT_FOUND",
  "message": "User not found",
  "timestamp": "2026-08-18T12:10:00+02:00"
}
```

- `code` : identifiant stable de l'erreur ;
- `message` : détail lisible ;
- `timestamp` : date de génération de la réponse.

## Lever une exception

Le code métier ne construit pas directement une réponse HTTP.

Exemple :

```java
throw new ResourceNotFoundException("User not found");
```

L'exception remonte ensuite jusqu'au `GlobalExceptionHandler`.

## GlobalExceptionHandler

`GlobalExceptionHandler` transforme les exceptions Java en réponses HTTP.

Exemple :

```text
Service
  ↓
ResourceNotFoundException
  ↓
GlobalExceptionHandler
  ↓
404 Not Found
  ↓
ApiError
```

Une exception non prévue est interceptée par le handler générique et retourne une erreur `500 INTERNAL_SERVER_ERROR`.

Les détails techniques internes ne doivent pas être exposés au client.

## Ajouter une nouvelle exception

Créer une exception spécifique uniquement lorsqu'un besoin métier apparaît.

Exemples possibles :

```text
ResourceNotFoundException → 404
ConflictException         → 409
ForbiddenException        → 403
```

Puis ajouter un `@ExceptionHandler` correspondant dans `GlobalExceptionHandler`.

Il n'est pas nécessaire de créer une classe d'exception pour chaque erreur possible.

## Règle générale

Les controllers et services lèvent des exceptions métier.

La conversion vers HTTP est centralisée dans `GlobalExceptionHandler`.

Cela permet de garder :

- des controllers simples ;
- un format d'erreur uniforme ;
- les règles HTTP centralisées ;
- une API plus facile à tester.

# Security Guide

Ce document décrit la sécurité actuelle de **IAHomeLab** et les évolutions prévues.

Il sert de référence simple pour les développeurs et pour les assistants IA qui interviennent sur le projet.

## Objectifs

La sécurité de IAHomeLab repose sur quelques principes simples :

- authentifier les utilisateurs avec Spring Security ;
- utiliser des sessions HTTP plutôt que des JWT pour la V1 ;
- stocker les mots de passe uniquement sous forme de hash ;
- séparer l'utilisateur métier de ses moyens d'authentification ;
- permettre l'ajout futur de OAuth2 / OIDC sans refonte complète ;
- garder les endpoints métier protégés par défaut.

## Modèle utilisateur

Deux entités principales existent :

```text
AppUser
  ↓ 1..N
UserIdentity
```

### AppUser

`AppUser` représente l'utilisateur IAHomeLab.

Exemples de données :

```text
username
email
displayName
role
enabled
```

Rôles actuels :

```text
USER
ADMIN
```

### UserIdentity

`UserIdentity` représente une méthode d'authentification liée à un `AppUser`.

Types actuels :

```text
LOCAL
OIDC
OAUTH2
```

Exemples :

```text
LOCAL  / local  / admin
OIDC   / google / subject-google
OAUTH2 / github / subject-github
```

Le champ `provider` reste une chaîne afin de pouvoir ajouter de nouveaux fournisseurs sans modifier le modèle.

## Authentification locale

Pour une identité locale :

```text
provider = local
providerSubject = username
passwordHash = hash du mot de passe
```

Le mot de passe brut n'est jamais enregistré en base.

Le hash est généré avec le `PasswordEncoder` Spring Security.

## Chaîne d'authentification

Le flux actuel est :

```text
username + password
        ↓
AuthenticationManager
        ↓
DaoAuthenticationProvider
        ↓
CustomUserDetailsService
        ↓
UserIdentityRepository
        ↓
UserIdentity + AppUser
        ↓
PasswordEncoder
        ↓
Authentication
```

`CustomUserDetailsService` charge l'identité locale depuis PostgreSQL et construit le `UserDetails` utilisé par Spring Security.

## Sessions HTTP

La V1 utilise une session HTTP :

```text
Browser
  ↓
Login
  ↓
Spring Security
  ↓
SecurityContext
  ↓
HttpSession
  ↓
Cookie de session
```

JWT n'est pas utilisé pour l'instant.

OAuth2 / OIDC pourra être ajouté plus tard tout en conservant une session locale après authentification.

## Protection des endpoints

Convention actuelle :

```text
/api/v1/auth/**        → public
/actuator/health       → public
/api/v1/**             → authentification requise
```

Les règles métier plus fines seront ajoutées ensuite.

## CSRF

Comme l'application utilise des cookies de session, la protection CSRF doit rester activée.

Ne pas désactiver CSRF globalement sans raison explicite.

## Gestion des mots de passe

Règles :

- jamais de mot de passe brut en base ;
- jamais de mot de passe dans les logs ;
- jamais de mot de passe versionné dans Git ;
- utiliser uniquement `PasswordEncoder` pour encoder et vérifier les mots de passe.

## OAuth2 / OIDC plus tard

L'architecture est prévue pour permettre :

```text
Google
GitHub
Microsoft
Keycloak
Authentik
autres fournisseurs
```

Le futur flux sera :

```text
Provider externe
      ↓
Spring Security OAuth2/OIDC
      ↓
provider + providerSubject
      ↓
UserIdentity
      ↓
AppUser
      ↓
HttpSession
```

## Tests de sécurité

Scénarios importants :

```text
login valide
login invalide
utilisateur désactivé
endpoint protégé sans session
endpoint protégé avec session
USER sur endpoint ADMIN
logout
```

## Évolutions prévues

```text
V1
- login local
- session HTTP
- rôles USER / ADMIN
- endpoint /auth/me
- logout

V2
- OAuth2 / OIDC
- liaison de plusieurs identités à un compte
- règles d'autorisation métier

Plus tard
- audit de sécurité
- gestion avancée des sessions
- politique de mot de passe si nécessaire
- authentification forte si besoin
```

## Règles à retenir

1. `AppUser` représente l'utilisateur métier.
2. `UserIdentity` représente un moyen d'authentification.
3. Les mots de passe sont toujours hashés.
4. La V1 utilise des sessions HTTP.
5. Les endpoints métier sont protégés par défaut.
6. CSRF reste activé avec les cookies de session.
7. OAuth2 / OIDC doit pouvoir être ajouté sans refonte du modèle.
8. Toute évolution importante de la sécurité doit mettre à jour ce document.

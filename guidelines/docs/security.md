# Security Guide

Ce document est la référence rapide pour comprendre et faire évoluer la sécurité de **IAHomeLab**.

L'objectif est volontairement simple : savoir **ce qui est sécurisé aujourd'hui**, **où ajouter une nouvelle règle**, et **comment la tester** sans réinventer l'architecture.

---

## 1. Vue d'ensemble

IAHomeLab utilise actuellement :

```text
Authentification locale
        ↓
Spring Security
        ↓
Session HTTP
        ↓
Cookie de session
```

Principes V1 :

- authentification gérée par Spring Security ;
- session HTTP, pas de JWT ;
- CSRF activé ;
- mots de passe toujours hashés ;
- endpoints métier protégés par défaut ;
- OAuth2 / OIDC prévu plus tard sans refonte du modèle utilisateur.

La configuration centrale se trouve dans :

```text
security/config/SecurityConfiguration.java
```

---

## 2. Modèle utilisateur

Le modèle sépare l'utilisateur de ses moyens d'authentification :

```text
AppUser
   1
   │
   └── * UserIdentity
```

### `AppUser`

Représente l'utilisateur IAHomeLab.

Il porte notamment :

```text
username
email
displayName
role
enabled
```

Rôles actuellement définis :

```text
USER
ADMIN
```

Ces rôles existent dans le modèle, mais **aucune règle métier USER vs ADMIN n'est imposée actuellement**.

Ne pas créer une restriction artificielle uniquement parce que le rôle existe.

### `UserIdentity`

Représente une méthode d'authentification associée à un `AppUser`.

Types prévus :

```text
LOCAL
OIDC
OAUTH2
```

Pour une identité locale :

```text
provider = local
providerSubject = username
passwordHash = hash du mot de passe
```

Cette séparation permet plus tard d'associer une identité GitHub, Google, Keycloak, etc. au même utilisateur interne.

---

## 3. Authentification locale

Flux actuel :

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

`CustomUserDetailsService` charge l'identité locale et construit le `UserDetails` utilisé par Spring Security.

Le `PasswordEncoder` est fourni par :

```java
PasswordEncoderFactories.createDelegatingPasswordEncoder()
```

### Règle absolue sur les mots de passe

- jamais de mot de passe brut en base ;
- jamais de mot de passe dans les logs ;
- jamais de mot de passe versionné dans Git ;
- toujours utiliser `PasswordEncoder` pour encoder et vérifier un mot de passe.

---

## 4. Session HTTP

Après authentification, le contexte de sécurité est conservé dans la session HTTP :

```text
Login réussi
     ↓
SecurityContext
     ↓
HttpSession
     ↓
Cookie de session
```

Le repository utilisé est :

```text
HttpSessionSecurityContextRepository
```

Conséquence importante : le frontend doit conserver le cookie de session lors des appels suivants.

JWT n'est pas utilisé dans la V1.

---

## 5. Endpoints d'authentification

Endpoints actuels :

```text
GET  /api/v1/auth/csrf
POST /api/v1/auth/login
GET  /api/v1/auth/me
POST /api/v1/auth/logout
```

Règles actuelles dans `SecurityConfiguration` :

```text
/actuator/health          → public
/api/v1/auth/csrf         → public
/api/v1/auth/login        → public
tout le reste             → utilisateur authentifié
```

Cela signifie notamment que :

```text
/api/v1/sources/**
/api/v1/... métier ...
```

sont automatiquement protégés tant qu'aucune règle plus spécifique n'est ajoutée.

### Convention importante

**Protection par défaut.**

Lorsqu'un nouvel endpoint métier est créé, ne pas ajouter `permitAll()` par habitude.

Un endpoint public doit être une décision explicite.

---

## 6. CSRF

CSRF reste activé parce que l'application utilise une authentification par cookie de session.

Ne pas faire :

```java
csrf(csrf -> csrf.disable())
```

pour résoudre rapidement un problème de test ou de frontend.

Pour une requête qui modifie l'état :

```text
POST
PUT
PATCH
DELETE
```

le client doit fournir un token CSRF valide.

### Conséquence dans les tests

Pour tester réellement l'absence d'authentification sur une requête d'écriture, fournir d'abord un CSRF valide.

Sinon le filtre CSRF répondra `403` avant que Spring Security ne vérifie l'utilisateur.

Exemple :

```java
mockMvc.perform(post("/api/v1/example")
        .with(csrf()))
    .andExpect(status().isUnauthorized());
```

Matrice utile :

```text
anonyme + pas de CSRF   → 403
anonyme + CSRF valide   → 401
authentifié + pas CSRF  → 403
authentifié + CSRF      → endpoint métier
```

---

## 7. Ajouter une nouvelle règle de sécurité

La règle générale est : **ajouter une restriction uniquement lorsqu'un besoin métier réel existe**.

### Cas A — nouvel endpoint métier standard

Aucune configuration supplémentaire n'est normalement nécessaire.

Avec :

```java
.anyRequest().authenticated()
```

le nouvel endpoint est déjà protégé.

Il faut seulement tester qu'un utilisateur anonyme ne peut pas y accéder.

### Cas B — nouvel endpoint public

Ajouter une règle explicite avant `anyRequest()` :

```java
.requestMatchers("/api/v1/example/public").permitAll()
```

Puis tester :

```text
anonyme → accès autorisé
```

Un endpoint public doit rester exceptionnel.

### Cas C — future règle ADMIN

À ajouter seulement lorsqu'une fonctionnalité ADMIN existe réellement.

Exemple de direction :

```java
.requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
```

Tests minimum à ajouter à ce moment-là :

```text
anonyme → 401
USER    → 403
ADMIN   → succès
```

Ne pas ajouter cette règle aujourd'hui : aucun endpoint métier ne nécessite encore cette séparation.

### Cas D — règle métier plus fine

Si une règle dépend du contenu métier plutôt que d'une simple URL, éviter de transformer `SecurityConfiguration` en liste infinie de cas.

Exemples futurs :

```text
un utilisateur ne peut modifier que sa propre ressource
une opération est autorisée uniquement dans un certain état
un ADMIN peut exécuter une opération supplémentaire
```

Dans ce cas :

1. définir clairement la règle métier ;
2. choisir l'endroit le plus proche du besoin pour l'appliquer ;
3. ajouter des tests d'autorisation explicites ;
4. documenter la nouvelle convention ici si elle devient générale.

---

## 8. Comment tester une nouvelle règle

Chaque nouvelle règle de sécurité doit être testée sur ses frontières importantes.

### Endpoint authentifié standard

Minimum :

```text
anonyme     → 401
authentifié → succès
```

Pour une requête d'écriture, ajouter le cas CSRF :

```text
authentifié + sans CSRF → 403
authentifié + CSRF      → succès
```

### Endpoint avec autorisation particulière

Tester chaque acteur significatif :

```text
anonyme
utilisateur autorisé
utilisateur authentifié mais non autorisé
```

Ne pas tester uniquement le "happy path".

### Convention MockMvc actuelle

Pour simuler un utilisateur dans les tests d'intégration :

```java
.with(user("test-user").roles("USER"))
```

Et pour les requêtes d'écriture :

```java
.with(csrf())
```

---

## 9. Codes HTTP attendus

Convention actuelle :

```text
401 Unauthorized
    utilisateur non authentifié

403 Forbidden
    utilisateur authentifié mais non autorisé
    ou requête refusée par CSRF
```

Ne pas utiliser `403` pour représenter un simple utilisateur non connecté si la requête a déjà passé le contrôle CSRF.

---

## 10. Bootstrap administrateur

IAHomeLab peut créer un administrateur initial au démarrage selon la configuration applicative.

Cette fonctionnalité sert au bootstrap local de l'application.

Les tests d'intégration doivent pouvoir la désactiver afin de conserver une base de test déterministe.

Configuration de test actuelle :

```properties
iahl.initial-admin.enabled=false
```

Ne jamais coder un mot de passe administrateur directement dans le code Java.

---

## 11. OAuth2 / OIDC plus tard

Le modèle `AppUser` / `UserIdentity` est prévu pour permettre :

```text
Google
GitHub
Microsoft
Keycloak
Authentik
...
```

Direction cible :

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

La session HTTP peut donc rester le mécanisme utilisé par l'application après une authentification externe.

---

## 12. Checklist avant de merger une évolution sécurité

Pour toute modification touchant Spring Security, l'authentification ou les autorisations :

- [ ] la règle répond à un besoin réel ;
- [ ] les endpoints restent protégés par défaut ;
- [ ] CSRF n'a pas été désactivé pour contourner un problème ;
- [ ] aucun mot de passe ou secret n'est loggé ou versionné ;
- [ ] le cas anonyme est testé ;
- [ ] le cas autorisé est testé ;
- [ ] le cas interdit est testé lorsqu'il existe ;
- [ ] les requêtes d'écriture couvrent CSRF ;
- [ ] `mvn verify` passe ;
- [ ] ce document est mis à jour si une nouvelle convention générale apparaît.

---

## 13. Règles à retenir

Si on ne doit retenir que quelques points :

1. **Tout endpoint métier est authentifié par défaut.**
2. **Un endpoint public doit être explicitement justifié.**
3. **Session HTTP, pas JWT pour la V1.**
4. **CSRF reste activé.**
5. **Les mots de passe passent toujours par `PasswordEncoder`.**
6. **Ne pas inventer de règles USER/ADMIN sans besoin métier.**
7. **Toute nouvelle règle doit avoir des tests sur ses frontières.**
8. **`AppUser` est l'utilisateur ; `UserIdentity` est son moyen d'authentification.**
9. **OAuth2/OIDC doit pouvoir être ajouté sans casser ce modèle.**
10. **Une nouvelle convention de sécurité doit être documentée ici.**

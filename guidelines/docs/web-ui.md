# Web UI — architecture transitoire sans NPM

## Objectif

La première interface web d'IAHomeLab sert à construire et valider l'UX sans introduire de toolchain Node/NPM.

Stack retenue :

```text
Spring Boot
├── Spring MVC
├── Thymeleaf
├── JavaScript vanilla
├── CSS maison
├── HTMX          (plus tard, uniquement pour des interactions ciblées)
└── Cytoscape.js  (plus tard, pour le graphe Setup)
```

Le build reste Maven uniquement :

```bash
mvn spring-boot:run
mvn clean verify
```

Pas de `package.json`, `node_modules`, Vite ou Webpack pendant cette phase.

---

## Architecture

L'interface HTML ne remplace pas l'API REST.

```text
                         ┌───────────────────────┐
                         │ REST Controllers      │
                         │ /api/v1/**            │
                         │ JSON                  │
                         └───────────┬───────────┘
                                     │
Future SPA / clients API ────────────┤
                                     ▼
                              Services métier
                                     ▲
Browser actuel ──────────────────────┤
                         ┌───────────┴───────────┐
                         │ MVC Controllers       │
                         │ /app/**               │
                         │ Thymeleaf / HTML      │
                         └───────────────────────┘
```

Règle :

```text
Web Controller
      ↓
Service métier
      ↓
Repository
      ↓
PostgreSQL
```

La logique métier ne doit pas être dupliquée dans les contrôleurs MVC, Thymeleaf ou JavaScript.

---

## Organisation des fichiers

```text
src/main/java/fr/lab/iahomelab/
├── source/
├── setup/
└── web/
    └── controller/
        ├── HomePageController.java
        ├── LoginPageController.java
        ├── NavigationPageController.java
        ├── SessionPageController.java
        ├── SourcePageController.java
        └── SetupPageController.java

src/main/resources/
├── templates/
│   ├── layouts/
│   │   └── app.html
│   ├── fragments/
│   │   ├── sidebar.html
│   │   └── topbar.html
│   ├── pages/
│   │   ├── home.html
│   │   ├── login.html
│   │   ├── experiments.html
│   │   └── findings.html
│   ├── source/
│   │   ├── list.html
│   │   └── form.html
│   └── setup/
│       ├── list.html
│       ├── new.html
│       ├── edit.html
│       ├── detail.html
│       └── workspace.html
│
└── static/
    ├── css/
    │   ├── tokens.css
    │   ├── themes.css
    │   ├── base.css
    │   ├── layout.css
    │   └── components.css
    ├── js/
    │   └── app.js
    └── vendor/
        └── README.md
```

---

## Navigation

Sidebar :

```text
Home
Research
Setups
Experiments
Findings
```

Une entrée visible doit conduire vers une vraie route. Pas de `href="#"` pour les actions principales.

`Experiments` et `Findings` restent des pages d'attente tant que leurs features métier ne sont pas implémentées.

---

## Authentification navigateur

Le navigateur utilise le même compte local que l'API, mais via `formLogin` Spring Security.

```text
GET /app sans session
        ↓
302 /login
        ↓
POST /login + CSRF
        ↓
session HTTP authentifiée
        ↓
302 /app
```

Le compte local vient du bootstrap :

```text
IAHL_INITIAL_USERNAME
IAHL_INITIAL_PASSWORD
```

Comportement attendu :

```text
/api/** sans authentification  → 401
/app/** sans authentification  → 302 /login
```

Déconnexion navigateur :

```text
POST /app/logout + CSRF
        ↓
session invalidée
        ↓
302 /login?logout
```

L'endpoint REST `/api/v1/auth/logout` reste séparé.

---

## Research / Sources

La page `Research` est maintenant la vue de gestion des `Source`.

Routes HTML :

```text
GET  /app/research
GET  /app/research/new
POST /app/research
GET  /app/research/{sourceId}/edit
POST /app/research/{sourceId}/edit
POST /app/research/{sourceId}/delete
```

Flux de création :

```text
Nouvelle source
      ↓
formulaire Thymeleaf
      ↓
SourcePageController
      ↓
SourceService.create(...)
      ↓
PostgreSQL
      ↓
redirect /app/research
```

Le formulaire supporte :

```text
title
url ou storagePath
type
status
fileName
mimeType
summary
notes
tags
```

Les tags sont saisis sous forme de liste séparée par des virgules puis convertis en `Set<String>` côté contrôleur MVC.

L'API REST dispose aussi de la liste et de la suppression :

```text
GET    /api/v1/sources
DELETE /api/v1/sources/{id}
```

---

## Setups

Routes HTML :

```text
GET  /app/setups
GET  /app/setups/new
POST /app/setups
GET  /app/setups/{setupId}
GET  /app/setups/{setupId}/edit
POST /app/setups/{setupId}/edit
POST /app/setups/{setupId}/delete
```

Flux :

```text
liste
  ↓
création / ouverture
  ↓
détail du Setup
  ├── modifier
  ├── supprimer
  └── gérer les versions
```

La suppression suit la règle métier existante :

```text
Setup sans version FROZEN  → suppression possible
Setup avec version FROZEN  → suppression interdite
```

La règle reste appliquée par `SetupService`, pas par le template.

---

## SetupVersion

Routes HTML :

```text
POST /app/setups/{setupId}/versions
GET  /app/setups/{setupId}/versions/{versionId}
POST /app/setups/{setupId}/versions/{versionId}/update
POST /app/setups/{setupId}/versions/{versionId}/freeze
POST /app/setups/{setupId}/versions/{versionId}/delete
```

Cycle UX :

```text
création
   ↓
DRAFT
├── description modifiable
├── suppression possible
├── composants / connexions modifiables
└── action "Figer"
        ↓
      FROZEN
      └── consultation uniquement
```

Le navigateur ne recrée aucune règle métier : `SetupVersionService` décide si une version peut être modifiée, figée ou supprimée.

---

## Setup Workspace

Le workspace charge :

```text
Setup
SetupVersion
ComponentInstance[]
Connection[]
```

Structure cible :

```text
┌─────────────┬────────────────────────────────┬─────────────────┐
│ navigation  │          graph canvas          │    inspector    │
│             │                                │                 │
│ Research    │  Prompt ──▶ Retriever          │ selected node   │
│ Setups      │                │               │ type            │
│ Experiments │                ▼               │ configuration   │
│ Findings    │               LLM              │ actions         │
└─────────────┴────────────────────────────────┴─────────────────┘
```

Pour l'instant, le workspace affiche les composants existants et le nombre de connexions. L'édition visuelle du graphe viendra après validation de l'UX.

---

## CSRF

Toutes les actions HTML mutantes utilisent `POST` et transmettent le token CSRF Thymeleaf :

```html
<input type="hidden" th:name="${_csrf.parameterName}" th:value="${_csrf.token}">
```

Le layout expose aussi le token dans des balises `meta` pour les futures requêtes HTMX.

CSRF ne doit pas être désactivé pour simplifier l'UI.

---

## CSS

Responsabilités :

```text
tokens.css       → espacements, rayons, tailles, typo
themes.css       → couleurs et identité visuelle
base.css         → styles HTML globaux
layout.css       → sidebar, topbar, zones d'écran
components.css   → boutons, panels, cards, formulaires, badges
```

La palette actuelle reste provisoire jusqu'à validation explicite de la charte.

---

## HTMX et Cytoscape

HTMX sera ajouté uniquement quand un rechargement partiel apporte un vrai gain UX.

Cytoscape.js sera ajouté uniquement lorsque le graphe Setup deviendra interactif.

Les bibliothèques navigateur seront placées dans `static/vendor/` avec une version explicite. Aucun NPM n'est requis.

---

## Tests

Les tests MVC suivent la stratégie du projet :

```text
@SpringBootTest
@AutoConfigureMockMvc
@Import(PostgresTestConfiguration.class)
```

Ils couvrent notamment :

```text
login / logout
redirection anonyme
navigation
création / modification / suppression de Source
création / modification / suppression de Setup
création / modification / freeze / suppression de SetupVersion
rendu du workspace
CSRF sur les mutations
```

Les tests REST restent séparés des tests MVC.

---

## Migration future vers une SPA

Aujourd'hui :

```text
Browser
  ↓
Thymeleaf / MVC
  ↓
Services
```

Plus tard si nécessaire :

```text
React / Vue / Svelte
        ↓
      /api/v1
        ↓
REST Controllers
        ↓
Services
```

Les services, repositories, entités et contrats REST restent réutilisables.

---

## Règles de développement UI

1. Pas de NPM pendant cette phase.
2. Pas de logique métier en JavaScript.
3. Pas d'entités JPA directement dans les templates.
4. Réutiliser les services et DTOs existants.
5. Garder `/api/v1/**` indépendant de `/app/**`.
6. Toutes les mutations HTML restent protégées par CSRF.
7. Ajouter HTMX uniquement pour une interaction concrète.
8. Ajouter Cytoscape uniquement quand le graphe devient interactif.
9. Garder les couleurs dans `themes.css`.
10. Garder dimensions et espacements dans `tokens.css`.
11. Ne pas afficher de bouton principal sans comportement réel.
12. Valider l'UX avant de multiplier les écrans.

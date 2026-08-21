# Web UI — architecture transitoire sans NPM

## Objectif

La première interface web d'IAHomeLab doit permettre de travailler rapidement sur l'UX et la charte graphique sans introduire immédiatement une toolchain Node/NPM.

La stack retenue pour cette phase est :

```text
Spring Boot
├── Spring MVC
├── Thymeleaf
├── HTMX           (ajouté lorsqu'une interaction en a besoin)
├── JavaScript vanilla
├── CSS maison
└── Cytoscape.js   (plus tard pour le graphe Setup)
```

Le build reste uniquement Maven :

```bash
mvn spring-boot:run
mvn clean verify
```

Il n'y a pas de `package.json`, `node_modules`, Vite, Webpack ou autre bundler frontend.

---

## Principe d'architecture

L'API REST existante reste indépendante de l'interface web.

```text
                         ┌───────────────────────┐
                         │  REST Controllers     │
                         │  /api/v1/**           │
                         │  JSON                 │
                         └───────────┬───────────┘
                                     │
Browser / future SPA ────────────────┤
                                     │
                         ┌───────────▼───────────┐
                         │      Services         │
                         │ logique métier        │
                         └───────────▲───────────┘
                                     │
Browser actuel ──────────────────────┤
                         ┌───────────┴───────────┐
                         │ MVC Page Controllers  │
                         │ /app/**               │
                         │ Thymeleaf / HTML      │
                         └───────────────────────┘
```

Les contrôleurs MVC ne remplacent pas les contrôleurs REST. Ils utilisent les mêmes services métier et produisent des pages ou fragments HTML.

Cette séparation permet de remplacer plus tard Thymeleaf par React, Vue, Svelte ou une autre SPA sans réécrire le backend métier ni l'API `/api/v1`.

---

## Organisation du code

```text
src/main/java/fr/lab/iahomelab/
├── setup/
├── source/
├── sourceidea/
└── web/
    └── controller/
        ├── HomePageController.java
        ├── LoginPageController.java
        ├── NavigationPageController.java
        ├── SessionPageController.java
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
│   │   ├── research.html
│   │   ├── experiments.html
│   │   └── findings.html
│   └── setup/
│       ├── list.html
│       ├── new.html
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

Le package `web` contient uniquement la présentation MVC. La logique métier reste dans les services des modules existants.

```text
Web Controller
      ↓
Service métier
      ↓
Repository
      ↓
PostgreSQL
```

---

## Routes

### API JSON

```text
/api/v1/**
```

### Interface HTML

```text
GET  /login
POST /login
GET  /app
GET  /app/research
GET  /app/setups
GET  /app/setups/new
POST /app/setups
GET  /app/setups/{setupId}
POST /app/setups/{setupId}/versions
GET  /app/setups/{setupId}/versions/{versionId}
GET  /app/experiments
GET  /app/findings
POST /app/logout
```

`Research`, `Experiments` et `Findings` sont actuellement des pages accessibles mais restent des écrans d'attente tant que leurs fonctions métier ne sont pas branchées.

La section `Setups` est déjà branchée sur les services existants : liste, création, versions et ouverture du workspace.

---

## Navigation

La sidebar est la navigation principale :

```text
Home
Research
Setups
Experiments
Findings
```

Une entrée affichée doit toujours conduire vers une route réelle. Les `href="#"` sont à éviter : si une feature n'est pas encore disponible, elle mène vers un écran d'attente explicite.

---

## Authentification navigateur

L'interface HTML utilise le même compte local que l'API avec un formulaire Spring Security.

```text
GET /app
   ↓ anonyme
302 /login
   ↓
POST /login + CSRF
   ↓
session HTTP authentifiée
   ↓
302 /app
```

Le compte utilisé est celui créé au bootstrap avec :

```text
IAHL_INITIAL_USERNAME
IAHL_INITIAL_PASSWORD
```

Le comportement reste différent selon le client :

```text
/api/** sans authentification  → 401
/app/** sans authentification  → redirection /login
```

### Déconnexion

La topbar expose un bouton `Déconnexion` qui envoie :

```text
POST /app/logout + CSRF
```

Flux :

```text
clic Déconnexion
      ↓
POST /app/logout
      ↓
SecurityContextLogoutHandler
      ↓
session invalidée et contexte de sécurité nettoyé
      ↓
302 /login?logout
```

L'endpoint JSON `/api/v1/auth/logout` reste séparé et inchangé.

---

## Rendu d'une page

```text
GET /app
   ↓
HomePageController
   ↓
return "pages/home"
   ↓
Thymeleaf
   ↓
layouts/app.html
   ├── fragments/sidebar.html
   ├── fragments/topbar.html
   └── pages/home.html
   ↓
HTML envoyé au navigateur
```

Le layout contient la structure commune à toutes les pages.

---

## CSS et charte graphique

Les styles sont séparés selon leur responsabilité :

```text
tokens.css       → dimensions, espaces, rayons, typo
       +
themes.css       → couleurs / identité
       +
components.css   → boutons, panels, cards, formulaires
       +
layout.css       → sidebar, topbar, workspace
```

La palette actuelle reste une base de travail tant qu'elle n'est pas explicitement validée.

---

## HTMX

HTMX n'est pas nécessaire pour les premières pages. Il sera introduit lorsqu'une interaction mérite d'éviter un rechargement complet.

Exemple futur :

```text
clic sur Freeze
      ↓
HTMX POST
      ↓
Spring MVC
      ↓
SetupVersionService.freeze(...)
      ↓
fragment HTML
      ↓
HTMX remplace uniquement la zone concernée
```

Les bibliothèques navigateur seront stockées dans `static/vendor/`. Aucun NPM n'est nécessaire.

---

## CSRF et session

La sécurité reste basée sur la session HTTP Spring Security et CSRF.

Les formulaires Thymeleaf de login, création de setup, création de version et déconnexion incluent le token CSRF.

Le layout expose aussi le token dans des balises `meta` pour les futures requêtes HTMX.

On ne désactive pas CSRF pour simplifier le frontend.

---

## Setup Workspace

Le workspace `SetupVersion` est le premier écran métier important.

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

Le workspace charge maintenant depuis les services :

```text
Setup
SetupVersion
ComponentInstance[]
Connection[]
```

Le rendu reste simple tant que l'édition visuelle du graphe n'est pas conçue.

Règles UX :

- le graphe est le centre de l'expérience ;
- les formulaires servent le graphe, pas l'inverse ;
- une version `DRAFT` est éditable ;
- une version `FROZEN` devient visuellement read-only ;
- l'inspecteur latéral porte les détails du composant sélectionné ;
- les listes CRUD restent des vues secondaires.

---

## Graphe interactif

Lorsque les besoins seront clairs, Cytoscape.js pourra être ajouté dans `static/vendor/`.

```text
ComponentInstanceResponse[]
ConnectionResponse[]
       ↓
JavaScript
       ↓
Cytoscape.js
       ↓
graphe interactif
```

---

## Tests

Les tests MVC utilisent :

```text
@SpringBootTest
@AutoConfigureMockMvc
@Import(PostgresTestConfiguration.class)
```

Ils couvrent progressivement :

- redirection anonyme vers `/login` ;
- login navigateur ;
- navigation sidebar ;
- logout navigateur ;
- rendu de la liste Setup ;
- création d'un Setup par formulaire ;
- détail d'un Setup ;
- ouverture d'un workspace ;
- CSRF sur les actions mutantes.

Les tests REST restent séparés des tests de pages MVC.

---

## Migration future vers une SPA

Aujourd'hui :

```text
Browser
  ↓
Thymeleaf / HTMX
  ↓
Spring MVC
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

Les services, repositories, entités et API REST restent inchangés.

---

## Règles de développement UI

1. Pas de NPM pendant cette phase.
2. Pas de logique métier en JavaScript.
3. Pas d'entités JPA directement dans les templates.
4. Réutiliser les services et DTOs existants.
5. Garder `/api/v1/**` indépendant de `/app/**`.
6. Ajouter HTMX uniquement pour une interaction concrète.
7. Ajouter Cytoscape uniquement quand le graphe devient interactif.
8. Garder les couleurs dans `themes.css`.
9. Garder les dimensions et espacements dans `tokens.css`.
10. Ne pas afficher de lien ou bouton principal qui ne mène nulle part.
11. Valider l'UX avant de multiplier les écrans.

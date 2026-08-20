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

Les contrôleurs MVC ne remplacent pas les contrôleurs REST.

Ils utilisent les mêmes services métier et produisent des pages ou fragments HTML.

Cette séparation permet de remplacer plus tard Thymeleaf par React, Vue, Svelte ou une autre SPA sans réécrire le backend métier ni l'API `/api/v1`.

---

## Organisation du code

```text
src/main/java/fr/lab/iahomelab/
├── setup/                       # domaine existant
├── source/
├── sourceidea/
└── web/
    └── controller/
        └── HomePageController.java

src/main/resources/
├── templates/
│   ├── layouts/
│   │   └── app.html             # shell global de l'application
│   ├── fragments/
│   │   ├── sidebar.html
│   │   └── topbar.html
│   ├── pages/
│   │   └── home.html
│   └── setup/
│       ├── list.html
│       └── workspace.html
│
└── static/
    ├── css/
    │   ├── tokens.css           # tailles, espacements, rayons, typo
    │   ├── themes.css           # couleurs / identité visuelle
    │   ├── base.css             # reset et styles HTML globaux
    │   ├── layout.css           # sidebar, topbar, workspace
    │   └── components.css       # boutons, panels, badges, etc.
    ├── js/
    │   └── app.js               # JS global et intégration CSRF/HTMX
    └── vendor/
        └── README.md            # futures libs JS sans NPM
```

### Règle importante

Le package `web` contient uniquement la couche présentation MVC.

La logique métier reste dans les services des modules existants :

```text
Web Controller
      ↓
SetupService / SetupVersionService / ...
      ↓
Repository
      ↓
PostgreSQL
```

Aucune logique métier ne doit être déplacée dans Thymeleaf ou dans JavaScript.

---

## Routes

Deux espaces HTTP sont volontairement séparés.

### API JSON

```text
/api/v1/**
```

Exemples :

```text
/api/v1/setups
/api/v1/setup-versions/{id}
/api/v1/components/{id}
```

### Interface HTML

```text
/app/**
```

La première route créée est :

```text
GET /app
```

Les routes prévues ensuite sont par exemple :

```text
GET /app/setups
GET /app/setups/{setupId}
GET /app/setups/{setupId}/versions/{versionId}
```

---

## Rendu d'une page

Exemple pour la page d'accueil :

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

Les pages ne doivent pas recopier sidebar, topbar ou imports CSS.

---

## CSS et charte graphique

Les styles sont séparés selon leur responsabilité.

### `tokens.css`

Contient les règles structurelles stables :

```text
spacing
radius
font families
largeurs
ombres de colonnes
transitions
```

### `themes.css`

Contient uniquement l'identité visuelle :

```text
background
panels
borders
text
accent
danger
warning
```

Cela permet de changer la charte graphique sans réécrire les composants.

```text
tokens.css       → structure
       +
themes.css       → identité
       +
components.css   → composants
       +
layout.css       → assemblage des écrans
```

La palette actuellement présente est une base de travail et n'est pas considérée comme la charte finale tant qu'elle n'est pas explicitement validée.

---

## HTMX

HTMX n'est pas nécessaire pour afficher les premières pages.

Il sera introduit lorsqu'une interaction mérite d'éviter un rechargement complet.

Exemple futur : figer une `SetupVersion`.

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

L'objectif est d'éviter d'écrire prématurément une SPA complète pour des interactions simples.

Les bibliothèques navigateur seront stockées dans `static/vendor/` avec une version explicite. Aucun NPM n'est nécessaire.

---

## CSRF et session

La sécurité actuelle reste basée sur la session HTTP Spring Security et CSRF.

Les pages Thymeleaf peuvent accéder au token CSRF fourni par Spring Security.

Le layout expose le token dans des balises `meta` :

```html
<meta name="_csrf" ...>
<meta name="_csrf_header" ...>
```

`static/js/app.js` est préparé pour injecter automatiquement ce token dans les requêtes HTMX lorsque HTMX sera chargé.

Flux :

```text
Spring Security
      ↓
CSRF token
      ↓
Thymeleaf meta tags
      ↓
app.js
      ↓
HTMX request header
      ↓
Spring Security valide la mutation
```

On ne désactive donc pas CSRF pour simplifier le frontend.

---

## Premier écran métier cible : Setup Workspace

Le workspace `SetupVersion` reste le premier écran métier important.

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

Règles UX :

- le graphe est le centre de l'expérience ;
- les formulaires servent le graphe, pas l'inverse ;
- une version `DRAFT` est éditable ;
- une version `FROZEN` devient visuellement read-only ;
- l'inspecteur latéral porte les détails du composant sélectionné ;
- les listes CRUD restent disponibles comme vues secondaires.

La première version du fichier `setup/workspace.html` ne contient encore qu'un squelette de cette structure.

---

## Graphe interactif

Le premier rendu peut rester HTML/CSS.

Lorsque les besoins seront clairs, Cytoscape.js pourra être ajouté dans `static/vendor/`.

```text
SetupVersionResponse
       ↓
ComponentInstanceResponse[]
ConnectionResponse[]
       ↓
JavaScript
       ↓
Cytoscape.js
       ↓
canvas interactif
```

Cytoscape ne doit être ajouté qu'au moment où l'on commence réellement l'édition visuelle du graphe.

---

## Tests

La stratégie reste identique au reste du projet.

Les tests MVC utiliseront :

```text
@SpringBootTest
@AutoConfigureMockMvc
@Import(PostgresTestConfiguration.class)
```

Ils devront contrôler au minimum :

- accès authentifié aux pages ;
- rendu des templates ;
- données injectées dans les modèles ;
- actions mutantes avec CSRF ;
- comportement read-only des versions `FROZEN` ;
- erreurs métier importantes.

Les tests d'API REST restent séparés des tests de pages MVC.

---

## Migration future vers une SPA

Cette architecture est volontairement transitoire mais pas jetable côté backend.

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

À ce moment-là, seuls `web/controller`, `templates` et éventuellement une partie de `static` pourront être retirés.

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
10. Valider l'UX avant de multiplier les écrans.

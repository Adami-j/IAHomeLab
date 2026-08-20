# Vendor JavaScript

Ce répertoire est réservé aux bibliothèques navigateur utilisées sans NPM.

Règles :

- les fichiers sont servis directement par Spring Boot ;
- aucune étape Node/NPM n'est nécessaire ;
- chaque bibliothèque doit être versionnée explicitement ;
- HTMX sera ajouté ici lorsqu'une première interaction dynamique en aura besoin ;
- Cytoscape.js pourra être ajouté plus tard pour le workspace graphique des setups ;
- ne pas ajouter de bibliothèque sans besoin concret.

Exemples futurs :

```text
static/vendor/
├── htmx-<version>.min.js
└── cytoscape-<version>.min.js
```

# Plan de Travail - Lab 4 : API REST pour Système de Gestion de Tickets

**Projet** : 6GEI311-A2025-lab4
**Date de création** : 2025-11-16
**Objectif** : Transformer l'application standalone en architecture client-serveur REST

---

## 📋 PROGRESSION GLOBALE

- [ ] Phase 1 : Conception API
- [ ] Phase 2 : Génération de code
- [ ] Phase 3 : Implémentation serveur
- [ ] Phase 4 : Intégration client
- [ ] Phase 5 : Tests et validation

---

## 🎯 PHASE 1 : CONCEPTION API REST

**Objectif** : Définir l'API REST complète avec OpenAPI/Swagger

### Tâches

- [ ] **1.1** Créer le fichier `api/openapi/tickets-api.yaml`
- [ ] **1.2** Définir les schémas JSON pour les DTOs
  - [ ] Schéma `TicketDTO`
  - [ ] Schéma `UserDTO`
  - [ ] Schéma `ContentItemDTO`
  - [ ] Schéma `CommentDTO`
  - [ ] Schéma `StatusUpdateDTO`
  - [ ] Schéma `AssignmentDTO`
- [ ] **1.3** Définir les endpoints `/users`
  - [ ] `GET /users` - Liste tous les utilisateurs
  - [ ] `GET /users/{id}` - Détails d'un utilisateur
- [ ] **1.4** Définir les endpoints `/tickets`
  - [ ] `GET /tickets` - Liste des tickets (avec filtres)
  - [ ] `GET /tickets/{id}` - Détails d'un ticket
  - [ ] `POST /tickets` - Créer un ticket
  - [ ] `PUT /tickets/{id}` - Modifier un ticket
  - [ ] `DELETE /tickets/{id}` - Supprimer un ticket
- [ ] **1.5** Définir les endpoints `/tickets/{id}/comments`
  - [ ] `GET /tickets/{id}/comments` - Liste des commentaires
  - [ ] `POST /tickets/{id}/comments` - Ajouter un commentaire
- [ ] **1.6** Définir les endpoints de gestion d'état
  - [ ] `PATCH /tickets/{id}/status` - Changer le statut
  - [ ] `PATCH /tickets/{id}/assignment` - Assigner à un utilisateur
  - [ ] `GET /tickets/{id}/export/pdf` - Exporter en PDF
- [ ] **1.7** Définir les endpoints `/auth`
  - [ ] `POST /auth/login` - Authentification
  - [ ] `GET /auth/session` - Vérifier session
  - [ ] `POST /auth/logout` - Déconnexion
- [ ] **1.8** Documenter les codes d'erreur HTTP
  - [ ] 200 OK
  - [ ] 201 Created
  - [ ] 400 Bad Request (validation échouée)
  - [ ] 401 Unauthorized (non authentifié)
  - [ ] 403 Forbidden (permissions insuffisantes)
  - [ ] 404 Not Found
  - [ ] 500 Internal Server Error
- [ ] **1.9** Valider le fichier OpenAPI avec un validateur en ligne

**Livrables Phase 1** :
- Fichier `api/openapi/tickets-api.yaml` complet et valide

---

## ⚙️ PHASE 2 : GÉNÉRATION DE CODE

**Objectif** : Générer les squelettes client et serveur avec swagger-codegen

### Tâches

- [ ] **2.1** Installer swagger-codegen (si nécessaire)
- [ ] **2.2** Créer les répertoires de destination
  - [ ] `api/server/`
  - [ ] `api/client/`
- [ ] **2.3** Générer le serveur JAX-RS
  ```bash
  swagger-codegen generate -i api/openapi/tickets-api.yaml -l jaxrs-spec -o api/server
  ```
- [ ] **2.4** Générer le client Java
  ```bash
  swagger-codegen generate -i api/openapi/tickets-api.yaml -l java -o api/client
  ```
- [ ] **2.5** Examiner le code généré
  - [ ] Vérifier les classes de ressources serveur
  - [ ] Vérifier les classes du client API
  - [ ] Identifier les fichiers à implémenter (stubs)
- [ ] **2.6** Compiler le code généré (test initial)
  - [ ] Compiler le serveur
  - [ ] Compiler le client
- [ ] **2.7** Corriger les erreurs de compilation si nécessaire

**Livrables Phase 2** :
- Code serveur généré dans `api/server/`
- Code client généré dans `api/client/`
- Code compilable sans erreurs

---

## 🖥️ PHASE 3 : IMPLÉMENTATION SERVEUR

**Objectif** : Implémenter la logique métier côté serveur

### Tâches

- [ ] **3.1** Déplacer ApplicationState côté serveur
  - [ ] Créer `api/server/state/ApplicationState.java`
  - [ ] Migrer la logique de gestion des tickets
  - [ ] Migrer la logique de gestion des utilisateurs
  - [ ] Ajouter synchronisation (thread-safety)
- [ ] **3.2** Implémenter les endpoints `/users`
  - [ ] `GET /users` - Retourner tous les utilisateurs en JSON
  - [ ] `GET /users/{id}` - Retourner un utilisateur par ID
  - [ ] Conversion `User` → `UserDTO` → JSON
- [ ] **3.3** Implémenter les endpoints `/tickets` (CRUD)
  - [ ] `GET /tickets` - Liste avec filtrage par statut/assigné
  - [ ] `GET /tickets/{id}` - Détails complets d'un ticket
  - [ ] `POST /tickets` - Création avec validation
  - [ ] `PUT /tickets/{id}` - Modification avec validation
  - [ ] `DELETE /tickets/{id}` - Suppression (permissions admin)
- [ ] **3.4** Implémenter la sérialisation du pattern Composite
  - [ ] Créer `ContentSerializer.java`
  - [ ] Méthode `serializeContent(Content)` → JSON
  - [ ] Méthode `deserializeContent(JSON)` → Content
  - [ ] Gérer les cas : TextContent, ImageContent, VideoContent, CompositeContent
- [ ] **3.5** Implémenter les endpoints de commentaires
  - [ ] `GET /tickets/{id}/comments` - Liste des commentaires
  - [ ] `POST /tickets/{id}/comments` - Ajout avec validation
- [ ] **3.6** Implémenter les endpoints de gestion d'état
  - [ ] `PATCH /tickets/{id}/status` - Avec validation des transitions
  - [ ] `PATCH /tickets/{id}/assignment` - Avec vérification des permissions
- [ ] **3.7** Implémenter l'export PDF
  - [ ] `GET /tickets/{id}/export/pdf` - Retourner le PDF généré
  - [ ] Utiliser l'Exporter existant
- [ ] **3.8** Implémenter l'authentification
  - [ ] `POST /auth/login` - Validation credentials
  - [ ] Gestion des sessions (cookies ou tokens)
  - [ ] `GET /auth/session` - Vérifier session active
  - [ ] `POST /auth/logout` - Invalider session
- [ ] **3.9** Ajouter la gestion des permissions côté serveur
  - [ ] Intégrer PermissionService
  - [ ] Vérifier les permissions avant chaque opération
  - [ ] Retourner 403 Forbidden si insuffisant
- [ ] **3.10** Implémenter la gestion des erreurs
  - [ ] Mapper IllegalStateException → 400 Bad Request
  - [ ] Mapper NullPointerException → 404 Not Found
  - [ ] Mapper autres exceptions → 500 Internal Server Error
  - [ ] Retourner messages d'erreur clairs en JSON
- [ ] **3.11** Créer la classe principale du serveur
  - [ ] `api/server/TicketAPIServer.java`
  - [ ] Configuration du serveur (port, etc.)
  - [ ] Initialisation d'ApplicationState avec données de test
- [ ] **3.12** Tester la compilation complète du serveur

**Livrables Phase 3** :
- Serveur REST fonctionnel et compilable
- Tous les endpoints implémentés
- Gestion des erreurs robuste

---

## 💻 PHASE 4 : INTÉGRATION CLIENT

**Objectif** : Modifier la GUI pour utiliser le client API au lieu d'ApplicationState local

### Tâches

- [ ] **4.1** Créer une abstraction pour le client API
  - [ ] Interface `ITicketService` (pour faciliter les tests)
  - [ ] Implémentation `RestTicketService` (utilise client généré)
- [ ] **4.2** Refactorer `TicketController`
  - [ ] Remplacer `ApplicationState.getInstance()` par `ITicketService`
  - [ ] Modifier `getAllTickets()` → appel HTTP
  - [ ] Modifier `getTicketById()` → appel HTTP
  - [ ] Modifier `createTicket()` → POST HTTP
  - [ ] Modifier `updateTicket()` → PUT HTTP
  - [ ] Modifier `assignTicket()` → PATCH HTTP
  - [ ] Modifier `changeTicketStatus()` → PATCH HTTP
  - [ ] Modifier `addComment()` → POST HTTP
  - [ ] Modifier `exportTicketToText()` → GET HTTP
- [ ] **4.3** Gérer l'authentification côté client
  - [ ] Modifier `LoginDialog` pour appeler `POST /auth/login`
  - [ ] Stocker le token/session localement
  - [ ] Inclure le token dans toutes les requêtes HTTP
- [ ] **4.4** Gérer les erreurs réseau
  - [ ] Modifier `ErrorHandler` pour gérer les exceptions HTTP
  - [ ] Afficher des messages clairs pour :
    - [ ] Erreur 400 (validation)
    - [ ] Erreur 401 (non authentifié)
    - [ ] Erreur 403 (permissions)
    - [ ] Erreur 404 (ressource introuvable)
    - [ ] Erreur 500 (erreur serveur)
    - [ ] Erreur réseau (serveur inaccessible)
- [ ] **4.5** Tester la conversion JSON → DTO
  - [ ] Vérifier que les DTOs sont correctement désérialisés
  - [ ] Tester la reconstruction des ContentItemDTO
- [ ] **4.6** Supprimer ApplicationState côté client
  - [ ] Retirer l'import de `ApplicationState` dans GUI
  - [ ] Vérifier qu'aucune référence directe ne reste
- [ ] **4.7** Tester l'interface GUI avec le serveur
  - [ ] Login
  - [ ] Affichage de la liste des tickets
  - [ ] Création d'un ticket
  - [ ] Modification d'un ticket
  - [ ] Ajout de commentaires
  - [ ] Changement de statut
  - [ ] Assignation
  - [ ] Export PDF

**Livrables Phase 4** :
- GUI modifiée et fonctionnelle avec le serveur REST
- Gestion des erreurs réseau robuste
- Plus aucune dépendance à ApplicationState local

---

## 🧪 PHASE 5 : TESTS ET VALIDATION

**Objectif** : Valider l'ensemble du système et préparer la démonstration

### Tâches

- [ ] **5.1** Tests des endpoints REST (Postman ou curl)
  - [ ] Tester `GET /users`
  - [ ] Tester `GET /tickets`
  - [ ] Tester `POST /tickets` (création)
  - [ ] Tester `PUT /tickets/{id}` (modification)
  - [ ] Tester `PATCH /tickets/{id}/status` (transitions)
  - [ ] Tester `POST /tickets/{id}/comments`
  - [ ] Tester `GET /tickets/{id}/export/pdf`
  - [ ] Tester les cas d'erreur (400, 404, 403)
- [ ] **5.2** Tests d'intégration GUI ↔ Serveur
  - [ ] Scénario 1 : Utilisateur normal crée un ticket
  - [ ] Scénario 2 : Admin assigne un ticket
  - [ ] Scénario 3 : Changement de statut avec validation
  - [ ] Scénario 4 : Ajout de commentaires
  - [ ] Scénario 5 : Ticket avec contenu composite (texte + image + vidéo)
  - [ ] Scénario 6 : Export PDF d'un ticket
  - [ ] Scénario 7 : Transition de statut invalide (doit échouer)
  - [ ] Scénario 8 : Permission refusée (utilisateur normal essaie d'assigner)
  - [ ] Scénario 9 : Modification d'un ticket par son créateur
- [ ] **5.3** Tests de concurrence (bonus)
  - [ ] Deux clients modifient le même ticket simultanément
  - [ ] Vérifier la cohérence des données
- [ ] **5.4** Tests de robustesse
  - [ ] Serveur éteint → client affiche erreur claire
  - [ ] Requête avec données invalides → 400 Bad Request
  - [ ] Token expiré → 401 Unauthorized
- [ ] **5.5** Validation des 9 scénarios de MainConsole via API
  - [ ] TEST 1 : Ticket avec texte simple
  - [ ] TEST 2 : Ticket avec image
  - [ ] TEST 3 : Ticket avec vidéo
  - [ ] TEST 4 : Ticket avec description composite
  - [ ] TEST 5 : Modification dynamique de description
  - [ ] TEST 6 : Gestion administrative (assignation, commentaires)
  - [ ] TEST 7 : Validation des transitions de statut
  - [ ] TEST 8 : Admin crée un ticket
  - [ ] TEST 9 : Vue d'ensemble de tous les tickets
- [ ] **5.6** Créer un script de démonstration
  - [ ] Script de démarrage du serveur
  - [ ] Script de démarrage du client GUI
  - [ ] Données de test pré-chargées
- [ ] **5.7** Documenter les commandes de build
  - [ ] Compilation serveur
  - [ ] Compilation client
  - [ ] Exécution serveur
  - [ ] Exécution client GUI
- [ ] **5.8** [Bonus] Interface web simple
  - [ ] Créer `web/index.html`
  - [ ] Formulaire de login
  - [ ] Affichage de la liste des tickets
  - [ ] Création d'un ticket
  - [ ] Appels AJAX vers l'API REST

**Livrables Phase 5** :
- Tous les tests passent avec succès
- Documentation de démonstration
- Scripts de build et d'exécution
- [Bonus] Interface web fonctionnelle

---

## 📊 ANALYSE DU DOMAINE EXISTANT

### Entités du domaine (core/entities/)

**User** (core/entities/User.java:5)
- Responsabilités : Créer/consulter/modifier des tickets
- Attributs : userID, name, email, role
- Méthode clé : `createTicket(title, description, priority)`

**Admin extends User** (core/entities/Admin.java:9)
- Responsabilités supplémentaires : Assigner, fermer, consulter tous les tickets
- Pattern : Héritage (Liskov Substitution Principle)

**Ticket** (core/entities/Ticket.java:10)
- Entité centrale du système
- Attributs : ticketID, title, description (Content), status (TicketStatus), priority
- Relations : assignedToUserID, createdByUserID
- Historique : comments (List<String>)
- Méthodes clés :
  - `assignTo(userID)` - Assigne et change statut à ASSIGNE
  - `updateStatus(newStatus)` - Valide les transitions
  - `addComment(comment)` - Ajoute à l'historique
  - `exportToPDF()` - Export via Strategy pattern

**TicketStatus** (core/entities/TicketStatus.java:17)
- Pattern : State Machine via Enum
- États : OUVERT → ASSIGNE → VALIDATION → TERMINE / FERME
- Méthode clé : `canTransitionTo(newStatus)` - Validation des transitions

### Patterns de conception identifiés

**1. COMPOSITE** (core/content/)
```
Content (interface)
├── TextContent (feuille)
├── ImageContent (feuille)
├── VideoContent (feuille)
└── CompositeContent (composite)
```
- Méthodes : `display()`, `accept(Exporter)`

**2. STRATEGY** (core/exporter/)
- Interface : `Exporter`
- Implémentation : `PDFExporter`
- Extensible à : HTMLExporter, JSONExporter, etc.

**3. VISITOR** (interaction Composite + Strategy)
- Méthode : `accept(Exporter)` dans Content
- Double dispatch pour export type-safe

**4. OBSERVER** (gui/controllers/)
- Subject : `ApplicationState`
- Observer : `TicketStateListener`
- Concrete Observer : `TicketManagerGUI`
- Événements : `onTicketsChanged()`, `onCurrentUserChanged()`

**5. SINGLETON** (ApplicationState)
- ⚠️ CRITIQUE : Sera migré côté serveur dans l'architecture REST

**6. MVC** (architecture GUI)
- Model : Entités dans core/ + ApplicationState
- View : TicketManagerGUI, dialogues
- Controller : TicketController

**7. DTO** (Data Transfer Object)
- `TicketDTO` - Représentation aplatie d'un Ticket
- `UserDTO` - Représentation d'un User avec permissions
- `ContentItemDTO` - Représentation d'un élément de contenu
- ⭐ Déjà prêts pour la sérialisation JSON !

### Flux de données actuel (Standalone)

```
TicketManagerGUI (View)
       ↓ utilise
TicketController (Controller)
       ↓ accède à
ApplicationState (Singleton - État global)
       ↓ contient
core/entities (Domain Model)
```

### Transformation REST

**AVANT (standalone)** :
- GUI accède directement à ApplicationState
- Toutes les données en mémoire
- Pas de persistance

**APRÈS (REST)** :
```
Client GUI
  ↓ HTTP
Client API (généré)
  ↓ HTTP/JSON
Serveur REST
  ↓ utilise
ApplicationState (côté serveur)
  ↓ contient
core/entities
```

---

## 🗺️ Mapping complet des endpoints REST

### Ressource : /users
| Méthode | Endpoint | Description | Entrée | Sortie |
|---------|----------|-------------|--------|--------|
| GET | `/users` | Liste tous les utilisateurs | - | `UserDTO[]` |
| GET | `/users/{id}` | Détails d'un utilisateur | id (path) | `UserDTO` |

### Ressource : /tickets
| Méthode | Endpoint | Description | Entrée | Sortie |
|---------|----------|-------------|--------|--------|
| GET | `/tickets` | Liste des tickets | filters (query) | `TicketDTO[]` |
| GET | `/tickets/{id}` | Détails d'un ticket | id (path) | `TicketDTO` |
| POST | `/tickets` | Créer un ticket | `CreateTicketRequest` | `TicketDTO` |
| PUT | `/tickets/{id}` | Modifier un ticket | id + `UpdateTicketRequest` | `TicketDTO` |
| DELETE | `/tickets/{id}` | Supprimer un ticket | id (path) | 204 No Content |

### Ressource : /tickets/{id}/comments
| Méthode | Endpoint | Description | Entrée | Sortie |
|---------|----------|-------------|--------|--------|
| GET | `/tickets/{id}/comments` | Liste des commentaires | id (path) | `CommentDTO[]` |
| POST | `/tickets/{id}/comments` | Ajouter un commentaire | id + `CommentRequest` | `CommentDTO` |

### Ressource : /tickets/{id}/status
| Méthode | Endpoint | Description | Entrée | Sortie |
|---------|----------|-------------|--------|--------|
| PATCH | `/tickets/{id}/status` | Changer le statut | id + `StatusUpdateRequest` | `TicketDTO` |

### Ressource : /tickets/{id}/assignment
| Méthode | Endpoint | Description | Entrée | Sortie |
|---------|----------|-------------|--------|--------|
| PATCH | `/tickets/{id}/assignment` | Assigner à un utilisateur | id + `AssignmentRequest` | `TicketDTO` |

### Ressource : /tickets/{id}/export
| Méthode | Endpoint | Description | Entrée | Sortie |
|---------|----------|-------------|--------|--------|
| GET | `/tickets/{id}/export/pdf` | Exporter en PDF | id (path) | PDF (text/plain) |

### Ressource : /auth
| Méthode | Endpoint | Description | Entrée | Sortie |
|---------|----------|-------------|--------|--------|
| POST | `/auth/login` | Authentification | `LoginRequest` | `AuthResponse` |
| GET | `/auth/session` | Vérifier session | - | `UserDTO` |
| POST | `/auth/logout` | Déconnexion | - | 204 No Content |

---

## ⚠️ Défis identifiés et solutions

### Défi 1 : Sérialisation du pattern Composite
**Problème** : CompositeContent contient List<Content> (polymorphe)

**Solution** :
```json
{
  "type": "composite",
  "items": [
    {"type": "text", "data": "Description..."},
    {"type": "image", "path": "/error.png", "caption": "Erreur"},
    {"type": "video", "path": "/demo.mp4", "duration": 180}
  ]
}
```

### Défi 2 : Gestion de la concurrence
**Problème** : ApplicationState partagé entre plusieurs clients

**Solution** : Synchronisation avec `synchronized` ou collections thread-safe

### Défi 3 : Persistance
**Problème** : Données perdues au redémarrage du serveur

**Solution (bonus)** : Sauvegarder en JSON ou base de données

### Défi 4 : Gestion des erreurs HTTP
**Problème** : Transmettre les exceptions métier au client

**Solution** : Mapper les exceptions vers codes HTTP appropriés
- `IllegalStateException` → 400 Bad Request
- `NullPointerException` → 404 Not Found
- `Exception` → 500 Internal Server Error

---

## 📝 Notes de progression

### Journal de développement
_(Mettre à jour au fur et à mesure de l'avancement)_

**Date** | **Phase** | **Tâche** | **Statut** | **Notes**
---------|-----------|-----------|------------|----------
2025-11-16 | Phase 0 | Analyse du domaine | ✅ Complété | Architecture existante bien structurée
 | | | |
 | | | |

### Difficultés rencontrées
_(Documenter les problèmes et leurs solutions)_

**Problème** | **Solution** | **Leçon apprise**
-------------|--------------|------------------
 | | |
 | | |

---

## 📚 Ressources et références

### Documentation technique
- [OpenAPI Specification](https://swagger.io/specification/)
- [Swagger Codegen](https://github.com/swagger-api/swagger-codegen)
- [JAX-RS Tutorial](https://docs.oracle.com/javaee/7/tutorial/jaxrs.htm)
- [Principes SOLID](https://en.wikipedia.org/wiki/SOLID)

### Fichiers clés du projet
- `core/entities/Ticket.java:10` - Entité centrale
- `core/entities/TicketStatus.java:17` - Machine à états
- `gui/controllers/TicketController.java:19` - Controller actuel
- `gui/controllers/ApplicationState.java:13` - Singleton (à migrer)
- `core/content/Content.java:10` - Pattern Composite
- `core/exporter/Exporter.java:9` - Pattern Strategy

### Commandes utiles

**Compilation actuelle (standalone)** :
```bash
# Console
javac -encoding UTF-8 -d classes MainConsole.java core/content/*.java core/exporter/*.java core/entities/*.java

# GUI
javac -encoding UTF-8 -d classes MainGUI.java gui/**/*.java core/**/*.java
```

**Génération avec Swagger Codegen** :
```bash
# Serveur JAX-RS
swagger-codegen generate -i api/openapi/tickets-api.yaml -l jaxrs-spec -o api/server

# Client Java
swagger-codegen generate -i api/openapi/tickets-api.yaml -l java -o api/client
```

**Compilation serveur** :
```bash
javac -encoding UTF-8 -d classes api/server/**/*.java core/**/*.java
```

**Compilation client + GUI** :
```bash
javac -encoding UTF-8 -d classes api/client/**/*.java gui/**/*.java MainGUI.java
```

**Exécution** :
```bash
# Serveur
java -cp classes api.server.TicketAPIServer

# Client GUI
java -cp classes MainGUI
```

---

## ✅ Critères de succès

### Livrables finaux attendus
- [ ] Code source complet (serveur + client + intégration)
- [ ] Fichier OpenAPI/Swagger YAML
- [ ] Rapport Section I : Modifications apportées à core/ et gui/
- [ ] Rapport Section II : Leçons apprises
- [ ] [Bonus] Interface web simple
- [ ] Démonstration fonctionnelle

### Validation technique
- [ ] Le serveur démarre sans erreurs
- [ ] Le client GUI se connecte au serveur
- [ ] Tous les endpoints répondent correctement
- [ ] Les 9 scénarios de MainConsole fonctionnent via l'API
- [ ] La gestion des erreurs est robuste
- [ ] Les permissions sont respectées
- [ ] Les transitions de statut sont validées

### Validation pédagogique
- [ ] Compréhension de l'architecture REST
- [ ] Maîtrise de OpenAPI/Swagger
- [ ] Application des principes SOLID
- [ ] Séparation des préoccupations (client/serveur)
- [ ] Gestion des erreurs HTTP appropriée

---

**Fin du plan de travail**

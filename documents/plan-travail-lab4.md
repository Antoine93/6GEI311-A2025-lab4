# Plan de Travail - Lab 4 : API REST pour Système de Gestion de Tickets

**Projet** : 6GEI311-A2025-lab4
**Date de création** : 2025-11-16
**Objectif** : Transformer l'application standalone en architecture client-serveur REST

---

## 📋 PROGRESSION GLOBALE

- [x] Phase 1 : Conception API
- [x] Phase 2 : Génération de code
- [x] Phase 3 : Implémentation serveur
- [x] Phase 4 : Intégration client ✅ **COMPLÉTÉE**
- [x] Phase 5 : Tests et validation ✅ **COMPLÉTÉE**

---

## 🎯 PHASE 1 : CONCEPTION API REST

**Objectif** : Définir l'API REST complète avec OpenAPI/Swagger

### Tâches

- [x] **1.1** Créer le fichier `api/openapi/tickets-api.yaml`
- [x] **1.2** Définir les schémas JSON pour les DTOs
  - [x] Schéma `TicketDTO`
  - [x] Schéma `UserDTO`
  - [x] Schéma `ContentItemDTO`
  - [x] Schéma `CommentDTO`
  - [x] Schéma `StatusUpdateDTO`
  - [x] Schéma `AssignmentDTO`
- [x] **1.3** Définir les endpoints `/users`
  - [x] `GET /users` - Liste tous les utilisateurs
  - [x] `GET /users/{id}` - Détails d'un utilisateur
- [x] **1.4** Définir les endpoints `/tickets`
  - [x] `GET /tickets` - Liste des tickets (avec filtres)
  - [x] `GET /tickets/{id}` - Détails d'un ticket
  - [x] `POST /tickets` - Créer un ticket
  - [x] `PUT /tickets/{id}` - Modifier un ticket
  - [x] `DELETE /tickets/{id}` - Supprimer un ticket
- [x] **1.5** Définir les endpoints `/tickets/{id}/comments`
  - [x] `GET /tickets/{id}/comments` - Liste des commentaires
  - [x] `POST /tickets/{id}/comments` - Ajouter un commentaire
- [x] **1.6** Définir les endpoints de gestion d'état
  - [x] `PATCH /tickets/{id}/status` - Changer le statut
  - [x] `PATCH /tickets/{id}/assignment` - Assigner à un utilisateur
  - [x] `GET /tickets/{id}/export/pdf` - Exporter en PDF
- [x] **1.7** Définir les endpoints `/auth`
  - [x] `POST /auth/login` - Authentification
  - [x] `GET /auth/session` - Vérifier session
  - [x] `POST /auth/logout` - Déconnexion
- [x] **1.8** Documenter les codes d'erreur HTTP
  - [x] 200 OK
  - [x] 201 Created
  - [x] 400 Bad Request (validation échouée)
  - [x] 401 Unauthorized (non authentifié)
  - [x] 403 Forbidden (permissions insuffisantes)
  - [x] 404 Not Found
  - [x] 500 Internal Server Error
- [x] **1.9** Valider le fichier OpenAPI avec un validateur en ligne

**Livrables Phase 1** :
- Fichier `api/openapi/tickets-api.yaml` complet et valide

---

## ⚙️ PHASE 2 : GÉNÉRATION DE CODE

**Objectif** : Créer le code serveur et client pour l'API REST

### Tâches

- [x] **2.1** Vérifier les outils disponibles (Java 25 disponible)
- [x] **2.2** Créer les répertoires de destination
  - [x] `api/server/models/`
  - [x] `api/server/resources/`
  - [x] `api/server/services/`
  - [x] `api/client/`
- [x] **2.3** Créer les modèles serveur (DTOs)
  - [x] UserDTO.java
  - [x] TicketDTO.java
  - [x] ContentItemDTO.java
  - [x] CreateTicketRequest.java
  - [x] UpdateTicketRequest.java
  - [x] CommentRequest.java
  - [x] StatusUpdateDTO.java
  - [x] AssignmentDTO.java
  - [x] LoginRequest.java
  - [x] AuthResponse.java
  - [x] ErrorResponse.java
- [x] **2.4** Créer les ressources HTTP (handlers)
  - [x] BaseResource.java (classe de base)
  - [x] AuthResource.java (authentification)
  - [x] UserResource.java (utilisateurs)
  - [x] TicketResource.java (tickets complets)
- [x] **2.5** Créer ApplicationState côté serveur
  - [x] Singleton thread-safe avec ConcurrentHashMap
  - [x] Gestion des sessions
  - [x] Conversion Entity ↔ DTO
  - [x] Méthodes métier (CRUD, commentaires, statuts, etc.)
- [x] **2.6** Créer le serveur HTTP principal
  - [x] TicketAPIServer.java
  - [x] Configuration HttpServer (port 8080)
  - [x] Enregistrement des handlers
- [x] **2.7** Télécharger Gson et compiler le serveur
  - [x] Télécharger gson-2.10.1.jar
  - [x] Compiler le code serveur
  - [x] Tester le démarrage du serveur
- [x] **2.8** Ajouter page d'accueil API
  - [x] Créer ApiHomeResource.java
  - [x] Enregistrer route GET /api/v1
  - [x] Mettre à jour la documentation README

**Livrables Phase 2** :
- ✅ Code serveur complet dans `api/server/`
- ✅ Serveur HTTP fonctionnel avec tous les endpoints
- ✅ Documentation README pour compilation/exécution
- ✅ Page d'accueil API (GET /api/v1)

---

## 🖥️ PHASE 3 : IMPLÉMENTATION SERVEUR ✅ **COMPLÉTÉE**

**Objectif** : Implémenter la logique métier côté serveur

### Tâches

- [x] **3.1** ApplicationState côté serveur (`api/server/services/ApplicationState.java`)
  - [x] Singleton thread-safe avec synchronisation
  - [x] Gestion des sessions (Map<token, User>)
  - [x] Conversion bidirectionnelle Entity ↔ DTO
  - [x] Méthodes métier complètes (CRUD, commentaires, statuts, assignation)
- [x] **3.2** Endpoints `/users` avec authentification
  - [x] `GET /users` - Liste avec auth requise
  - [x] `GET /users/{id}` - Détails avec auth requise
  - [x] Logging des accès avec nom d'utilisateur
- [x] **3.3** Endpoints `/tickets` (CRUD sécurisés)
  - [x] `GET /tickets` - Filtrage automatique selon permissions
  - [x] `GET /tickets/{id}` - Vérification d'accès (créateur ou Admin/Dev)
  - [x] `POST /tickets` - Création avec utilisateur authentifié
  - [x] `PUT /tickets/{id}` - Modification avec vérification permissions
  - [x] `DELETE /tickets/{id}` - Admin seulement
- [x] **3.4** Sérialisation du pattern Composite (intégré à ApplicationState)
  - [x] `convertContentToDTO(Content)` → List<ContentItemDTO>
  - [x] `convertDTOToContent(List<ContentItemDTO>)` → Content
  - [x] Gestion complète : TextContent, ImageContent, VideoContent, CompositeContent
  - [x] Support contenu composite (plusieurs items)
- [x] **3.5** Endpoints de commentaires avec authentification
  - [x] `GET /tickets/{id}/comments` - Auth requise
  - [x] `POST /tickets/{id}/comments` - Auth requise + validation + logging
- [x] **3.6** Endpoints de gestion d'état (Admin/Dev seulement)
  - [x] `GET /tickets/{id}/status` - Transitions disponibles
  - [x] `PATCH /tickets/{id}/status` - Avec validation State Machine
  - [x] `PATCH /tickets/{id}/assignment` - Avec vérification permissions
- [x] **3.7** Export PDF avec authentification
  - [x] `GET /tickets/{id}/export/pdf` - Utilise PDFExporter
  - [x] Auth requise
- [x] **3.8** Authentification complète (AuthResource)
  - [x] `POST /auth/login` - Génération token UUID
  - [x] Gestion sessions (ConcurrentHashMap<token, User>)
  - [x] `GET /auth/session` - Validation token Bearer
  - [x] `POST /auth/logout` - Invalidation session
- [x] **3.9** Gestion des permissions côté serveur (BaseResource)
  - [x] `requireAuth()` - Vérification token (401 si absent)
  - [x] `requireAdmin()` - Vérification Admin (403 si non-admin)
  - [x] `hasFullAccess()` - Admin ou Développeur
  - [x] `canEditTicket()` - Créateur ou Admin/Dev
  - [x] Filtrage tickets selon permissions
- [x] **3.10** Gestion complète des erreurs
  - [x] IllegalStateException → 400 Bad Request (transitions invalides)
  - [x] Token invalide/absent → 401 Unauthorized
  - [x] Permissions insuffisantes → 403 Forbidden
  - [x] Ressource introuvable → 404 Not Found
  - [x] Exceptions générales → 500 Internal Server Error
  - [x] ErrorResponse JSON avec error + message
- [x] **3.11** Serveur HTTP complet (TicketAPIServer.java)
  - [x] Configuration port 8080
  - [x] Initialisation données de test (3 users, 3 tickets)
  - [x] Enregistrement de tous les handlers
  - [x] Logging complet avec noms d'utilisateurs
- [x] **3.12** Compilation et démarrage réussis
  - [x] Compilation sans erreurs avec Gson
  - [x] Serveur démarré avec succès
  - [x] 16 endpoints fonctionnels

**Livrables Phase 3** : ✅ **TOUS COMPLÉTÉS**
- ✅ Serveur REST fonctionnel et sécurisé
- ✅ Tous les endpoints implémentés avec authentification
- ✅ Gestion des erreurs robuste (401, 403, 404, 400, 500)
- ✅ Permissions validées côté serveur
- ✅ State Machine des statuts fonctionnelle
- ✅ Pattern Composite sérialisé correctement
- ✅ Documentation de tests (documents/TESTS_API.md)

---

## 💻 PHASE 4 : INTÉGRATION CLIENT ✅ **COMPLÉTÉE**

**Objectif** : Modifier la GUI pour utiliser le client API au lieu d'ApplicationState local

### Tâches

- [x] **4.1** Créer une abstraction pour le client API
  - [x] Interface `ITicketService` (pour faciliter les tests)
  - [x] Exception `ServiceException` (gestion erreurs HTTP)
  - [x] Implémentation `RestTicketService` (appels HTTP/JSON)
  - [x] Client HTTP `SimpleHttpClient` (java.net.HttpURLConnection)
- [x] **4.2** Refactorer `TicketController`
  - [x] Créer `TicketControllerREST` (délègue à ITicketService)
  - [x] `getAllTickets()` → GET /tickets
  - [x] `getTicketById()` → GET /tickets/{id}
  - [x] `createTicket()` → POST /tickets
  - [x] `updateTicket()` → PUT /tickets/{id}
  - [x] `assignTicket()` → PATCH /tickets/{id}/assignment
  - [x] `changeTicketStatus()` → PATCH /tickets/{id}/status
  - [x] `addComment()` → POST /tickets/{id}/comments
  - [x] `exportTicketToText()` → GET /tickets/{id}/export/pdf
- [x] **4.3** Gérer l'authentification côté client
  - [x] Login via dialogue simplifié (ID utilisateur)
  - [x] `POST /auth/login` → retourne token + UserDTO
  - [x] Token stocké dans `RestTicketService.authToken`
  - [x] Token inclus dans header `Authorization: Bearer <token>`
- [x] **4.4** Gérer les erreurs réseau
  - [x] `ServiceException` avec `httpStatusCode` et `errorCode`
  - [x] Méthodes utilitaires : `isAuthenticationError()`, `isPermissionError()`, etc.
  - [x] Messages clairs pour :
    - [x] Erreur 400 (validation) → `isValidationError()`
    - [x] Erreur 401 (non authentifié) → `isAuthenticationError()`
    - [x] Erreur 403 (permissions) → `isPermissionError()`
    - [x] Erreur 404 (ressource introuvable) → `isNotFoundError()`
    - [x] Erreur 500 (erreur serveur) → `isServerError()`
    - [x] Erreur réseau (serveur inaccessible) → `IOException`
- [x] **4.5** Tester la conversion JSON → DTO
  - [x] Gson gère la sérialisation/désérialisation automatiquement
  - [x] Conversion `api.server.models.TicketDTO` ↔ `gui.models.TicketDTO`
  - [x] Conversion `gui.models.ContentItemDTO` ↔ `api.server.models.ContentItemDTO`
- [x] **4.6** Supprimer ApplicationState côté client
  - [x] `MainGUI_REST` n'importe pas `ApplicationState`
  - [x] Tout passe par `ITicketService`
- [x] **4.7** Tester l'interface GUI avec le serveur
  - [x] Login (ID 1, 2, 100) ✅
  - [x] Affichage de la liste des tickets ✅
  - [x] Création d'un ticket ✅
  - [x] Ajout de commentaires ✅
  - [x] Changement de statut (avec validation transitions) ✅
  - [x] Assignation ✅
  - [x] Export PDF ✅
  - [x] Gestion d'erreur (transition invalide) ✅
  - [x] Gestion serveur non démarré ✅

**Livrables Phase 4** : ✅ **TOUS COMPLÉTÉS**
- ✅ GUI REST fonctionnelle (`MainGUI_REST.java`)
- ✅ Gestion des erreurs réseau robuste (`ServiceException`, `SimpleHttpClient`)
- ✅ Plus aucune dépendance à ApplicationState local dans le client REST
- ✅ Documentation complète (documents/PHASE4_INTEGRATION_CLIENT.md)

**Nouveaux fichiers créés** :
- `gui/services/ITicketService.java` - Interface de service
- `gui/services/ServiceException.java` - Exception personnalisée
- `gui/services/SimpleHttpClient.java` - Client HTTP léger
- `gui/services/RestTicketService.java` - Implémentation REST (510 lignes)
- `gui/controllers/TicketControllerREST.java` - Controller REST
- `MainGUI_REST.java` - Interface graphique REST complète (340 lignes)
- `documents/PHASE4_INTEGRATION_CLIENT.md` - Documentation détaillée

**Compilation et exécution** :
```bash
# Compilation
javac -encoding UTF-8 -cp "api/server/lib/*;classes" -d classes \
  core/**/*.java api/server/**/*.java gui/**/*.java MainGUI_REST.java

# Démarrage serveur (Terminal 1)
java -cp "classes;api/server/lib/*" api.server.TicketAPIServer

# Démarrage client GUI (Terminal 2)
java -cp "classes;api/server/lib/*" MainGUI_REST
```

---

## 🧪 PHASE 5 : TESTS ET VALIDATION ✅ **COMPLÉTÉE**

**Objectif** : Valider l'ensemble du système et préparer la démonstration

### Tâches

- [x] **5.1** Tests des endpoints REST (Postman ou curl)
  - [x] Tester `GET /users`
  - [x] Tester `GET /tickets`
  - [x] Tester `POST /tickets` (création)
  - [x] Tester `PUT /tickets/{id}` (modification)
  - [x] Tester `PATCH /tickets/{id}/status` (transitions)
  - [x] Tester `POST /tickets/{id}/comments`
  - [x] Tester `GET /tickets/{id}/export/pdf`
  - [x] Tester les cas d'erreur (400, 404, 403)
- [x] **5.2** Tests d'intégration GUI ↔ Serveur (validé en Phase 4)
  - [x] Scénario 1 : Utilisateur normal crée un ticket
  - [x] Scénario 2 : Admin assigne un ticket
  - [x] Scénario 3 : Changement de statut avec validation
  - [x] Scénario 4 : Ajout de commentaires
  - [x] Scénario 5 : Ticket avec contenu composite (texte + image + vidéo)
  - [x] Scénario 6 : Export PDF d'un ticket
  - [x] Scénario 7 : Transition de statut invalide (doit échouer)
  - [x] Scénario 8 : Permission refusée (utilisateur normal essaie d'assigner)
  - [x] Scénario 9 : Modification d'un ticket par son créateur
- [ ] **5.3** Tests de concurrence (bonus - non requis)
  - [ ] Deux clients modifient le même ticket simultanément
  - [ ] Vérifier la cohérence des données
- [x] **5.4** Tests de robustesse
  - [x] Serveur éteint → client affiche erreur claire
  - [x] Requête avec données invalides → 400 Bad Request
  - [x] Token expiré → 401 Unauthorized
- [x] **5.5** Validation des 9 scénarios de MainConsole via API
  - [x] TEST 1 : Ticket avec texte simple
  - [x] TEST 2 : Ticket avec image
  - [x] TEST 3 : Ticket avec vidéo
  - [x] TEST 4 : Ticket avec description composite
  - [x] TEST 5 : Modification dynamique de description
  - [x] TEST 6 : Gestion administrative (assignation, commentaires)
  - [x] TEST 7 : Validation des transitions de statut
  - [x] TEST 8 : Admin crée un ticket
  - [x] TEST 9 : Vue d'ensemble de tous les tickets
- [x] **5.6** Créer un script de démonstration
  - [x] Script de démarrage du serveur (documentation dans PHASE5_TESTS_VALIDATION.md)
  - [x] Script de démarrage du client GUI (documentation dans PHASE5_TESTS_VALIDATION.md)
  - [x] Données de test pré-chargées (3 users, 3 tickets initiaux)
- [x] **5.7** Documenter les commandes de build
  - [x] Compilation serveur
  - [x] Compilation client
  - [x] Exécution serveur
  - [x] Exécution client GUI
- [ ] **5.8** [Bonus] Interface web simple (non requis)
  - [ ] Créer `web/index.html`
  - [ ] Formulaire de login
  - [ ] Affichage de la liste des tickets
  - [ ] Création d'un ticket
  - [ ] Appels AJAX vers l'API REST

**Livrables Phase 5** : ✅ **TOUS COMPLÉTÉS**
- ✅ Tous les tests passent avec succès (16/16 endpoints)
- ✅ Documentation de tests complète (documents/PHASE5_TESTS_VALIDATION.md)
- ✅ Scripts de build et d'exécution documentés
- ✅ 9/9 scénarios de MainConsole validés via API
- ✅ Gestion d'erreurs robuste (400, 401, 403, 404, 500)
- ✅ Permissions testées (Admin, Développeur, Testeur)
- ✅ Pattern Composite validé (sérialisation JSON)
- ✅ Export PDF fonctionnel (Pattern Strategy + Visitor)

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

### Ressource : / (racine API)
| Méthode | Endpoint | Description | Entrée | Sortie |
|---------|----------|-------------|--------|--------|
| GET | `/api/v1` | Page d'accueil de l'API | - | Infos API (nom, version, status) |

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
2025-11-16 | Phase 1 | Conception API OpenAPI | ✅ Complété | Fichier tickets-api.yaml validé
2025-11-16 | Phase 2 | Génération code serveur | ✅ Complété | Serveur HTTP avec Gson fonctionnel
2025-11-16 | Phase 2 | Ajout page d'accueil API | ✅ Complété | GET /api/v1 retourne infos de l'API
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

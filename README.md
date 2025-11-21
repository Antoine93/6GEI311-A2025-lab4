# Laboratoire 4 - Architecture REST API
## 6GEI311 - Architecture des logiciels

**Équipe:** Antoine Larouche Tremblay
**Date de remise:** 20 Novembre 2025

---

# Instructions d'exécution

## Prérequis

- Java JDK 8+
- Bibliothèque Gson: `lib/gson-2.10.1.jar`

## Compilation

```powershell
# Créer le dossier classes
New-Item -ItemType Directory -Force -Path classes

# Compiler le serveur
javac -encoding UTF-8 -cp "lib/gson-2.10.1.jar" -d classes `
  api\server\*.java `
  api\server\models\*.java `
  api\server\resources\*.java `
  api\server\services\*.java `
  core\entities\*.java `
  core\content\*.java `
  core\exporter\*.java

# Compiler le client GUI
javac -encoding UTF-8 -cp "lib/gson-2.10.1.jar;classes" -d classes `
  gui\services\*.java `
  gui\controllers\*.java `
  gui\models\*.java `
  gui\views\*.java `
  gui\views\dialogs\*.java `
  gui\views\components\*.java `
  gui\validators\*.java `
  gui\utils\*.java `
  MainGUI.java
```

## Exécution

```powershell
# Terminal 1: Démarrer le serveur
java -cp "lib/gson-2.10.1.jar;classes" api.server.TicketAPIServer

# Terminal 2: Démarrer le client GUI
java -cp "lib/gson-2.10.1.jar;classes" MainGUI
```

## Interface web (bonus)

Ouvrir `web/index.html` dans un navigateur (serveur doit être démarré).

## Documentation API

- Swagger UI: http://localhost:8080/docs
- OpenAPI YAML: http://localhost:8080/openapi.yaml

---

## Table des matières

[Instructions d'exécution](#instructions-dexécution)
1. [Section I - Modifications apportées](#section-i---modifications-apportées)
2. [Section II - Leçons apprises](#section-ii---leçons-apprises)

---

# Section I - Modifications apportées

## 1. Vue d'ensemble architecturale

### Architecture AVANT (Lab 2-3) - Standalone

```
┌────────────────────────────────────────────┐
│               Client (GUI)                 │
│                                            │
│  ┌──────────────┐    ┌──────────────────┐  │
│  │TicketManager │───→│TicketController  │  │
│  │    GUI       │    └────────┬─────────┘  │
│  └──────────────┘             │            │
│                               ↓            │
│                    ┌──────────────────┐    │
│                    │ApplicationState  │    │
│                    │  (Singleton)     │    │
│                    └────────┬─────────┘    │
│                             ↓              │
│                    ┌──────────────────┐    │
│                    │  Core Entities   │    │
│                    │ (User, Ticket)   │    │
│                    └──────────────────┘    │
└────────────────────────────────────────────┘
```

### Architecture APRÈS (Lab 4) - Client-Serveur REST

```
┌────────────────────────────┐         ┌────────────────────────────┐
│      Client (GUI)          │         │      Serveur REST          │
│                            │         │                            │
│  ┌──────────────┐          │         │  ┌──────────────────┐      │
│  │TicketManager │          │         │  │  HTTP Server     │      │
│  │ GUI (View)   │          │         │  │  (port 8080)     │      │
│  └──────┬───────┘          │         │  └────────┬─────────┘      │
│         │                  │         │           │                │
│         ↓                  │         │           ↓                │
│  ┌──────────────┐          │   HTTP  │  ┌──────────────────┐      │
│  │TicketControl │          │  REST   │  │  Resources       │      │
│  │     ler      │          │  (JSON) │  │ (TicketResource, │      │
│  └──────┬───────┘          │ ◄─────► │  │  AuthResource)   │      │
│         │                  │         │  └────────┬─────────┘      │
│         ↓                  │         │           │                │
│  ┌──────────────┐          │         │           ↓                │
│  │RestApiClient │          │         │  ┌──────────────────┐      │
│  │ (Singleton)  │          │         │  │ApplicationState  │      │
|  |  (Service)   |          |         |  |   (Serveur)      |      |
│  └──────────────┘          │         |  └────────┬─────────┘      |
│                            │         │           │                |
│                            │         │           │                │
│                            │         │           ↓                │
│                            │         │  ┌──────────────────┐      │
│                            │         │  │  Core Entities   │      │
│                            │         │  │ (User, Ticket)   │      │
│                            │         │  └──────────────────┘      │
└────────────────────────────┘         └────────────────────────────┘
```

**Avantages de la nouvelle architecture:**
- Multi-utilisateurs simultanés (état centralisé serveur)
- Multi-clients possibles (Swing GUI + Interface Web)
- Communication standardisée HTTP/JSON
- Documentation via OpenAPI/Swagger

---

## 2. Modifications côté serveur

### Nouveaux fichiers créés

| Fichier | Rôle |
|---------|------|
| `api/server/TicketAPIServer.java` | Serveur HTTP sur port 8080, enregistrement des handlers |
| `api/server/services/ApplicationState.java` | État centralisé: sessions, tickets, utilisateurs |
| `api/server/resources/BaseResource.java` | Classe abstraite avec utilitaires (JSON, auth, erreurs) |
| `api/server/resources/AuthResource.java` | Endpoints `/auth/login`, `/auth/session`, `/auth/logout` |
| `api/server/resources/UserResource.java` | Endpoints `/users`, `/users/{id}` |
| `api/server/resources/TicketResource.java` | Endpoints CRUD tickets, commentaires, statuts, assignation, export |
| `api/server/resources/SwaggerUIResource.java` | Interface Swagger UI sur `/docs` |
| `api/server/resources/OpenApiResource.java` | Fichier YAML sur `/openapi.yaml` |

### DTOs serveur (`api/server/models/`)

| DTO | Description |
|-----|-------------|
| `UserDTO.java` | Représentation JSON d'un utilisateur |
| `TicketDTO.java` | Représentation JSON d'un ticket |
| `ContentItemDTO.java` | Élément de contenu (type: TEXT/IMAGE/VIDEO) |
| `CreateTicketRequest.java` | Corps de requête POST /tickets |
| `UpdateTicketRequest.java` | Corps de requête PUT /tickets/{id} |
| `CommentRequest.java` | Corps de requête POST /tickets/{id}/comments |
| `StatusUpdateDTO.java` | Corps de requête POST /tickets/{id}/status |
| `AssignmentDTO.java` | Corps de requête POST /tickets/{id}/assignment |
| `LoginRequest.java` | Corps de requête POST /auth/login |
| `AuthResponse.java` | Réponse avec token et utilisateur |
| `ErrorResponse.java` | Format d'erreur standardisé |

### Documentation API (`api/openapi/tickets-api.yaml`)

Spécification OpenAPI 3.0.3 complète avec:
- 14 endpoints documentés
- Schémas de données (UserDTO, TicketDTO, ContentItemDTO, etc.)
- Codes d'erreur standardisés (400, 401, 403, 404, 500)
- Authentification Bearer token
- Exemples de requêtes/réponses

---

## 3. Modifications côté client GUI

### Nouveau fichier: `gui/services/RestApiClient.java`

**Rôle:** Client HTTP encapsulant tous les appels vers l'API REST.

**Caractéristiques:**
- Pattern Singleton (une instance partagée)
- Sérialisation JSON avec Gson
- Gestion du token Bearer pour authentification
- Gestion des erreurs HTTP

**Méthodes principales:**
```java
// Authentification
UserDTO login(int userID)
void logout()

// Utilisateurs
List<UserDTO> getAllUsers()

// Tickets CRUD
List<TicketDTO> getAllTickets()
TicketDTO getTicketById(int id)
TicketDTO createTicket(String title, String priority, List<ContentItemDTO> content)
TicketDTO updateTicket(int id, String title, String priority, List<ContentItemDTO> content)
void deleteTicket(int id)

// Commentaires, Statuts, Assignation
List<String> getTicketComments(int ticketID)
String addComment(int ticketID, String text)
TicketDTO changeTicketStatus(int ticketID, String newStatus)
TicketDTO assignTicket(int ticketID, int userID)
String exportTicketToPDF(int ticketID)
```

### Modifié: `gui/controllers/TicketController.java`

**AVANT (Lab 2-3):**
```java
private ApplicationState state;

public List<TicketDTO> getAllTickets() {
    return convertToTicketDTOs(state.getAllTickets());
}
```

**APRÈS (Lab 4):**
```java
private RestApiClient apiClient;

public List<TicketDTO> getAllTickets() {
    try {
        return apiClient.getAllTickets();
    } catch (IOException e) {
        System.err.println("Erreur réseau: " + e.getMessage());
        return new ArrayList<>();
    }
}
```

**Changements clés:**
- Remplacement de `ApplicationState` par `RestApiClient`
- Ajout de gestion des erreurs réseau (`IOException`)
- Ajout des méthodes `login()` et `logout()`
- Conversion des statuts d'affichage vers format API

### Déprécié: `gui/controllers/ApplicationState.java`

- Marqué `@Deprecated`
- Conservé uniquement pour compatibilité de compilation
- L'état est maintenant géré côté serveur
- Toutes les méthodes affichent des avertissements

### Autres modifications mineures

| Fichier | Modification |
|---------|--------------|
| `gui/views/LoginDialog.java` | Appel `TicketController.login()` au lieu de `ApplicationState.setCurrentUser()` |
| `gui/views/EditTicketDialog.java` | Conversion statuts affichage ↔ API |
| `gui/views/TicketManagerGUI.java` | Suppression référence à ApplicationState |

---

## 4. Partie métier (core/) - Aucune modification

Le package `core/` n'a subi **aucune modification** pour l'intégration REST.

**Fichiers préservés:**
- `core/entities/User.java`, `Admin.java`, `Ticket.java`, `TicketStatus.java`
- `core/content/Content.java`, `TextContent.java`, `ImageContent.java`, `VideoContent.java`, `CompositeContent.java`
- `core/exporter/Exporter.java`, `PDFExporter.java`

**Principe architectural:** La logique métier reste indépendante de la couche de présentation (REST ou GUI). Le serveur REST agit comme une couche de transformation Domain ↔ JSON.

---

## 5. Interface web bonus

### Fichiers créés (`web/`)

| Fichier | Rôle |
|---------|------|
| `index.html` | SPA avec pages login et principale |
| `style.css` | Styles CSS responsives |
| `api.js` | Client REST JavaScript (classe `ApiClient`) |
| `app.js` | Logique applicative (événements, rendu) |

### Fonctionnalités

- Connexion utilisateur (IDs: 1=Développeur, 2=Testeur, 100=Admin)
- Liste des tickets avec filtrage par statut
- Détails ticket avec actions (commentaire, statut, assigner, export PDF)
- Création/modification de tickets
- Suppression (admin uniquement)

---

# Section II - Leçons apprises

## 1. Avantages de l'architecture REST

### Séparation client/serveur
La transformation en architecture REST permet une **séparation claire des responsabilités**. Le client (GUI Swing ou Web) ne connaît plus la logique métier, il communique uniquement via HTTP/JSON. Cette séparation a permis de créer deux clients différents (Swing et Web) partageant la même API.

### État centralisé multi-utilisateurs
Contrairement à l'architecture standalone où chaque instance avait son propre état en mémoire, l'état est maintenant **centralisé côté serveur**. Plusieurs utilisateurs peuvent travailler simultanément sur les mêmes tickets, avec une vue cohérente des données.

### Communication standardisée
L'utilisation de HTTP et JSON offre une **interopérabilité maximale**. N'importe quel client capable de faire des requêtes HTTP peut utiliser l'API (navigateur, application mobile, scripts, outils comme Postman/curl).

### Documentation automatique
La spécification OpenAPI/Swagger fournit une **documentation vivante** de l'API. L'interface Swagger UI (`/docs`) permet de tester les endpoints directement depuis le navigateur.

---

## 2. Défis rencontrés

### Gestion des sessions
**Défi:** Implémenter un système d'authentification simple mais fonctionnel.

**Solution:** Token de session (UUID) stocké côté serveur dans une `Map<String, User>`. Le client envoie le token via l'en-tête `Authorization: Bearer <token>`.

**Limitation:** Les sessions sont perdues au redémarrage du serveur (stockage en mémoire).

### Sérialisation du pattern Composite
**Défi:** Représenter en JSON les différents types de contenu (TextContent, ImageContent, VideoContent) du pattern Composite.

**Solution:** Création de `ContentItemDTO` avec un champ `type` discriminant:
```json
{"type": "TEXT", "data": "...", "metadata": null}
{"type": "IMAGE", "data": "path/to/image.png", "metadata": "légende"}
{"type": "VIDEO", "data": "path/to/video.mp4", "metadata": "120"}
```

### Conversion des statuts
**Défi:** Les statuts sont affichés différemment dans la GUI ("En validation") et dans l'API ("VALIDATION").

**Solution:** Méthodes de conversion dans `TicketController`:
```java
private String displayStatusToApiStatus(String displayStatus)
private String apiStatusToDisplayStatus(String apiStatus)
```

### CORS pour l'interface web
**Défi:** Le navigateur bloque les requêtes cross-origin vers `localhost:8080`.

**Solution:** Ajout des en-têtes CORS dans `BaseResource`:
```java
headers.set("Access-Control-Allow-Origin", "*");
headers.set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
headers.set("Access-Control-Allow-Headers", "Content-Type, Authorization");
```

### Gestion des erreurs réseau
**Défi:** Le client doit gérer gracieusement les erreurs (serveur non démarré, timeout, etc.).

**Solution:** Encapsulation des appels dans try/catch avec messages utilisateur appropriés.

---

## 3. Bonnes pratiques appliquées

### DTOs pour découplage
Les DTOs (Data Transfer Objects) assurent un **découplage complet** entre les entités métier (`core.entities.*`) et la représentation JSON. Cela permet de modifier l'un sans affecter l'autre.

### Codes HTTP standardisés
| Code | Utilisation |
|------|-------------|
| 200 OK | Succès avec contenu |
| 201 Created | Ressource créée |
| 204 No Content | Succès sans contenu (DELETE) |
| 400 Bad Request | Validation échouée |
| 401 Unauthorized | Non authentifié |
| 403 Forbidden | Permissions insuffisantes |
| 404 Not Found | Ressource introuvable |
| 500 Internal Error | Erreur serveur |

### Format d'erreur uniforme
```json
{
  "error": "INVALID_TRANSITION",
  "message": "Transition invalide : Ouvert -> Termine. Transitions autorisées : ASSIGNE, FERME"
}
```

### Pattern Singleton pour RestApiClient
Une seule instance du client HTTP assure la **cohérence du token** d'authentification à travers toute l'application.

---

## 4. Améliorations futures possibles

| Amélioration | Bénéfice |
|--------------|----------|
| JWT (JSON Web Tokens) | Authentification stateless, scalable |
| Base de données (SQLite/PostgreSQL) | Persistance des données |
| WebSockets | Notifications temps réel |
| Cache côté client | Réduction des appels réseau |
| HTTPS | Sécurisation des communications |
| Pagination | Performance avec grands volumes |

---

*6GEI311 - Architecture des logiciels - Automne 2025*

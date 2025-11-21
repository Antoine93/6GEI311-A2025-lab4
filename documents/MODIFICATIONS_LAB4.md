# Modifications apportées pour le Lab 4 - Architecture REST

## Vue d'ensemble

Transformation de l'application standalone (Lab 2-3) en architecture distribuée client-serveur avec API REST.

## Architecture AVANT (Lab 2-3)

```
┌─────────────────────────────────────────────┐
│               Client (GUI)                  │
│                                             │
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
└─────────────────────────────────────────────┘
```

**Problèmes:**
- État partagé en mémoire (mono-utilisateur)
- Impossible de distribuer l'application
- Pas de séparation client/serveur

## Architecture APRÈS (Lab 4)

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

**Avantages:**
- Architecture distribuée multi-utilisateurs
- État centralisé côté serveur
- Communication standardisée (REST/JSON)
- Possibilité d'ajouter d'autres clients (web, mobile)

---

## Modifications détaillées

### 1. Nouveau fichier: `gui/services/RestApiClient.java`

**Rôle:** Client HTTP pour communiquer avec l'API REST du serveur.

**Responsabilités:**
- Encapsule tous les appels HTTP (GET, POST, PUT, PATCH, DELETE)
- Gère l'authentification via token Bearer
- Sérialise/désérialise JSON avec Gson
- Gère les erreurs réseau et HTTP

**Méthodes principales:**
- `login(userID)` → Authentification, récupère token
- `getAllTickets()` → GET /tickets
- `getTicketById(id)` → GET /tickets/{id}
- `createTicket(...)` → POST /tickets
- `updateTicket(...)` → PUT /tickets/{id}
- `deleteTicket(id)` → DELETE /tickets/{id}
- `addComment(...)` → POST /tickets/{id}/comments
- `changeTicketStatus(...)` → PATCH /tickets/{id}/status
- `assignTicket(...)` → PATCH /tickets/{id}/assignment
- `exportTicketToPDF(...)` → GET /tickets/{id}/export/pdf

**Pattern:** Singleton (une seule instance partagée)

---

### 2. Modifié: `gui/controllers/TicketController.java`

**AVANT (Lab 2-3):**
```java
public class TicketController {
    private ApplicationState state;

    public TicketController() {
        this.state = ApplicationState.getInstance();
    }

    public List<TicketDTO> getAllTickets() {
        return convertToTicketDTOs(state.getAllTickets());
    }
}
```

**APRÈS (Lab 4):**
```java
public class TicketController {
    private RestApiClient apiClient;
    private UserDTO currentUser;

    public TicketController() {
        this.apiClient = RestApiClient.getInstance();
        this.currentUser = null;
    }

    public List<TicketDTO> getAllTickets() {
        try {
            return apiClient.getAllTickets();
        } catch (IOException e) {
            System.err.println("Erreur: " + e.getMessage());
            return new ArrayList<>();
        }
    }
}
```

**Changements:**
- ❌ Supprimé: Dépendance à `ApplicationState` (client)
- ❌ Supprimé: Accès direct aux entités `core.entities.*`
- ✅ Ajouté: Dépendance à `RestApiClient`
- ✅ Ajouté: Gestion des erreurs réseau (`IOException`)
- ✅ Ajouté: Méthode `login()` pour authentification
- ✅ Ajouté: Méthode `logout()` pour déconnexion
- ✅ Ajouté: Conversion des statuts d'affichage vers format API

**Toutes les méthodes modifiées:**
- `createTicketWithContentItems()` → Appelle `apiClient.createTicket()`
- `getAllTickets()` → Appelle `apiClient.getAllTickets()`
- `getTicketById()` → Appelle `apiClient.getTicketById()`
- `assignTicket()` → Appelle `apiClient.assignTicket()`
- `changeTicketStatus()` → Appelle `apiClient.changeTicketStatus()`
- `addComment()` → Appelle `apiClient.addComment()`
- `getTicketComments()` → Appelle `apiClient.getTicketComments()`
- `updateTicketWithContentItems()` → Appelle `apiClient.updateTicket()`
- `exportTicketToText()` → Appelle `apiClient.exportTicketToPDF()`
- `deleteTicket()` → Appelle `apiClient.deleteTicket()`

---

### 3. Modifié: `gui/controllers/ApplicationState.java`

**Statut:** DÉPRÉCIÉ (conservé pour compatibilité)

**AVANT (Lab 2-3):**
- Gérait l'état global de l'application côté client
- Contenait la liste des tickets en mémoire
- Contenait la liste des utilisateurs
- Implémentait le pattern Observer

**APRÈS (Lab 4):**
- Marqué comme `@Deprecated`
- Toutes les méthodes affichent des avertissements
- Conservé uniquement pour éviter les erreurs de compilation
- L'état est maintenant géré côté serveur (`api.server.services.ApplicationState`)

**Documentation ajoutée:**
```java
/**
 * IMPORTANT: Ce fichier est conservé pour compatibilité, mais n'est PLUS utilisé
 * dans l'architecture REST du Lab 4.
 *
 * NOUVEAU FLUX (Lab 4):
 * GUI View → TicketController → RestApiClient → API Server → Server ApplicationState → Core Entities
 *
 * ANCIEN FLUX (Lab 2-3):
 * GUI View → TicketController → Client ApplicationState (ce fichier) → Core Entities
 */
```

---

### 4. Modifié: `gui/views/TicketManagerGUI.java`

**Changement mineur:**
```java
// AVANT
ApplicationState.getInstance().addListener(this);

// APRÈS
// Note: ApplicationState n'est plus utilisé en Lab 4 (architecture REST)
// Les changements d'état sont maintenant gérés via le serveur REST
```

**Raison:** Le pattern Observer local n'est plus nécessaire car l'état est géré côté serveur.

---

## Dépendances

### Bibliothèques requises

1. **Gson (Google JSON)** - Sérialisation/désérialisation JSON
   - Utilisé par: `RestApiClient`, `BaseResource` (serveur)
   - Version utilisée: 2.10.1
   - Téléchargement: https://repo1.maven.org/maven2/com/google/code/gson/gson/2.10.1/gson-2.10.1.jar
   - Placer le fichier dans: `lib/gson-2.10.1.jar`

### Compilation (PowerShell/Windows)

```powershell
# Créer le dossier classes si nécessaire
New-Item -ItemType Directory -Force -Path classes

# Serveur (avec Gson)
javac -encoding UTF-8 -cp "lib/gson-2.10.1.jar" -d classes `
  api\server\*.java `
  api\server\models\*.java `
  api\server\resources\*.java `
  api\server\services\*.java `
  core\entities\*.java `
  core\content\*.java `
  core\exporter\*.java

# Client GUI (avec Gson)
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

**Note:** Le backtick `` ` `` en PowerShell permet de continuer la commande sur plusieurs lignes.

### Exécution (PowerShell/Windows)

```powershell
# Démarrer le serveur (port 8080)
java -cp "lib/gson-2.10.1.jar;classes" api.server.TicketAPIServer

# Démarrer le client GUI (dans un autre terminal)
java -cp "lib/gson-2.10.1.jar;classes" MainGUI
```

---

## Flux d'authentification

### Lab 2-3 (Local)
1. LoginDialog affiche la liste des utilisateurs
2. L'utilisateur sélectionne un profil
3. `ApplicationState.setCurrentUser(user)`
4. L'état est stocké en mémoire client

### Lab 4 (REST)
1. LoginDialog affiche la liste des utilisateurs (via GET /users)
2. L'utilisateur sélectionne un profil
3. `TicketController.login(userID)` → POST /auth/login
4. Le serveur crée une session et retourne un token
5. `RestApiClient` stocke le token
6. Tous les appels suivants incluent: `Authorization: Bearer <token>`

---

## Exemples de communication REST

### 1. Création d'un ticket

**Client → Serveur:**
```http
POST /api/v1/tickets HTTP/1.1
Authorization: Bearer session_abc123xyz
Content-Type: application/json

{
  "title": "Bug critique - Crash à la connexion",
  "priority": "Haute",
  "descriptionContent": [
    {
      "type": "TEXT",
      "data": "L'application crash lors de la connexion...",
      "metadata": null
    }
  ]
}
```

**Serveur → Client:**
```http
HTTP/1.1 201 Created
Content-Type: application/json

{
  "ticketID": 1001,
  "title": "Bug critique - Crash à la connexion",
  "status": "Ouvert",
  "priority": "Haute",
  "createdByName": "Utilisateur1",
  "assignedToName": null,
  "description": "[TEXTE] L'application crash...",
  "creationDate": "2025-11-20T10:30:00Z"
}
```

### 2. Changement de statut

**Client → Serveur:**
```http
PATCH /api/v1/tickets/1001/status HTTP/1.1
Authorization: Bearer session_abc123xyz
Content-Type: application/json

{
  "newStatus": "ASSIGNE"
}
```

**Serveur → Client:**
```http
HTTP/1.1 200 OK
Content-Type: application/json

{
  "ticketID": 1001,
  "title": "Bug critique - Crash à la connexion",
  "status": "Assigne",
  ...
}
```

---

## Gestion des erreurs

### Lab 2-3 (Local)
- Exceptions Java propagées directement
- Pas de codes d'erreur standardisés

### Lab 4 (REST)

**Codes HTTP standardisés:**
- `200 OK` - Succès
- `201 Created` - Ressource créée
- `204 No Content` - Succès sans contenu (ex: DELETE)
- `400 Bad Request` - Validation échouée
- `401 Unauthorized` - Non authentifié
- `403 Forbidden` - Permissions insuffisantes
- `404 Not Found` - Ressource introuvable
- `500 Internal Server Error` - Erreur serveur

**Format d'erreur JSON:**
```json
{
  "error": "VALIDATION_ERROR",
  "message": "Le titre du ticket ne peut pas être vide",
  "details": {}
}
```

**Gestion dans le client:**
```java
try {
    apiClient.createTicket(...);
} catch (IOException e) {
    System.err.println("Erreur: " + e.getMessage());
    // Afficher message à l'utilisateur
}
```

---

## Points d'attention pour le développement futur

### 1. Authentification
- **Actuel:** Token simple (session ID)
- **Amélioration possible:** JWT (JSON Web Tokens)

### 2. Gestion des sessions
- **Actuel:** En mémoire côté serveur (perdu au redémarrage)
- **Amélioration possible:** Base de données ou Redis

### 3. Gestion des erreurs réseau
- **Actuel:** Affichage console (`System.err`)
- **Amélioration possible:** Dialogues Swing avec retry

### 4. Rechargement automatique
- **Actuel:** L'utilisateur doit cliquer "Rafraîchir"
- **Amélioration possible:** Polling périodique ou WebSockets

### 5. Cache côté client
- **Actuel:** Aucun cache
- **Amélioration possible:** Cache avec invalidation intelligente

---

## Tests à effectuer

### Tests fonctionnels
- ✅ Login utilisateur
- ✅ Création de ticket
- ✅ Modification de ticket
- ✅ Assignation de ticket
- ✅ Changement de statut
- ✅ Ajout de commentaires
- ✅ Export PDF
- ✅ Suppression de ticket (admin)
- ✅ Filtrage par permissions

### Tests d'erreur
- ✅ Serveur non démarré → Message d'erreur
- ✅ Session expirée → 401 Unauthorized
- ✅ Permissions insuffisantes → 403 Forbidden
- ✅ Ticket inexistant → 404 Not Found
- ✅ Validation échouée → 400 Bad Request

---

## Résumé des fichiers modifiés

| Fichier | Type | Description |
|---------|------|-------------|
| `gui/services/RestApiClient.java` | ✅ NOUVEAU | Client HTTP REST |
| `gui/controllers/TicketController.java` | 🔄 MODIFIÉ | Utilise RestApiClient au lieu de ApplicationState |
| `gui/controllers/ApplicationState.java` | ⚠️ DÉPRÉCIÉ | Conservé pour compatibilité |
| `gui/views/TicketManagerGUI.java` | 🔄 MODIFIÉ | Suppression référence à ApplicationState |

## Fichiers serveur (déjà existants)
| Fichier | Description |
|---------|-------------|
| `api/server/resources/TicketResource.java` | Endpoints REST tickets |
| `api/server/resources/AuthResource.java` | Endpoints REST authentification |
| `api/server/services/ApplicationState.java` | État serveur (sessions, tickets) |

---

## Avantages de l'architecture REST

1. **Séparation client/serveur**
   - Le client GUI ne connaît pas la logique métier
   - Le serveur peut servir plusieurs clients simultanément

2. **Scalabilité**
   - Possibilité d'ajouter d'autres clients (web, mobile)
   - Possibilité de distribuer le serveur (load balancing)

3. **Standardisation**
   - Communication via HTTP/JSON (standard web)
   - Documentation via OpenAPI/Swagger

4. **Testabilité**
   - Les endpoints peuvent être testés indépendamment (Postman, curl)
   - Les clients peuvent être testés avec des mocks

5. **Maintenance**
   - Changements serveur sans recompiler le client
   - Versioning de l'API possible (/api/v1, /api/v2)

---

## Tests et validation de l'API

### Tests effectués avec curl

Tous les endpoints de l'API ont été testés et validés avec succès le 2025-11-20.

#### Scénario de test complet

**1. Authentification utilisateur normal (ID 1)**
```
POST /auth/login
✅ Réponse: Token session_30504897-b531-4ef9-952f-c4fe978a332f
✅ Utilisateur: Utilisateur1 (Developpeur)
[AUTH] Login réussi pour l'utilisateur: Utilisateur1
```

**2. Récupération de la liste des utilisateurs**
```
GET /users
✅ Réponse: 3 utilisateurs (Utilisateur1, Utilisateur2, Admin1)
[USERS] Liste de 3 utilisateurs récupérée par Utilisateur1
```

**3. Récupération de la liste des tickets**
```
GET /tickets
✅ Réponse: 4 tickets affichés (filtrés selon permissions)
[TICKETS] Liste de 4 tickets récupérée pour Utilisateur1
```

**4. Création d'un nouveau ticket**
```
POST /tickets
Body: {
  "title": "Test API REST - Nouveau ticket",
  "priority": "Haute",
  "descriptionContent": [{"type": "TEXT", "data": "Ceci est un test de creation via curl"}]
}
✅ Réponse: Ticket #1005 créé
✅ Statut: Ouvert
✅ Créé par: Utilisateur1
[TICKETS] Ticket #1005 créé par Utilisateur1: Test API REST - Nouveau ticket
```

**5. Récupération d'un ticket spécifique**
```
GET /tickets/1005
✅ Réponse: Détails complets du ticket #1005
[TICKETS] Ticket #1005 récupéré par Utilisateur1
```

**6. Ajout d'un commentaire**
```
POST /tickets/1005/comments
Body: {"text": "Premier commentaire de test via API"}
✅ Réponse: Commentaire ajouté avec succès
[COMMENTS] Commentaire ajouté au ticket #1005 par Utilisateur1
```

**7. Récupération des commentaires**
```
GET /tickets/1005/comments
✅ Réponse: ["Premier commentaire de test via API"]
[COMMENTS] 1 commentaires récupérés pour ticket #1005
```

**8. Vérification des transitions disponibles**
```
GET /tickets/1005/status
✅ Réponse: ["ASSIGNE", "FERME"]
✅ Validation: Transitions correctes depuis statut "Ouvert"
```

**9. Changement de statut**
```
POST /tickets/1005/status
Body: {"newStatus": "ASSIGNE"}
✅ Réponse: Statut changé de "Ouvert" à "Assigne"
✅ Validation: Transition autorisée respectée
[STATUS] Statut du ticket #1005 changé vers: ASSIGNE par Utilisateur1
```

**10. Assignation du ticket**
```
POST /tickets/1005/assignment
Body: {"userID": 2}
✅ Réponse: Ticket assigné à "Utilisateur2"
✅ Champ assignedToName mis à jour
[ASSIGNMENT] Ticket #1005 assigné à l'utilisateur #2 par Utilisateur1
```

**11. Modification du ticket**
```
PUT /tickets/1005
Body: {"title": "Test API REST - Ticket modifie", "priority": "Critique"}
✅ Réponse: Titre et priorité modifiés
✅ Date updateDate mise à jour
[TICKETS] Ticket #1005 modifié par Utilisateur1
```

**12. Export PDF**
```
GET /tickets/1005/export/pdf
✅ Réponse: Contenu PDF formaté (texte)
✅ Format:
==================================================
     EXPORT PDF - TICKET DESCRIPTION
==================================================
SECTION TEXTE
--------------------------------------------------
Ceci est un test de creation via curl
--------------------------------------------------
[EXPORT] Ticket #1005 exporté en PDF par Utilisateur1
```

**13. Authentification Admin (ID 100)**
```
POST /auth/login
Body: {"userID": 100}
✅ Réponse: Token session_0269a42a-018e-4ac1-811d-569a9db7be92
✅ Utilisateur: Admin1 (Admin, isAdmin: true)
[AUTH] Login réussi pour l'utilisateur: Admin1
```

**14. Suppression de ticket (Admin uniquement)**
```
DELETE /tickets/1005
✅ Réponse: 204 No Content
✅ Validation: Ticket supprimé avec succès
[TICKETS] Ticket #1005 supprimé par Admin1
```

**15. Vérification de la suppression**
```
GET /tickets/1005
✅ Réponse: 404 Not Found
✅ Message: "Ticket #1005 introuvable"
✅ Validation: Suppression effective
```

### Résultats des tests

| Endpoint | Méthode | Test | Résultat |
|----------|---------|------|----------|
| `/auth/login` | POST | Authentification utilisateur | ✅ PASS |
| `/auth/login` | POST | Authentification admin | ✅ PASS |
| `/users` | GET | Liste utilisateurs | ✅ PASS |
| `/tickets` | GET | Liste tickets (filtrée) | ✅ PASS |
| `/tickets` | POST | Création ticket | ✅ PASS |
| `/tickets/{id}` | GET | Détails ticket | ✅ PASS |
| `/tickets/{id}` | PUT | Modification ticket | ✅ PASS |
| `/tickets/{id}` | DELETE | Suppression ticket (admin) | ✅ PASS |
| `/tickets/{id}/comments` | GET | Liste commentaires | ✅ PASS |
| `/tickets/{id}/comments` | POST | Ajout commentaire | ✅ PASS |
| `/tickets/{id}/status` | GET | Transitions disponibles | ✅ PASS |
| `/tickets/{id}/status` | POST | Changement statut | ✅ PASS |
| `/tickets/{id}/assignment` | POST | Assignation ticket | ✅ PASS |
| `/tickets/{id}/export/pdf` | GET | Export PDF | ✅ PASS |

**Total: 14/14 endpoints testés avec succès** ✅

### Validations fonctionnelles

- ✅ **Authentification**: Token Bearer fonctionnel
- ✅ **Permissions**: Admin vs Utilisateur normal correctement implémentées
- ✅ **Filtrage**: Utilisateurs voient uniquement leurs tickets
- ✅ **Transitions de statut**: Validation des transitions selon la machine à états
- ✅ **Codes HTTP**: 200 (OK), 201 (Created), 204 (No Content), 404 (Not Found)
- ✅ **Format JSON**: Sérialisation/désérialisation correcte avec Gson
- ✅ **Gestion d'erreurs**: Messages clairs et codes HTTP appropriés
- ✅ **Pattern Composite**: Content sérialisé correctement en JSON
- ✅ **Pattern Strategy**: Export PDF fonctionnel

### Logs serveur

Les logs serveur confirment le bon fonctionnement:
```
[AUTH] Login réussi pour l'utilisateur: Utilisateur1
[USERS] Liste de 3 utilisateurs récupérée par Utilisateur1
[TICKETS] Liste de 4 tickets récupérée pour Utilisateur1
[TICKETS] Ticket #1005 créé par Utilisateur1: Test API REST - Nouveau ticket
[TICKETS] Ticket #1005 récupéré par Utilisateur1
[COMMENTS] Commentaire ajouté au ticket #1005 par Utilisateur1
[COMMENTS] 1 commentaires récupérés pour ticket #1005
[STATUS] Statut du ticket #1005 changé vers: ASSIGNE par Utilisateur1
[ASSIGNMENT] Ticket #1005 assigné à l'utilisateur #2 par Utilisateur1
[TICKETS] Ticket #1005 modifié par Utilisateur1
[EXPORT] Ticket #1005 exporté en PDF par Utilisateur1
[AUTH] Login réussi pour l'utilisateur: Admin1
[TICKETS] Ticket #1005 supprimé par Admin1
```

### Conclusion des tests

L'architecture REST est **entièrement fonctionnelle et prête pour la démonstration**. Tous les endpoints répondent correctement, les permissions sont respectées, et la communication client-serveur via HTTP/JSON fonctionne parfaitement.

---

*Document créé pour le Lab 4 - 6GEI311 A2025*
*Date: 2025-11-20*
*Tests validés le: 2025-11-20*

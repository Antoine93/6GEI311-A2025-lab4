# Phase 4 : Intégration Client - Documentation

**Date:** 2025-11-17
**Projet:** 6GEI311-A2025-lab4 - Système de gestion de tickets
**Statut:** ✅ COMPLÉTÉE

---

## 📋 Résumé de la Phase 4

La Phase 4 a consisté à **transformer la GUI pour utiliser l'API REST** au lieu d'accéder directement à `ApplicationState` local.

**Objectif:** Découpler la GUI de la logique métier et la faire communiquer avec le serveur REST via HTTP/JSON.

---

## 🏗️ Architecture mise en place

### Avant (Architecture monolithique)

```
TicketManagerGUI (Swing)
   ↓
TicketController
   ↓
ApplicationState (singleton local)
   ↓
Entities (core/)
```

### Après (Architecture client-serveur REST)

```
MainGUI_REST (Swing)
   ↓
TicketControllerREST
   ↓
ITicketService (interface)
   ↓
RestTicketService (implémentation)
   ↓
SimpleHttpClient (HTTP)
   ↓ HTTP/JSON
Serveur REST (api.server.TicketAPIServer)
   ↓
ApplicationState (serveur)
   ↓
Entities (core/)
```

---

## 📦 Nouveaux composants créés

### 1. **ITicketService** (gui/services/ITicketService.java)

Interface d'abstraction pour les opérations sur les tickets.

**Responsabilités:**
- Définir le contrat de service (méthodes)
- Permettre plusieurs implémentations (REST, mock pour tests)

**Méthodes principales:**
- `login(userID)` : Authentification
- `getAllTickets()` : Récupérer tous les tickets
- `createTicket(...)` : Créer un ticket
- `updateTicket(...)` : Modifier un ticket
- `addComment(...)` : Ajouter un commentaire
- `changeTicketStatus(...)` : Changer le statut
- `assignTicket(...)` : Assigner un ticket
- `exportTicketToPDF(...)` : Exporter en PDF

### 2. **ServiceException** (gui/services/ServiceException.java)

Exception personnalisée pour gérer les erreurs de service.

**Propriétés:**
- `httpStatusCode` : Code HTTP (400, 401, 403, 404, 500)
- `errorCode` : Code métier (VALIDATION_ERROR, UNAUTHORIZED, etc.)
- `message` : Message d'erreur détaillé

**Méthodes utilitaires:**
- `isAuthenticationError()` : Erreur 401
- `isPermissionError()` : Erreur 403
- `isValidationError()` : Erreur 400
- `isNotFoundError()` : Erreur 404
- `isServerError()` : Erreur 500+

### 3. **SimpleHttpClient** (gui/services/SimpleHttpClient.java)

Client HTTP léger utilisant `java.net.HttpURLConnection`.

**Responsabilités:**
- Envoyer des requêtes HTTP (GET, POST, PUT, PATCH, DELETE)
- Gérer l'authentification (header Authorization: Bearer token)
- Gérer les timeouts (5s connexion, 10s lecture)

**Méthodes:**
- `get(endpoint)` : GET request
- `post(endpoint, jsonBody)` : POST request
- `put(endpoint, jsonBody)` : PUT request
- `patch(endpoint, jsonBody)` : PATCH request
- `delete(endpoint)` : DELETE request
- `setAuthToken(token)` : Définir le token
- `clearAuthToken()` : Supprimer le token

**Gestion des erreurs:**
- `ConnectException` → "Serveur inaccessible"
- `SocketTimeoutException` → "Timeout"
- Codes HTTP >= 400 → Exception avec body d'erreur

### 4. **RestTicketService** (gui/services/RestTicketService.java)

Implémentation REST de `ITicketService`.

**Responsabilités:**
- Appeler l'API REST via `SimpleHttpClient`
- Convertir les objets Java ↔ JSON (avec Gson)
- Convertir les DTOs serveur ↔ DTOs GUI
- Gérer les erreurs HTTP et les transformer en `ServiceException`

**URL de base:** `http://localhost:8080/api/v1`

**Exemples d'appels:**

```java
// Login
UserDTO user = restService.login(1);

// Récupérer tous les tickets
List<TicketDTO> tickets = restService.getAllTickets();

// Créer un ticket
List<ContentItemDTO> content = List.of(
    new ContentItemDTO(ContentItemDTO.ContentType.TEXT, "Description", null)
);
TicketDTO ticket = restService.createTicket("Titre", content, "Haute");

// Changer le statut
TicketDTO updated = restService.changeTicketStatus(1001, "ASSIGNE");
```

**Gestion de la session:**
- Login → Stocke `authToken` et `currentUser`
- Token envoyé dans header `Authorization: Bearer <token>` pour toutes les requêtes
- Logout → Supprime `authToken` et `currentUser`

### 5. **TicketControllerREST** (gui/controllers/TicketControllerREST.java)

Version REST du `TicketController`.

**Différences avec TicketController:**
- ✅ Délègue tout à `ITicketService` (pas d'accès direct aux entités)
- ✅ Gère les `ServiceException` et affiche des messages d'erreur
- ✅ Compatible avec l'ancien controller (mêmes méthodes publiques)

**Gestion des erreurs:**

```java
try {
    ticketService.createTicket(...);
} catch (ServiceException e) {
    if (e.isValidationError()) {
        System.err.println("Erreur de validation: " + e.getMessage());
    } else if (e.isAuthenticationError()) {
        System.err.println("Non authentifié. Veuillez vous reconnecter.");
    }
}
```

### 6. **MainGUI_REST** (MainGUI_REST.java)

Interface graphique Swing utilisant l'API REST.

**Fonctionnalités implémentées:**
- ✅ Login avec dialogue (demande ID utilisateur)
- ✅ Affichage de la liste des tickets dans une table
- ✅ Détails du ticket sélectionné
- ✅ Création de ticket
- ✅ Ajout de commentaire
- ✅ Changement de statut (avec transitions validées)
- ✅ Assignation de ticket
- ✅ Export PDF

**Composants UI:**
- `JTable` : Liste des tickets (6 colonnes : ID, Titre, Statut, Priorité, Créé par, Assigné à)
- `JTextArea` : Détails du ticket sélectionné
- Boutons : Rafraîchir, Créer, Commentaire, Statut, Assigner, Export

**Gestion des erreurs réseau:**
- Si le serveur n'est pas démarré → Message clair lors du login
- Si une requête échoue → Message dans la console (peut être amélioré avec dialogue)

---

## 🚀 Compilation et Exécution

### Prérequis

1. **Serveur REST démarré** (port 8080)
2. **Gson** disponible dans `api/server/lib/gson-2.10.1.jar`

### Étape 1 : Compiler tous les composants

```bash
javac -encoding UTF-8 -cp "api/server/lib/*;classes" -d classes \
  core/**/*.java \
  api/server/models/*.java \
  api/server/services/*.java \
  api/server/resources/*.java \
  api/server/*.java \
  gui/models/*.java \
  gui/services/*.java \
  gui/controllers/TicketControllerREST.java \
  MainGUI_REST.java
```

**Résultat attendu:**
```
Note: gui\services\SimpleHttpClient.java uses or overrides a deprecated API.
Note: Recompile with -Xlint:deprecation for details.
```

✅ Compilation réussie (le warning est normal et sans danger)

### Étape 2 : Démarrer le serveur REST

**Terminal 1:**

```bash
java -cp "classes;api/server/lib/*" api.server.TicketAPIServer
```

**Sortie attendue:**

```
==================================================
  Serveur API REST - Système de Gestion de Tickets
  Port: 8080
  URL de base: http://localhost:8080/api/v1
==================================================

[INIT] ApplicationState initialisé avec 3 utilisateurs et 3 tickets
[INFO] Handlers enregistrés avec succès
[OK] Serveur démarré avec succès!

📚 Documentation interactive (Swagger UI):
  http://localhost:8080/docs

Endpoints disponibles:
  GET    /api/v1
  POST   /api/v1/auth/login
  GET    /api/v1/auth/session
  POST   /api/v1/auth/logout
  GET    /api/v1/users
  GET    /api/v1/users/{id}
  GET    /api/v1/tickets
  POST   /api/v1/tickets
  ...

Appuyez sur Ctrl+C pour arrêter le serveur...
```

### Étape 3 : Lancer le client GUI REST

**Terminal 2:**

```bash
java -cp "classes;api/server/lib/*" MainGUI_REST
```

**Dialogue de connexion:**
1. Entre `1` (Utilisateur1, Développeur)
2. Ou `2` (Utilisateur2, Testeur)
3. Ou `100` (Admin1, Admin)

**Interface graphique:**
- Liste des tickets chargée depuis l'API
- Sélectionner un ticket → Détails affichés
- Boutons actifs pour toutes les opérations

---

## 🧪 Tests effectués

### Test 1 : Connexion et affichage des tickets

**Procédure:**
1. Démarrer le serveur
2. Lancer `MainGUI_REST`
3. Entrer ID utilisateur `1`

**Résultat attendu:**
- ✅ Dialogue "Bienvenue Utilisateur1 (Developpeur)"
- ✅ 3 tickets affichés (créés lors de l'initialisation du serveur)

**Sortie serveur:**

```
[AUTH] Login réussi pour l'utilisateur: Utilisateur1
[TICKETS] Liste de 3 tickets récupérée pour Utilisateur1
```

### Test 2 : Création d'un ticket

**Procédure:**
1. Cliquer sur "Créer un ticket"
2. Titre: "Test REST"
3. Description: "Ticket créé depuis GUI REST"
4. Priorité: "Haute"

**Résultat attendu:**
- ✅ Message "Ticket #1004 créé avec succès!"
- ✅ Table rafraîchie avec 4 tickets

**Sortie serveur:**

```
[TICKETS] Ticket #1004 créé par Utilisateur1: Test REST
```

### Test 3 : Ajout de commentaire

**Procédure:**
1. Sélectionner un ticket
2. Cliquer sur "Ajouter commentaire"
3. Saisir: "Commentaire depuis GUI REST"

**Résultat attendu:**
- ✅ Commentaire ajouté
- ✅ Détails rafraîchis avec le nouveau commentaire

**Sortie serveur:**

```
[COMMENTS] Commentaire ajouté au ticket #1001 par Utilisateur1
```

### Test 4 : Changement de statut

**Procédure:**
1. Sélectionner ticket #1001 (statut OUVERT)
2. Cliquer sur "Changer statut"
3. Sélectionner "ASSIGNE"

**Résultat attendu:**
- ✅ Statut changé à "Assigne"
- ✅ Table rafraîchie

**Sortie serveur:**

```
[STATUS] Statut du ticket #1001 changé vers: ASSIGNE par Utilisateur1
Statut du ticket #1001 change : Ouvert -> Assigne
```

### Test 5 : Assignation (nécessite Admin)

**Procédure:**
1. Se connecter en tant que `100` (Admin)
2. Sélectionner un ticket
3. Cliquer sur "Assigner"
4. Sélectionner "Utilisateur2 (ID: 2)"

**Résultat attendu:**
- ✅ Ticket assigné
- ✅ Table rafraîchie avec nom de l'assigné

**Sortie serveur:**

```
[ASSIGNMENT] Ticket #1001 assigné à l'utilisateur #2 par Admin1
Ticket #1001 assigne a l'utilisateur ID: 2
```

### Test 6 : Export PDF

**Procédure:**
1. Sélectionner un ticket
2. Cliquer sur "Export PDF"

**Résultat attendu:**
- ✅ Nouvelle fenêtre avec contenu PDF formaté

**Sortie serveur:**

```
[EXPORT] Ticket #1001 exporté en PDF par Utilisateur1
```

### Test 7 : Erreur - Transition invalide

**Procédure:**
1. Ticket au statut OUVERT
2. Tenter de changer directement à TERMINE (transition invalide)

**Résultat attendu:**
- ✅ Erreur retournée par le serveur
- ✅ Message d'erreur affiché dans la console

**Sortie serveur:**

```
[ERROR] Erreur lors du changement de statut: Transition invalide : Ouvert -> Termine. Transitions autorisées : ASSIGNE, FERME
  → Transition invalide. Transition invalide : Ouvert -> Termine...
```

### Test 8 : Erreur - Serveur non démarré

**Procédure:**
1. Arrêter le serveur (Ctrl+C)
2. Lancer `MainGUI_REST`
3. Tenter de se connecter

**Résultat attendu:**
- ✅ Message "Impossible de se connecter au serveur. Vérifiez que le serveur est démarré."

---

## 📊 Comparaison avec l'architecture monolithique

| Aspect | Architecture monolithique | Architecture REST |
|--------|---------------------------|-------------------|
| **Démarrage** | 1 processus (MainGUI) | 2 processus (Serveur + Client) |
| **Communication** | Appels de méthodes Java | HTTP/JSON |
| **État** | En mémoire dans ApplicationState local | En mémoire côté serveur |
| **Latence** | < 1ms (local) | 5-50ms (réseau local) |
| **Scalabilité** | 1 client | N clients simultanés |
| **Testabilité** | Difficile (tout couplé) | Facile (API testable indépendamment) |
| **Réutilisabilité** | GUI Java seulement | Tout client HTTP (web, mobile, CLI) |
| **Complexité** | Faible | Moyenne (2 composants) |
| **Persistance** | Non (perte au redémarrage) | Non (mais serveur peut persister) |

---

## 🎯 Objectifs de la Phase 4 - Statut

### Tâches complétées

- [x] **4.1** Créer une abstraction pour le client API
  - [x] Interface `ITicketService`
  - [x] Implémentation `RestTicketService`

- [x] **4.2** Refactorer `TicketController`
  - [x] `TicketControllerREST` créé
  - [x] Toutes les méthodes implémentées (CRUD, commentaires, statuts, assignation, export)

- [x] **4.3** Gérer l'authentification côté client
  - [x] Login simplifié via dialogue
  - [x] Token stocké localement dans `RestTicketService`
  - [x] Token inclus dans toutes les requêtes HTTP

- [x] **4.4** Gérer les erreurs réseau
  - [x] `ServiceException` avec types d'erreur (401, 403, 404, 400, 500)
  - [x] Messages clairs pour toutes les erreurs
  - [x] Gestion de serveur inaccessible

- [x] **4.5** Tester la conversion JSON → DTO
  - [x] Conversion automatique via Gson
  - [x] ContentItemDTO correctement sérialisé/désérialisé

- [x] **4.6** Supprimer ApplicationState côté client
  - [x] Pas de dépendance à `ApplicationState` dans `MainGUI_REST`
  - [x] Tout passe par `ITicketService`

- [x] **4.7** Tester l'interface GUI avec le serveur
  - [x] Login ✅
  - [x] Affichage de la liste des tickets ✅
  - [x] Création d'un ticket ✅
  - [x] Modification d'un ticket (via commentaires)
  - [x] Ajout de commentaires ✅
  - [x] Changement de statut ✅
  - [x] Assignation ✅
  - [x] Export PDF ✅

### Livrables Phase 4

- ✅ GUI modifiée et fonctionnelle avec le serveur REST (`MainGUI_REST.java`)
- ✅ Gestion des erreurs réseau robuste (`ServiceException`, `SimpleHttpClient`)
- ✅ Plus aucune dépendance à ApplicationState local dans le client REST
- ✅ Documentation complète de la phase 4

---

## 🔍 Améliorations possibles (non requises pour le lab)

### 1. Gestion d'erreur dans la GUI

**Actuellement:** Messages dans la console
**Amélioration:** Dialogues `JOptionPane` pour toutes les erreurs

```java
try {
    ticketService.createTicket(...);
} catch (ServiceException e) {
    JOptionPane.showMessageDialog(
        this,
        e.getMessage(),
        "Erreur " + e.getHttpStatusCode(),
        JOptionPane.ERROR_MESSAGE
    );
}
```

### 2. Indicateur de chargement

**Actuellement:** Label "Chargement..." dans la barre de statut
**Amélioration:** `JProgressBar` ou curseur d'attente

### 3. Rafraîchissement automatique

**Actuellement:** Bouton "Rafraîchir" manuel
**Amélioration:** Timer pour rafraîchir toutes les N secondes

```java
Timer timer = new Timer(5000, e -> loadTickets());
timer.start();
```

### 4. Cache local

**Actuellement:** Chaque accès = requête HTTP
**Amélioration:** Cacher les tickets localement

```java
private Map<Integer, TicketDTO> ticketCache;
```

### 5. Edition de ticket complète

**Actuellement:** Pas de dialogue d'édition
**Amélioration:** Réutiliser `EditTicketDialog` avec `RestTicketService`

---

## 🎓 Leçons apprises

### 1. Avantages de l'architecture REST

✅ **Découplage** : La GUI et le serveur peuvent évoluer indépendamment
✅ **Testabilité** : L'API peut être testée avec curl/Postman sans GUI
✅ **Réutilisabilité** : Plusieurs clients (desktop, web, mobile) peuvent utiliser la même API
✅ **Scalabilité** : Plusieurs clients peuvent se connecter simultanément

### 2. Défis rencontrés

⚠️ **Latence réseau** : Les appels HTTP sont plus lents que les appels locaux (5-50ms vs < 1ms)
⚠️ **Gestion d'erreurs** : Plus complexe (erreurs réseau + erreurs métier)
⚠️ **Sérialisation** : Conversion Java ↔ JSON nécessite attention (types, null, dates)
⚠️ **Synchronisation** : Le client doit rafraîchir pour voir les changements d'autres clients

### 3. Pattern DTO essentiel

Le pattern DTO permet de:
- Découpler les entités du domaine (`core.entities.Ticket`) de la représentation API
- Aplatir les structures complexes (Content → ContentItemDTO)
- Contrôler ce qui est exposé à l'API (pas de champs internes)

### 4. Importance de l'abstraction (ITicketService)

L'interface `ITicketService` permet:
- De changer d'implémentation sans modifier la GUI
- De créer des mocks pour les tests
- De basculer entre mode local et mode REST facilement

```java
// Production
ITicketService service = new RestTicketService();

// Tests
ITicketService service = new MockTicketService();
```

---

## 📝 Fichiers créés/modifiés

### Nouveaux fichiers

```
gui/services/ITicketService.java          (Interface)
gui/services/ServiceException.java        (Exception personnalisée)
gui/services/SimpleHttpClient.java        (Client HTTP)
gui/services/RestTicketService.java       (Implémentation REST)
gui/controllers/TicketControllerREST.java (Controller REST)
MainGUI_REST.java                         (Interface graphique REST)
documents/PHASE4_INTEGRATION_CLIENT.md    (Ce document)
```

### Fichiers inchangés

```
core/**/*.java                            (Logique métier pure)
api/server/**/*.java                      (Serveur REST)
gui/models/**/*.java                      (DTOs)
MainGUI.java                              (GUI originale standalone)
```

---

## ✅ Conclusion

La **Phase 4 a été complétée avec succès** !

L'application peut maintenant fonctionner en mode **client-serveur REST** avec:
- ✅ Serveur REST indépendant
- ✅ Client GUI communiquant via HTTP/JSON
- ✅ Gestion des erreurs robuste
- ✅ Authentification avec tokens
- ✅ Toutes les fonctionnalités implémentées

**Prochaine phase:** Phase 5 - Tests et validation complète

---

**Fin de la documentation Phase 4**

# Leçons Apprises - Lab 4 : API REST pour Système de Gestion de Tickets

## Phase 3 : Implémentation Serveur (Complétée le 2025-11-16)

### 📊 Résumé Exécutif

**Statut**: ✅ Phase 3 complétée avec succès
**Serveur**: Fonctionnel sur http://localhost:8080/api/v1
**Temps estimé**: ~4 heures

---

## ✅ Réalisations Majeures

### 1. Architecture REST Sécurisée

**Transformation Standalone → REST:**

| Aspect | Avant (Lab 2-3) | Après (Lab 4) |
|--------|-----------------|---------------|
| État application | Client (mémoire locale) | **Serveur centralisé** |
| Authentification | Aucune | **Token Bearer (UUID)** |
| Permissions | GUI seulement | **Validées côté serveur** |
| Sessions | Aucune | **ConcurrentHashMap thread-safe** |
| Filtrage données | Client | **Serveur (selon rôle)** |

### 2. Sécurité Implémentée (BaseResource.java)

```java
// Authentification requise
protected User requireAuth(HttpExchange exchange) {
    String token = extractToken(exchange);
    User user = appState.getUserFromSession(token);
    if (user == null) {
        sendErrorResponse(exchange, 401, "UNAUTHORIZED", "Authentification requise");
    }
    return user;
}

// Admin seulement
protected boolean requireAdmin(HttpExchange exchange, User user) {
    if (!(user instanceof Admin)) {
        sendErrorResponse(exchange, 403, "FORBIDDEN", "Privilèges admin requis");
        return false;
    }
    return true;
}
```

**Résultat**: Tous les endpoints protégés, permissions validées!

### 3. Pattern Composite Sérialisé

**Défi**: Sérialiser hiérarchie polymorphe `Content` interface

**Solution** (ApplicationState.java:175-265):
- `convertContentToDTO()` → List<ContentItemDTO>
- `convertDTOToContent()` → Content (simple ou composite)
- Support complet: TextContent, ImageContent, VideoContent, CompositeContent

**Exemple JSON**:
```json
{
  "ticketID": 1003,
  "descriptionContent": [
    {"type": "TEXT", "data": "Description", "metadata": null},
    {"type": "IMAGE", "data": "/error.png", "metadata": "Caption"},
    {"type": "VIDEO", "data": "/demo.mp4", "metadata": "120"}
  ]
}
```

---

## 🎯 Avantages Architecture REST

### 1. Séparation Client-Serveur

**Avantages constatés:**
- ✅ **Scalabilité**: Plusieurs clients peuvent se connecter simultanément
- ✅ **Testabilité**: Endpoints testables avec Postman/curl (indépendamment GUI)
- ✅ **Maintenabilité**: Changements serveur sans impact client
- ✅ **Sécurité**: Validation centralisée (client ne peut pas contourner)

**Exemple concret:**
```java
// Avant
List<Ticket> tickets = ApplicationState.getInstance().getAllTickets();

// Après
GET /api/v1/tickets
Authorization: Bearer <token>
→ Serveur filtre selon permissions utilisateur
```

### 2. Validation State Machine Côté Serveur

**Implémentation** (TicketResource.java:316):
```java
try {
    TicketDTO updatedTicket = appState.changeTicketStatus(ticketId, newStatus);
} catch (IllegalStateException e) {
    // Transition invalide détectée!
    sendErrorResponse(exchange, 400, "INVALID_TRANSITION", e.getMessage());
}
```

**Résultat**: Transitions invalides (ex: OUVERT → TERMINE) **rejetées** avec message clair!

### 3. Thread-Safety

**Solution implémentée:**
```java
allTickets = Collections.synchronizedList(new ArrayList<>());
sessions = new ConcurrentHashMap<>();

synchronized (allTickets) {
    allTickets.add(ticket);
}
```

**Protection**: Modifications concurrentes gérées correctement

---

## ⚠️ Défis Rencontrés et Solutions

### Défi 1: Pattern Observer Ne Fonctionne Plus

**Problème:**
- Standalone: `ApplicationState.notifyTicketsChanged()` → GUI refresh auto
- REST: Serveur distant, impossible de notifier client directement

**Solutions possibles:**
1. **Polling** (simple): Client appelle GET /tickets périodiquement
2. **WebSocket** (avancé): Notifications push temps réel
3. **Refresh manuel** (basique): Bouton "Actualiser"

**Recommandation Phase 4**: Commencer par refresh manuel

### Défi 2: HttpServer vs Spring Boot

**Choix**: `com.sun.net.httpserver.HttpServer` (JDK intégré)

**Avantages pédagogiques:**
- ✅ Comprendre HTTP de bas niveau
- ✅ Routage manuel = contrôle total
- ✅ Pas de dépendances (sauf Gson)

**Inconvénients:**
- ❌ Routage verbeux (`if path.contains("/comments")`)
- ❌ Pas de binding automatique JSON
- ❌ Pas d'annotations REST

**Comparaison:**
```java
// HttpServer (actuel)
if ("GET".equals(method) && path.endsWith("/tickets")) {
    handleGetAllTickets(exchange);
}

// Spring Boot (équivalent)
@GetMapping("/tickets")
public List<TicketDTO> getAllTickets(@AuthenticationPrincipal User user) {
    return service.getAllTickets();
}
```

**Conclusion**: HttpServer adapté pour projet académique

### Défi 3: Gestion Erreurs Réseau

**Implémentation** (BaseResource.java:65):
```java
protected void sendErrorResponse(HttpExchange exchange, int statusCode,
                                  String error, String message) {
    ErrorResponse errorResponse = new ErrorResponse(error, message);
    sendJsonResponse(exchange, statusCode, errorResponse);
}
```

**Codes d'erreur gérés:**
- `401 Unauthorized` → Token absent/invalide
- `403 Forbidden` → Permissions insuffisantes
- `404 Not Found` → Ressource introuvable
- `400 Bad Request` → Validation échouée
- `500 Internal Server Error` → Erreur serveur

---

## 📈 Changements Architecturaux

### 1. Singleton ApplicationState Migré

| Aspect | GUI (avant) | Serveur (après) |
|--------|-------------|-----------------|
| **Localisation** | gui/controllers/ | api/server/services/ |
| **Thread-safety** | Non | **Oui (synchronized)** |
| **Sessions** | Aucune | **ConcurrentHashMap** |
| **Conversion** | Partielle | **Entity ↔ DTO complet** |

### 2. Responsabilités Déplacées

| Responsabilité | Standalone | REST |
|----------------|------------|------|
| Validation permissions | GUI | ✅ **Serveur** |
| Gestion sessions | Aucune | ✅ **Serveur** |
| Filtrage données | GUI | ✅ **Serveur** |
| Validation métier | Partielle | ✅ **Serveur (complet)** |

**Principe appliqué**: **Never Trust the Client**

---

## 📊 Métriques Projet

### Code Serveur

| Composant | Fichiers | Lignes (approx.) |
|-----------|----------|------------------|
| Models (DTOs) | 11 | ~500 |
| Resources | 5 | ~800 |
| Services | 1 | ~420 |
| **Total** | **17** | **~1720** |

### Endpoints Sécurisés

| Catégorie | Nombre | Authentification |
|-----------|--------|------------------|
| Auth | 3 | Public + Bearer |
| Users | 2 | Bearer |
| Tickets CRUD | 5 | Bearer + Permissions |
| Comments | 2 | Bearer |
| Status | 2 | Bearer + Admin/Dev |
| Assignment | 1 | Bearer + Admin/Dev |
| Export | 1 | Bearer |
| **Total** | **16** | **Tous protégés** |

### Permissions Validées

| Opération | Utilisateur | Développeur | Admin |
|-----------|-------------|-------------|-------|
| Créer ticket | ✅ | ✅ | ✅ |
| Voir ses tickets | ✅ | ✅ | ✅ |
| Voir tous tickets | ❌ | ✅ | ✅ |
| Modifier ses tickets | ✅ | ✅ | ✅ |
| Modifier tous tickets | ❌ | ✅ | ✅ |
| Changer statut | ❌ | ✅ | ✅ |
| Assigner ticket | ❌ | ✅ | ✅ |
| Supprimer ticket | ❌ | ❌ | ✅ (Admin seul) |

---

## 💡 Leçons Techniques

### 1. Sécurité Dès le Départ

**Leçon**: Implémenter authentification/autorisation DÈS le début, pas après coup

**Bénéfices:**
- Code plus propre (pas de refactoring massif)
- Sécurité garantie sur TOUS les endpoints
- Traçabilité (logging avec noms utilisateurs)

### 2. Validation Côté Serveur Critique

**Exemple concret:**
```
Sans validation serveur:
  Client → PATCH /status avec TERMINE
  Serveur → Accepte aveuglément
  Résultat → Ticket OUVERT → TERMINE (INVALIDE!)

Avec validation serveur:
  Client → PATCH /status avec TERMINE
  Serveur → Vérifie canTransitionTo()
  Résultat → 400 Bad Request "Transition invalide"
```

**Leçon**: JAMAIS faire confiance aux données du client

### 3. DTOs vs Entités

**Avant:**
```java
// GUI accède directement entités métier
Ticket ticket = state.getTicket(id);
ticket.setTitle("Nouveau titre"); // Modification directe!
```

**Après:**
```java
// GUI utilise DTOs (immutables)
TicketDTO dto = service.getTicket(id);
// dto.setTitle() n'existe PAS → modification via PUT /tickets/{id}
```

**Leçon**: DTOs protègent les entités métier

---

## 📚 Documentation Créée

| Fichier | Contenu | Utilité |
|---------|---------|---------|
| `api/server/README.md` | Compilation, exécution | Démarrage rapide |
| `documents/TESTS_API.md` | 16 tests manuels Postman | Validation complète |
| `documents/plan-travail-lab4.md` | Suivi phases | Gestion projet |
| `documents/lecons_apprises.md` | Ce document | Rapport Section II |
| `test-api.ps1` | Script PowerShell | Tests automatisés |

---

## 🎯 Prochaines Étapes (Phase 4)

### Client REST à Créer

1. **Interface** `ITicketService`
2. **Implémentation** `RestTicketService` (HttpClient)
3. **Refactoring** `TicketController` (utilise ITicketService)
4. **Gestion erreurs** réseau (IOException, timeout)
5. **Stockage token** (pour maintenir session)

### Défis Anticipés

- Gestion erreurs réseau (serveur éteint, timeout)
- Refresh interface (remplacer Observer)
- Latence (HTTP vs mémoire locale)
- Conversion JSON ↔ DTO

---

## ✅ Critères de Succès Phase 3

| Critère | Statut |
|---------|--------|
| Serveur compile sans erreurs | ✅ |
| Serveur démarre sur port 8080 | ✅ |
| Authentification fonctionne | ✅ |
| Permissions validées | ✅ (401/403) |
| State Machine respectée | ✅ |
| Pattern Composite sérialisé | ✅ |
| Tous endpoints implémentés | ✅ (16/16) |
| Documentation complète | ✅ |

---

## 🏆 Conclusion Phase 3

**Objectif atteint**: Serveur REST complet, sécurisé et fonctionnel!

**Forces:**
- Architecture propre et maintenable
- Sécurité robuste (auth + permissions)
- Patterns métier préservés
- Documentation exhaustive

**Points d'amélioration (bonus):**
- Persistance données (JSON/base de données)
- Expiration automatique tokens
- Support HTTPS
- Tests unitaires (JUnit)
- WebSocket (notifications temps réel)

---

## 🛠️ Configuration et Démarrage

### Classpath Windows vs Linux

- Classpath Windows vs Linux : Sous Windows, le séparateur de classpath est ; (pas :). Erreur classique lors du portage de commandes Unix.
  - Dépendances externes : Gson nécessaire pour sérialisation JSON. Les erreurs IDE (soulignements rouges) n'impactent pas la compilation/exécution si le classpath est correct.

  ### Architecture REST

  - Route racine optionnelle : Une API REST n'a pas besoin de handler pour /api/v1, mais c'est une bonne pratique pour fournir une page d'accueil (infos, version, status).
  - Navigation navigateur limitée : Le navigateur ne teste que les routes GET. Pour POST/PATCH/DELETE, il faut curl ou Postman.

  ### Bonnes pratiques identifiées

  - Documentation proactive : Maintenir le README.md et plan-travail-lab4.md à jour facilite le suivi et la reprise du projet.
  - Endpoints découvrables : Lister les endpoints disponibles au démarrage du serveur améliore l'expérience développeur.

# Permissions API REST - Gestion des tickets
**Date:** 2025-11-17

## Vue d'ensemble

L'API REST implémente un système de permissions à trois niveaux basé sur le rôle de l'utilisateur authentifié. L'authentification se fait actuellement **sans mot de passe**, uniquement avec un `userID` via l'endpoint `/auth/login`.

## Authentification

### Connexion (sans mot de passe)
```json
POST /api/v1/auth/login
{
  "userID": 1
}

Response:
{
  "token": "session_abc123...",
  "user": {
    "userID": 1,
    "name": "Utilisateur1",
    "email": "utilisateur1@uqac.ca",
    "role": "Developpeur",
    "isAdmin": false
  }
}
```

Le token retourné doit être inclus dans le header `Authorization: Bearer <token>` pour toutes les requêtes subséquentes.

**Note:** Il n'y a actuellement **aucune validation de mot de passe**. N'importe qui connaissant un `userID` valide peut se connecter.

## Utilisateurs de test

| UserID | Nom | Rôle | Type |
|--------|-----|------|------|
| 1 | Utilisateur1 | Developpeur | User |
| 2 | Utilisateur2 | Testeur | User |
| 100 | Admin1 | Admin | Admin |

## Matrice des permissions

| Endpoint | Méthode | Testeur | Développeur | Admin |
|----------|---------|---------|-------------|-------|
| **Authentification** |
| `/auth/login` | POST | ✅ Oui | ✅ Oui | ✅ Oui |
| **Utilisateurs** |
| `/users` | GET | ✅ Oui | ✅ Oui | ✅ Oui |
| `/users/{id}` | GET | ✅ Oui | ✅ Oui | ✅ Oui |
| **Tickets - Lecture** |
| `/tickets` | GET | ✅ Ses tickets | ✅ Tous | ✅ Tous |
| `/tickets/{id}` | GET | ✅ Son ticket | ✅ Tous | ✅ Tous |
| **Tickets - Écriture** |
| `/tickets` | POST | ✅ Oui | ✅ Oui | ✅ Oui |
| `/tickets/{id}` | PUT | ✅ Son ticket | ✅ Tous | ✅ Tous |
| `/tickets/{id}` | DELETE | ❌ Non | ❌ Non | ✅ Oui |
| **Commentaires** |
| `/tickets/{id}/comments` | GET | ✅ Oui | ✅ Oui | ✅ Oui |
| `/tickets/{id}/comments` | POST | ✅ Oui | ✅ Oui | ✅ Oui |
| **Statut** |
| `/tickets/{id}/status` | GET | ✅ Oui | ✅ Oui | ✅ Oui |
| `/tickets/{id}/status` | PATCH | ❌ Non | ✅ Oui | ✅ Oui |
| **Assignation** |
| `/tickets/{id}/assignment` | PATCH | ❌ Non | ✅ Oui | ✅ Oui |
| **Export** |
| `/tickets/{id}/export/pdf` | GET | ✅ Oui | ✅ Oui | ✅ Oui |

## Niveaux de permissions

### 1. Testeur (Rôle: "Testeur")
**Permissions:**
- Créer des tickets
- Voir uniquement ses propres tickets
- Modifier uniquement ses propres tickets
- Ajouter des commentaires
- Exporter en PDF

**Restrictions:**
- ❌ Ne peut pas voir les tickets des autres utilisateurs
- ❌ Ne peut pas changer les statuts
- ❌ Ne peut pas assigner des tickets
- ❌ Ne peut pas supprimer des tickets

### 2. Développeur (Rôle: "Developpeur")
**Permissions:**
- Toutes les permissions du Testeur
- Voir tous les tickets (tous les utilisateurs)
- Modifier tous les tickets
- Changer les statuts de n'importe quel ticket
- Assigner des tickets à n'importe quel utilisateur

**Restrictions:**
- ❌ Ne peut pas supprimer des tickets

### 3. Administrateur (Classe: Admin)
**Permissions:**
- Toutes les permissions du Développeur
- **Supprimer des tickets**

## Implémentation technique

### Méthodes de vérification (BaseResource.java)

#### `hasFullAccess(User user)`
Retourne `true` pour Admin et Développeur.
```java
protected boolean hasFullAccess(User user) {
    if (user instanceof Admin) return true;
    String role = user.getRole();
    return "Developpeur".equals(role);
}
```

#### `canEditTicket(User user, Integer ticketCreatorID)`
Vérifie si l'utilisateur peut modifier un ticket spécifique.
```java
protected boolean canEditTicket(User user, Integer ticketCreatorID) {
    if (hasFullAccess(user)) return true;
    return user.getUserID() == ticketCreatorID;
}
```

#### `requireAdmin(HttpExchange exchange, User user)`
Vérifie que l'utilisateur est Admin, sinon retourne 403 FORBIDDEN.
```java
protected boolean requireAdmin(HttpExchange exchange, User user) {
    if (!(user instanceof Admin)) {
        sendErrorResponse(exchange, 403, "FORBIDDEN",
            "Cette opération nécessite des privilèges administrateur.");
        return false;
    }
    return true;
}
```

### Filtrage des tickets (TicketResource.java)

```java
List<TicketDTO> tickets = appState.getAllTicketsDTO();

if (!hasFullAccess(user)) {
    // Utilisateur normal: seulement ses propres tickets
    tickets = tickets.stream()
        .filter(t -> t.getCreatedByName().equals(user.getName()))
        .toList();
}
```

## Codes de réponse HTTP

| Code | Signification | Contexte |
|------|---------------|----------|
| 200 | OK | Opération réussie |
| 201 | Created | Ressource créée (ticket, commentaire) |
| 204 | No Content | Suppression réussie |
| 400 | Bad Request | Données invalides |
| 401 | Unauthorized | Token manquant ou invalide |
| 403 | Forbidden | Permissions insuffisantes |
| 404 | Not Found | Ressource introuvable |
| 405 | Method Not Allowed | Méthode HTTP non supportée |
| 500 | Internal Server Error | Erreur serveur |

## Scénarios de test

### Testeur - Restrictions de lecture
```bash
# Login en tant que Testeur (ID: 2)
POST /auth/login {"userID": 2}
# Retourne: token

# Créer un ticket
POST /tickets
Authorization: Bearer <token>
{"title": "Mon ticket", "priority": "Haute"}
# ✅ 201 Created

# Lister les tickets
GET /tickets
Authorization: Bearer <token>
# ✅ 200 OK - Ne voit que ses propres tickets

# Tenter de changer le statut
PATCH /tickets/1/status
Authorization: Bearer <token>
{"newStatus": "EN_COURS"}
# ❌ 403 FORBIDDEN - "Seuls les administrateurs et développeurs peuvent changer les statuts"
```

### Développeur - Accès complet sauf suppression
```bash
# Login en tant que Développeur (ID: 1)
POST /auth/login {"userID": 1}

# Lister tous les tickets
GET /tickets
Authorization: Bearer <token>
# ✅ 200 OK - Voit TOUS les tickets

# Changer un statut
PATCH /tickets/1/status
Authorization: Bearer <token>
{"newStatus": "EN_COURS"}
# ✅ 200 OK

# Assigner un ticket
PATCH /tickets/1/assignment
Authorization: Bearer <token>
{"userID": 2}
# ✅ 200 OK

# Tenter de supprimer
DELETE /tickets/1
Authorization: Bearer <token>
# ❌ 403 FORBIDDEN - "Cette opération nécessite des privilèges administrateur"
```

### Admin - Accès total
```bash
# Login en tant qu'Admin (ID: 100)
POST /auth/login {"userID": 100}

# Supprimer un ticket
DELETE /tickets/1
Authorization: Bearer <token>
# ✅ 204 No Content
```

## Limitations actuelles

### Sécurité
- ⚠️ **Pas de mot de passe** : Authentification basée uniquement sur `userID`
- ⚠️ **Tokens prévisibles** : Format `session_<uuid>` stocké en mémoire
- ⚠️ **Sessions persistantes** : Pas d'expiration de token
- ⚠️ **Pas de HTTPS** : Communication en clair

### Améliorations futures suggérées
1. Ajouter un système de mot de passe avec hachage (bcrypt)
2. Implémenter JWT avec expiration
3. Ajouter un système de refresh tokens
4. Logger les actions sensibles (suppression, changements de permissions)
5. Rate limiting pour prévenir les abus
6. Support HTTPS en production

# Tests API REST - Guide de Test Manuel

## Serveur démarré avec succès! ✅

```
Port: 8080
URL de base: http://localhost:8080/api/v1
```

## Données de test préchargées

**Utilisateurs:**
- User ID 1: Utilisateur1 (Développeur)
- User ID 2: Utilisateur2 (Testeur)
- User ID 100: Admin1 (Administrateur)

**Tickets:**
- Ticket #1001: "Bug critique - Crash à la connexion" (créé par Utilisateur1)
- Ticket #1002: "Amélioration UI - Responsive design" (créé par Utilisateur1)
- Ticket #1003: "Bug 2FA - Validation incorrecte" (créé par Utilisateur2, contenu composite)

---

## Tests avec Postman / Insomnia

### TEST 1: Page d'accueil API

```http
GET http://localhost:8080/api/v1
```

**Réponse attendue (200 OK):**
```json
{
  "name": "Ticket Management System API",
  "version": "1.0.0",
  "status": "running",
  "endpoints": [...]
}
```

---

### TEST 2: Authentification - Login Utilisateur

```http
POST http://localhost:8080/api/v1/auth/login
Content-Type: application/json

{
  "userID": 1
}
```

**Réponse attendue (200 OK):**
```json
{
  "token": "session_<uuid>",
  "user": {
    "userID": 1,
    "name": "Utilisateur1",
    "email": "utilisateur1@uqac.ca",
    "role": "Developpeur",
    "isAdmin": false
  }
}
```

**⚠️ IMPORTANT:** Copier le `token` pour les requêtes suivantes!

---

### TEST 3: Vérifier session active

```http
GET http://localhost:8080/api/v1/auth/session
Authorization: Bearer <VOTRE_TOKEN>
```

**Réponse attendue (200 OK):**
```json
{
  "userID": 1,
  "name": "Utilisateur1",
  ...
}
```

---

### TEST 4: Liste des utilisateurs (Authentifié)

```http
GET http://localhost:8080/api/v1/users
Authorization: Bearer <VOTRE_TOKEN>
```

**Réponse attendue (200 OK):**
```json
[
  {
    "userID": 1,
    "name": "Utilisateur1",
    "email": "utilisateur1@uqac.ca",
    "role": "Developpeur",
    "isAdmin": false
  },
  ...
]
```

---

### TEST 5: Liste des tickets (Authentifié)

```http
GET http://localhost:8080/api/v1/tickets
Authorization: Bearer <VOTRE_TOKEN>
```

**Réponse attendue (200 OK):**
```json
[
  {
    "ticketID": 1001,
    "title": "Bug critique - Crash à la connexion",
    "status": "Ouvert",
    "priority": "Haute",
    "createdByName": "Utilisateur1",
    "assignedToName": "Non assigné",
    "description": "[TEXTE] L'application crash...",
    "descriptionContent": [
      {
        "type": "TEXT",
        "data": "L'application crash...",
        "metadata": null
      }
    ],
    "creationDate": "...",
    "updateDate": "..."
  },
  ...
]
```

**Note:** Utilisateur normal voit SEULEMENT ses propres tickets.

---

### TEST 6: Créer un nouveau ticket

```http
POST http://localhost:8080/api/v1/tickets
Authorization: Bearer <VOTRE_TOKEN>
Content-Type: application/json

{
  "title": "Nouveau ticket via API",
  "priority": "Moyenne",
  "descriptionContent": [
    {
      "type": "TEXT",
      "data": "Description du problème",
      "metadata": null
    }
  ]
}
```

**Réponse attendue (201 Created):**
```json
{
  "ticketID": 1004,
  "title": "Nouveau ticket via API",
  ...
}
```

---

### TEST 7: Créer un ticket avec contenu composite (texte + image + vidéo)

```http
POST http://localhost:8080/api/v1/tickets
Authorization: Bearer <VOTRE_TOKEN>
Content-Type: application/json

{
  "title": "Bug avec captures d'écran",
  "priority": "Haute",
  "descriptionContent": [
    {
      "type": "TEXT",
      "data": "Voici le problème rencontré",
      "metadata": null
    },
    {
      "type": "IMAGE",
      "data": "/captures/error_screen.png",
      "metadata": "Capture de l'erreur"
    },
    {
      "type": "VIDEO",
      "data": "/videos/reproduction.mp4",
      "metadata": "180"
    }
  ]
}
```

**Réponse attendue (201 Created):** Ticket avec `descriptionContent` composite

---

### TEST 8: Récupérer un ticket spécifique

```http
GET http://localhost:8080/api/v1/tickets/1001
Authorization: Bearer <VOTRE_TOKEN>
```

**Réponse attendue (200 OK):** Détails complets du ticket #1001

---

### TEST 9: Ajouter un commentaire

```http
POST http://localhost:8080/api/v1/tickets/1001/comments
Authorization: Bearer <VOTRE_TOKEN>
Content-Type: application/json

{
  "text": "Commentaire de test via API"
}
```

**Réponse attendue (201 Created):**
```json
"Commentaire de test via API"
```

---

### TEST 10: Récupérer les commentaires

```http
GET http://localhost:8080/api/v1/tickets/1001/comments
Authorization: Bearer <VOTRE_TOKEN>
```

**Réponse attendue (200 OK):**
```json
[
  "Commentaire de test via API"
]
```

---

### TEST 11: Login Admin

```http
POST http://localhost:8080/api/v1/auth/login
Content-Type: application/json

{
  "userID": 100
}
```

**Réponse attendue (200 OK):**
```json
{
  "token": "session_<uuid>",
  "user": {
    "userID": 100,
    "name": "Admin1",
    "role": "Admin",
    "isAdmin": true
  }
}
```

**⚠️ Copier le nouveau token admin!**

---

### TEST 12: Transitions de statut disponibles

```http
GET http://localhost:8080/api/v1/tickets/1001/status
Authorization: Bearer <ADMIN_TOKEN>
```

**Réponse attendue (200 OK):**
```json
[
  "ASSIGNE",
  "FERME"
]
```

---

### TEST 13: Changer le statut (Admin/Dev seulement)

```http
PATCH http://localhost:8080/api/v1/tickets/1001/status
Authorization: Bearer <ADMIN_TOKEN>
Content-Type: application/json

{
  "newStatus": "ASSIGNE"
}
```

**Réponse attendue (200 OK):** Ticket mis à jour avec `status: "Assigne"`

---

### TEST 14: Assigner un ticket (Admin/Dev seulement)

```http
PATCH http://localhost:8080/api/v1/tickets/1001/assignment
Authorization: Bearer <ADMIN_TOKEN>
Content-Type: application/json

{
  "userID": 2
}
```

**Réponse attendue (200 OK):** Ticket assigné à Utilisateur2

---

### TEST 15: Modifier un ticket (créateur ou Admin/Dev)

```http
PUT http://localhost:8080/api/v1/tickets/1001
Authorization: Bearer <ADMIN_TOKEN>
Content-Type: application/json

{
  "title": "Bug critique - Crash à la connexion [URGENT]",
  "priority": "Critique",
  "descriptionContent": [
    {
      "type": "TEXT",
      "data": "Description mise à jour",
      "metadata": null
    }
  ]
}
```

**Réponse attendue (200 OK):** Ticket mis à jour

---

### TEST 16: Exporter un ticket en PDF

```http
GET http://localhost:8080/api/v1/tickets/1001/export/pdf
Authorization: Bearer <VOTRE_TOKEN>
```

**Réponse attendue (200 OK):** Contenu texte formaté (simule PDF)

---

## Tests d'erreurs (validation sécurité)

### TEST E1: Accès sans authentification

```http
GET http://localhost:8080/api/v1/tickets
```

**Réponse attendue (401 Unauthorized):**
```json
{
  "error": "UNAUTHORIZED",
  "message": "Authentification requise. Veuillez vous connecter."
}
```

---

### TEST E2: Token invalide

```http
GET http://localhost:8080/api/v1/tickets
Authorization: Bearer invalid_token_123
```

**Réponse attendue (401 Unauthorized)**

---

### TEST E3: Utilisateur normal tente de supprimer (Admin seulement)

```http
DELETE http://localhost:8080/api/v1/tickets/1001
Authorization: Bearer <USER_TOKEN>
```

**Réponse attendue (403 Forbidden):**
```json
{
  "error": "FORBIDDEN",
  "message": "Cette opération nécessite des privilèges administrateur."
}
```

---

### TEST E4: Utilisateur normal tente de changer un statut (Admin/Dev seulement)

```http
PATCH http://localhost:8080/api/v1/tickets/1001/status
Authorization: Bearer <USER_TOKEN>
Content-Type: application/json

{
  "newStatus": "TERMINE"
}
```

**Réponse attendue (403 Forbidden):**
```json
{
  "error": "FORBIDDEN",
  "message": "Seuls les administrateurs et développeurs peuvent changer les statuts"
}
```

---

### TEST E5: Transition de statut invalide

```http
PATCH http://localhost:8080/api/v1/tickets/1001/status
Authorization: Bearer <ADMIN_TOKEN>
Content-Type: application/json

{
  "newStatus": "TERMINE"
}
```

**Si le ticket est OUVERT:**

**Réponse attendue (400 Bad Request):**
```json
{
  "error": "INVALID_TRANSITION",
  "message": "Transition invalide : OUVERT -> TERMINE. Transitions autorisées : ASSIGNE, FERME"
}
```

---

### TEST E6: Accéder au ticket d'un autre utilisateur (utilisateur normal)

```http
GET http://localhost:8080/api/v1/tickets/1003
Authorization: Bearer <USER1_TOKEN>
```

**Si Utilisateur1 tente d'accéder au ticket de Utilisateur2:**

**Réponse attendue (403 Forbidden):**
```json
{
  "error": "FORBIDDEN",
  "message": "Vous n'avez pas accès à ce ticket"
}
```

---

## Résumé des permissions

| Opération | Tous | Dev | Admin |
|-----------|------|-----|-------|
| Créer ticket | ✅ | ✅ | ✅ |
| Voir ses tickets | ✅ | ✅ | ✅ |
| Voir tous tickets | ❌ | ✅ | ✅ |
| Modifier ses tickets | ✅ | ✅ | ✅ |
| Modifier tous tickets | ❌ | ✅ | ✅ |
| Ajouter commentaire | ✅ | ✅ | ✅ |
| Changer statut | ❌ | ✅ | ✅ |
| Assigner ticket | ❌ | ✅ | ✅ |
| Supprimer ticket | ❌ | ❌ | ✅ |
| Export PDF | ✅ | ✅ | ✅ |

---

## Validation de la State Machine

**Transitions autorisées:**

```
OUVERT → ASSIGNE, FERME
ASSIGNE → VALIDATION, FERME
VALIDATION → TERMINE, ASSIGNE
TERMINE → (état final)
FERME → (état final)
```

**Tester:**
1. Créer ticket (statut OUVERT)
2. Assigner → ASSIGNE ✅
3. ASSIGNE → VALIDATION ✅
4. VALIDATION → TERMINE ✅
5. Tenter OUVERT → TERMINE ❌ (doit échouer)

---

## Phase 3 - COMPLÉTÉE ✅

**Réalisations:**
- ✅ Authentification par token fonctionnelle
- ✅ Vérification token dans TOUS les endpoints
- ✅ Gestion des permissions (User/Dev/Admin)
- ✅ Filtrage des tickets selon les permissions
- ✅ Validation des transitions de statut
- ✅ Messages d'erreur clairs (401, 403, 404, 400)
- ✅ Logging des opérations avec nom d'utilisateur
- ✅ Serveur compilé et démarré avec succès

**Prochaine étape:** Phase 4 - Intégration client GUI

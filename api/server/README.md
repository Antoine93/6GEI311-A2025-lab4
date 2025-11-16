# Serveur API REST - Système de Gestion de Tickets

## Dépendances

Le serveur nécessite **Gson** pour la sérialisation/désérialisation JSON.

### Option 1 : Télécharger Gson manuellement

1. Télécharger `gson-2.10.1.jar` depuis [Maven Central](https://repo1.maven.org/maven2/com/google/code/gson/gson/2.10.1/gson-2.10.1.jar)
2. Placer le fichier dans `api/server/lib/`

### Option 2 : Utiliser Maven ou Gradle

**Maven (pom.xml)** :
```xml
<dependency>
    <groupId>com.google.code.gson</groupId>
    <artifactId>gson</artifactId>
    <version>2.10.1</version>
</dependency>
```

**Gradle (build.gradle)** :
```gradle
implementation 'com.google.code.gson:gson:2.10.1'
```

## Compilation

### Windows (PowerShell)

```powershell
# Créer le répertoire classes si nécessaire
if (!(Test-Path classes)) { New-Item -ItemType Directory -Path classes }

# Compiler (avec Gson dans le classpath) - Version multilignes avec backtick
javac -encoding UTF-8 -cp "api/server/lib/gson-2.10.1.jar" -d classes `
  api/server/models/*.java `
  api/server/services/*.java `
  api/server/resources/*.java `
  api/server/TicketAPIServer.java `
  core/entities/*.java `
  core/content/*.java `
  core/exporter/*.java
```

### Linux/macOS (Bash)

```bash
# Créer le répertoire classes si nécessaire
mkdir -p classes

# Compiler (avec Gson dans le classpath)
javac -encoding UTF-8 -cp "api/server/lib/gson-2.10.1.jar" -d classes \
  api/server/models/*.java \
  api/server/services/*.java \
  api/server/resources/*.java \
  api/server/TicketAPIServer.java \
  core/entities/*.java \
  core/content/*.java \
  core/exporter/*.java
```

## Exécution

### Windows (PowerShell)
```cmd
java -cp "classes;api\server\lib\gson-2.10.1.jar" api.server.TicketAPIServer
```

Le serveur démarre sur `http://localhost:8080/api/v1`

### Linux/macOS (Bash)

```bash
# Démarrer le serveur (avec Gson dans le classpath)
java -cp "classes:api/server/lib/gson-2.10.1.jar" api.server.TicketAPIServer
```

## Test rapide

```bash
# Page d'accueil de l'API (informations générales)
curl http://localhost:8080/api/v1

# Tester l'authentification
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"userID": 1}'

# Lister tous les tickets
curl http://localhost:8080/api/v1/tickets

# Obtenir un ticket spécifique
curl http://localhost:8080/api/v1/tickets/1001
```

## Architecture

```
api/server/
├── models/                 # DTOs (POJOs pour JSON)
│   ├── UserDTO.java
│   ├── TicketDTO.java
│   ├── ContentItemDTO.java
│   ├── CreateTicketRequest.java
│   ├── UpdateTicketRequest.java
│   ├── CommentRequest.java
│   ├── StatusUpdateDTO.java
│   ├── AssignmentDTO.java
│   ├── LoginRequest.java
│   ├── AuthResponse.java
│   └── ErrorResponse.java
├── resources/              # HTTP Handlers
│   ├── BaseResource.java
│   ├── ApiHomeResource.java
│   ├── AuthResource.java
│   ├── UserResource.java
│   └── TicketResource.java
├── services/               # Logique métier
│   └── ApplicationState.java
└── TicketAPIServer.java    # Point d'entrée du serveur
```

## Endpoints disponibles

### Page d'accueil
- `GET /api/v1` - Informations générales sur l'API (nom, version, status)

### Authentification
- `POST /api/v1/auth/login` - Connexion utilisateur
- `GET /api/v1/auth/session` - Vérifier session active
- `POST /api/v1/auth/logout` - Déconnexion

### Utilisateurs
- `GET /api/v1/users` - Liste tous les utilisateurs
- `GET /api/v1/users/{id}` - Détails d'un utilisateur

### Tickets
- `GET /api/v1/tickets` - Liste tous les tickets (avec filtres optionnels)
- `POST /api/v1/tickets` - Créer un nouveau ticket
- `GET /api/v1/tickets/{id}` - Détails d'un ticket
- `PUT /api/v1/tickets/{id}` - Modifier un ticket
- `DELETE /api/v1/tickets/{id}` - Supprimer un ticket
- `GET /api/v1/tickets/{id}/comments` - Commentaires d'un ticket
- `POST /api/v1/tickets/{id}/comments` - Ajouter un commentaire
- `PATCH /api/v1/tickets/{id}/status` - Changer le statut
- `GET /api/v1/tickets/{id}/status` - Obtenir le statut actuel
- `PATCH /api/v1/tickets/{id}/assignment` - Assigner à un utilisateur
- `GET /api/v1/tickets/{id}/export/pdf` - Exporter en PDF
```

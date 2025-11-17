# GUI - Interface graphique

## Démarrage rapide

### 1. Compiler
```bash
javac -encoding UTF-8 -cp "api/server/lib/*;classes" -d classes core/**/*.java api/server/**/*.java gui/**/*.java MainGUI_REST.java
```

### 2. Démarrer le serveur (Terminal 1)
```bash
java -cp "classes;api/server/lib/*" api.server.TicketAPIServer
```

### 3. Lancer l'interface (Terminal 2)
```bash
java -cp "classes;api/server/lib/*" MainGUI_REST
```

## Connexion

Entrez un ID utilisateur :
- `1` - Utilisateur1 (Développeur)
- `2` - Utilisateur2 (Testeur)
- `100` - Admin1 (Administrateur)

## Fonctionnalités

- Liste des tickets
- Créer un ticket
- Ajouter un commentaire
- Changer le statut
- Assigner un ticket
- Exporter en PDF

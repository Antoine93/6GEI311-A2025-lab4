# Interface Web - Système de Gestion de Tickets

Interface web simple développée en HTML/CSS/JavaScript pour démontrer l'intégration avec l'API REST.

## 📁 Fichiers

```
web/
├── index.html      # Structure HTML de l'interface
├── style.css       # Styles CSS (design moderne)
├── api.js          # Client API REST (communication avec le serveur)
├── app.js          # Logique de l'application
└── README.md       # Ce fichier
```

## ✨ Fonctionnalités implémentées

### Authentification
- ✅ Connexion par ID utilisateur (1, 2, ou 100)
- ✅ Gestion du token Bearer dans localStorage
- ✅ Déconnexion

### Gestion des tickets
- ✅ Affichage de la liste des tickets (filtrés selon permissions)
- ✅ Création de nouveaux tickets
- ✅ Affichage des détails d'un ticket
- ✅ Filtrage par statut
- ✅ Sélection d'un ticket pour voir les détails

### Actions sur les tickets
- ✅ Ajout de commentaires
- ✅ Changement de statut (avec validation des transitions)
- ✅ Assignation à un utilisateur
- ✅ Export PDF (téléchargement du fichier texte)
- ✅ Suppression (admin uniquement)

### Interface utilisateur
- ✅ Design moderne et responsive
- ✅ Notifications de succès/erreur
- ✅ Modals pour les formulaires
- ✅ Badges colorés pour statuts et priorités
- ✅ Affichage conditionnel des boutons selon permissions

## 🚀 Utilisation

### 1. Démarrer le serveur REST

Assurez-vous que le serveur REST est démarré sur le port 8080:

```powershell
# Dans le dossier racine du projet
java -cp "lib/gson-2.10.1.jar;classes" api.server.TicketAPIServer
```

### 2. Ouvrir l'interface web

Ouvrez simplement le fichier `index.html` dans votre navigateur:

```powershell
# Option 1: Double-clic sur index.html
# Option 2: Depuis le terminal
start index.html

# Ou avec un serveur HTTP local (recommandé)
python -m http.server 8000
# Puis ouvrir http://localhost:8000
```

### 3. Se connecter

Entrez un des IDs utilisateur disponibles:
- **1** - Utilisateur1 (Développeur) - Voit ses propres tickets
- **2** - Utilisateur2 (Testeur) - Voit ses propres tickets
- **100** - Admin1 (Admin) - Voit tous les tickets + peut supprimer

## 🎨 Fonctionnalités de l'interface

### Page de connexion
![Login](docs/login.png)
- Saisie de l'ID utilisateur
- Validation et gestion des erreurs

### Page principale
![Main](docs/main.png)
- **Header**: Affiche l'utilisateur connecté et bouton déconnexion
- **Toolbar**: Boutons d'action (Nouveau ticket, Rafraîchir, Filtre par statut)
- **Liste des tickets**: Cartes cliquables avec badge statut/priorité
- **Détails du ticket**: Panneau détaillé à droite avec toutes les infos et commentaires
- **Actions**: Boutons pour ajouter commentaire, changer statut, assigner, exporter, supprimer

### Modals interactifs
- **Nouveau ticket**: Formulaire avec titre, priorité, description
- **Ajouter commentaire**: Zone de texte pour le commentaire
- **Changer statut**: Liste déroulante des transitions disponibles
- **Assigner**: Liste déroulante des utilisateurs

## 🔧 Architecture technique

### api.js - Client API REST

Classe `ApiClient` qui encapsule toutes les communications avec l'API:

```javascript
const api = new ApiClient();

// Authentification
await api.login(userID);
await api.logout();

// Tickets
await api.getAllTickets(filters);
await api.getTicketById(ticketID);
await api.createTicket(title, priority, content);
await api.updateTicket(ticketID, ...);
await api.deleteTicket(ticketID);

// Commentaires
await api.getTicketComments(ticketID);
await api.addComment(ticketID, text);

// Statuts
await api.getAvailableTransitions(ticketID);
await api.changeTicketStatus(ticketID, newStatus);

// Assignation
await api.assignTicket(ticketID, userID);

// Export
await api.exportTicketToPDF(ticketID);
```

### app.js - Logique de l'application

Gestion de:
- Initialisation et routing (login vs main page)
- Event listeners sur tous les boutons/formulaires
- Chargement et affichage des données
- Gestion des modals
- Notifications (succès/erreur)

### style.css - Design moderne

- Variables CSS pour thème cohérent
- Design responsive (mobile-friendly)
- Animations et transitions
- Badges colorés par type
- Cards avec hover effects

## 📡 Communication avec l'API

### Headers HTTP

Toutes les requêtes (sauf `/auth/login`) incluent:
```
Authorization: Bearer <token>
Content-Type: application/json
```

### Gestion des erreurs

```javascript
try {
    const result = await api.someOperation();
    showSuccess('Opération réussie!');
} catch (error) {
    showError('Erreur: ' + error.message);
}
```

### Persistence

Le token d'authentification est stocké dans `localStorage`:
```javascript
localStorage.setItem('authToken', token);
localStorage.getItem('authToken');
```

## 🎯 Tests réalisés

### Scénario de test complet

1. ✅ **Login** avec ID 1 (Développeur)
2. ✅ **Affichage** de la liste des tickets
3. ✅ **Création** d'un nouveau ticket
4. ✅ **Sélection** d'un ticket pour voir les détails
5. ✅ **Ajout** d'un commentaire
6. ✅ **Changement** de statut (Ouvert → Assigné)
7. ✅ **Assignation** à un autre utilisateur
8. ✅ **Export** PDF (téléchargement)
9. ✅ **Filtrage** par statut
10. ✅ **Déconnexion** et reconnexion avec ID 100 (Admin)
11. ✅ **Suppression** d'un ticket (admin)

## 🌐 Compatibilité navigateurs

Testé et fonctionnel sur:
- ✅ Chrome/Edge (recommandé)
- ✅ Firefox
- ✅ Safari

**Note**: Nécessite un navigateur moderne supportant:
- ES6+ (async/await, arrow functions, classes)
- Fetch API
- localStorage

## 🔐 Sécurité

- Token Bearer stocké en localStorage (session persistante)
- Validation côté serveur pour toutes les opérations
- Permissions respectées (Admin vs Utilisateur normal)
- Pas de données sensibles exposées dans le code

## 📝 Limites et améliorations possibles

### Limites actuelles
- Pas de validation formulaire côté client (validé côté serveur)
- Pas de pagination pour les grandes listes
- Pas de recherche/tri dans la liste
- Export PDF simple (texte brut)

### Améliorations possibles
- WebSockets pour mises à jour en temps réel
- Upload de fichiers pour les images/vidéos
- Rich text editor pour la description
- Graphiques et statistiques
- Mode sombre
- Internationalisation (i18n)

## 🏆 Bonus Lab 4

Cette interface web démontre:
1. ✅ **Intégration complète** avec l'API REST
2. ✅ **Interface simple et fonctionnelle**
3. ✅ **Toutes les opérations CRUD** implémentées
4. ✅ **Respect des permissions** (Admin vs User)
5. ✅ **Design moderne et responsive**
6. ✅ **Gestion d'erreurs robuste**

---

*Interface web développée pour le Lab 4 - 6GEI311 A2025*
*Bonus: Interface web simple intégrée avec l'API REST*

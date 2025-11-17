package gui.controllers;

import gui.models.*;
import gui.services.*;
import java.util.*;

/**
 * TicketControllerREST
 * Version REST du TicketController qui utilise l'API REST au lieu d'ApplicationState local
 *
 * Délègue toutes les opérations à ITicketService (RestTicketService)
 *
 * Responsabilités:
 * - Fournir une interface simplifiée pour la GUI
 * - Gérer les exceptions de service et les convertir en messages d'erreur
 * - Maintenir la cohérence avec l'ancien TicketController (compatibilité)
 */
public class TicketControllerREST {
    private ITicketService ticketService;

    public TicketControllerREST() {
        this.ticketService = new RestTicketService();
    }

    /**
     * Constructeur avec service personnalisé (pour tests)
     */
    public TicketControllerREST(ITicketService ticketService) {
        this.ticketService = ticketService;
    }

    // ========================================================================
    // Authentification
    // ========================================================================

    /**
     * Authentifie un utilisateur
     * @param userID ID de l'utilisateur
     * @return UserDTO de l'utilisateur connecté ou null si échec
     */
    public UserDTO login(int userID) {
        try {
            return ticketService.login(userID);
        } catch (ServiceException e) {
            System.err.println("[ERROR] Login échoué: " + e.getMessage());
            return null;
        }
    }

    /**
     * Déconnecte l'utilisateur actuel
     */
    public void logout() {
        try {
            ticketService.logout();
        } catch (ServiceException e) {
            System.err.println("[ERROR] Logout échoué: " + e.getMessage());
        }
    }

    /**
     * Retourne l'utilisateur actuellement connecté
     */
    public UserDTO getCurrentUser() {
        return ticketService.getCurrentUser();
    }

    /**
     * Vérifie si un utilisateur est connecté
     */
    public boolean isAuthenticated() {
        return ticketService.isAuthenticated();
    }

    // ========================================================================
    // Utilisateurs
    // ========================================================================

    /**
     * Retourne tous les utilisateurs
     */
    public List<UserDTO> getAllUsers() {
        try {
            return ticketService.getAllUsers();
        } catch (ServiceException e) {
            System.err.println("[ERROR] Erreur lors de la récupération des utilisateurs: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Change l'utilisateur actuel (pour compatibilité - non utilisé en mode REST)
     * En mode REST, l'utilisateur est défini par le login
     */
    public void setCurrentUser(int userID) {
        // En mode REST, appeler login() au lieu de setCurrentUser()
        login(userID);
    }

    // ========================================================================
    // Tickets (CRUD)
    // ========================================================================

    /**
     * Crée un nouveau ticket avec une liste de ContentItemDTO
     * @return ID du ticket créé, ou -1 si échec
     */
    public int createTicketWithContentItems(String title, List<ContentItemDTO> contentItems, String priority) {
        try {
            TicketDTO createdTicket = ticketService.createTicket(title, contentItems, priority);
            return createdTicket.getTicketID();
        } catch (ServiceException e) {
            System.err.println("[ERROR] Erreur lors de la création du ticket: " + e.getMessage());
            // Afficher un message utilisateur plus clair
            if (e.isValidationError()) {
                System.err.println("  → Erreur de validation: " + e.getMessage());
            } else if (e.isAuthenticationError()) {
                System.err.println("  → Non authentifié. Veuillez vous reconnecter.");
            }
            return -1;
        }
    }

    /**
     * Récupère tous les tickets (filtrés selon permissions côté serveur)
     */
    public List<TicketDTO> getAllTickets() {
        try {
            return ticketService.getAllTickets();
        } catch (ServiceException e) {
            System.err.println("[ERROR] Erreur lors de la récupération des tickets: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Récupère les tickets filtrés (pour compatibilité - délègue à getAllTickets)
     * Le filtrage est fait côté serveur selon les permissions
     */
    public List<TicketDTO> getFilteredTickets() {
        return getAllTickets();
    }

    /**
     * Récupère un ticket par son ID
     */
    public TicketDTO getTicketById(int ticketID) {
        try {
            return ticketService.getTicketById(ticketID);
        } catch (ServiceException e) {
            System.err.println("[ERROR] Erreur lors de la récupération du ticket #" + ticketID + ": " + e.getMessage());
            return null;
        }
    }

    /**
     * Modifie un ticket avec une liste de ContentItemDTO
     */
    public void updateTicketWithContentItems(int ticketID, String title, String priority, List<ContentItemDTO> contentItems) {
        try {
            ticketService.updateTicket(ticketID, title, priority, contentItems);
        } catch (ServiceException e) {
            System.err.println("[ERROR] Erreur lors de la modification du ticket: " + e.getMessage());
            if (e.isPermissionError()) {
                System.err.println("  → Vous n'avez pas les permissions pour modifier ce ticket.");
            }
        }
    }

    /**
     * Supprime un ticket (Admin seulement)
     */
    public void deleteTicket(int ticketID) {
        try {
            ticketService.deleteTicket(ticketID);
        } catch (ServiceException e) {
            System.err.println("[ERROR] Erreur lors de la suppression du ticket: " + e.getMessage());
            if (e.isPermissionError()) {
                System.err.println("  → Seuls les administrateurs peuvent supprimer des tickets.");
            }
        }
    }

    // ========================================================================
    // Commentaires
    // ========================================================================

    /**
     * Récupère les commentaires d'un ticket
     */
    public List<String> getTicketComments(int ticketID) {
        try {
            return ticketService.getTicketComments(ticketID);
        } catch (ServiceException e) {
            System.err.println("[ERROR] Erreur lors de la récupération des commentaires: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Ajoute un commentaire à un ticket
     */
    public void addComment(int ticketID, String comment) {
        try {
            ticketService.addComment(ticketID, comment);
        } catch (ServiceException e) {
            System.err.println("[ERROR] Erreur lors de l'ajout du commentaire: " + e.getMessage());
        }
    }

    // ========================================================================
    // Gestion du statut
    // ========================================================================

    /**
     * Retourne les transitions possibles pour un ticket
     */
    public List<String> getAvailableTransitions(int ticketID) {
        try {
            return ticketService.getAvailableTransitions(ticketID);
        } catch (ServiceException e) {
            System.err.println("[ERROR] Erreur lors de la récupération des transitions: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Change le statut d'un ticket
     */
    public void changeTicketStatus(int ticketID, String newStatusStr) {
        try {
            ticketService.changeTicketStatus(ticketID, newStatusStr);
        } catch (ServiceException e) {
            System.err.println("[ERROR] Erreur lors du changement de statut: " + e.getMessage());
            if (e.isValidationError()) {
                System.err.println("  → Transition invalide. " + e.getMessage());
            } else if (e.isPermissionError()) {
                System.err.println("  → Seuls les admins et développeurs peuvent changer les statuts.");
            }
        }
    }

    // ========================================================================
    // Assignation
    // ========================================================================

    /**
     * Assigne un ticket à un utilisateur
     */
    public void assignTicket(int ticketID, int userID) {
        try {
            ticketService.assignTicket(ticketID, userID);
        } catch (ServiceException e) {
            System.err.println("[ERROR] Erreur lors de l'assignation: " + e.getMessage());
            if (e.isPermissionError()) {
                System.err.println("  → Seuls les admins et développeurs peuvent assigner des tickets.");
            }
        }
    }

    // ========================================================================
    // Export
    // ========================================================================

    /**
     * Exporte un ticket en format texte (PDF simulé)
     */
    public String exportTicketToText(int ticketID) {
        try {
            return ticketService.exportTicketToPDF(ticketID);
        } catch (ServiceException e) {
            System.err.println("[ERROR] Erreur lors de l'export: " + e.getMessage());
            return "";
        }
    }

    /**
     * Retourne les détails complets d'un ticket (pour affichage)
     */
    public String getTicketDetails(int ticketID) {
        try {
            TicketDTO ticket = ticketService.getTicketById(ticketID);
            if (ticket == null) {
                return "Ticket #" + ticketID + " non trouvé.";
            }

            StringBuilder details = new StringBuilder();
            details.append("Ticket #").append(ticket.getTicketID()).append("\n\n");
            details.append("Titre: ").append(ticket.getTitle()).append("\n");
            details.append("Statut: ").append(ticket.getStatus()).append("\n");
            details.append("Priorité: ").append(ticket.getPriority()).append("\n");
            details.append("Créé par: ").append(ticket.getCreatedByName()).append("\n");
            details.append("Assigné à: ").append(ticket.getAssignedToName()).append("\n");
            details.append("Créé le: ").append(ticket.getCreationDate()).append("\n\n");

            details.append("Description:\n");
            details.append(ticket.getDescription()).append("\n\n");

            // Récupérer les commentaires
            List<String> comments = getTicketComments(ticketID);
            if (!comments.isEmpty()) {
                details.append("Commentaires (").append(comments.size()).append("):\n");
                int i = 1;
                for (String comment : comments) {
                    details.append("[").append(i++).append("] ").append(comment).append("\n");
                }
            }

            return details.toString();

        } catch (ServiceException e) {
            System.err.println("[ERROR] Erreur lors de la récupération des détails: " + e.getMessage());
            return "Erreur lors de la récupération du ticket.";
        }
    }
}

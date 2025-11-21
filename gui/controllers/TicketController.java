package gui.controllers;

import gui.models.*;
import gui.services.RestApiClient;
import java.io.IOException;
import java.util.*;

/**
 * TicketController
 * Gère la logique métier liée aux tickets en utilisant l'API REST
 * Pont entre la View (qui utilise des DTOs) et l'API REST
 *
 * REFACTORISÉ (Lab 4) :
 * - Remplace l'accès direct à ApplicationState par des appels REST via RestApiClient
 * - Toutes les opérations passent par le serveur REST
 * - Gestion des erreurs réseau et HTTP
 *
 * Responsabilités:
 * - Encapsuler les appels au RestApiClient
 * - Gérer les erreurs et exceptions réseau
 * - Maintenir une interface cohérente pour la View
 */
public class TicketController {
    private RestApiClient apiClient;
    private UserDTO currentUser;

    public TicketController() {
        this.apiClient = RestApiClient.getInstance();
        this.currentUser = null;
    }

    /**
     * Authentifie un utilisateur via l'API REST
     * @return true si succès, false sinon
     */
    public boolean login(int userID) {
        try {
            this.currentUser = apiClient.login(userID);
            return true;
        } catch (IOException e) {
            System.err.println("Erreur de connexion: " + e.getMessage());
            return false;
        }
    }

    /**
     * Déconnexion
     */
    public void logout() {
        try {
            apiClient.logout();
            this.currentUser = null;
        } catch (IOException e) {
            System.err.println("Erreur de déconnexion: " + e.getMessage());
        }
    }

    /**
     * Retourne l'utilisateur actuel
     */
    public UserDTO getCurrentUser() {
        return currentUser;
    }

    /**
     * Vérifie si un utilisateur est connecté
     */
    public boolean isAuthenticated() {
        return currentUser != null && apiClient.isAuthenticated();
    }

    /**
     * Retourne tous les utilisateurs
     */
    public List<UserDTO> getAllUsers() {
        try {
            return apiClient.getAllUsers();
        } catch (IOException e) {
            System.err.println("Erreur lors de la récupération des utilisateurs: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Change l'utilisateur actuel (réauthentification)
     */
    public void setCurrentUser(int userID) {
        login(userID);
    }

    /**
     * Crée un nouveau ticket avec une liste de ContentItemDTO
     */
    public int createTicketWithContentItems(String title, List<ContentItemDTO> contentItems, String priority) {
        try {
            TicketDTO createdTicket = apiClient.createTicket(title, priority, contentItems);
            return createdTicket.getTicketID();
        } catch (IOException e) {
            System.err.println("Erreur lors de la création du ticket: " + e.getMessage());
            return -1;
        }
    }

    /**
     * Récupère tous les tickets
     * Les tickets sont automatiquement filtrés côté serveur selon les permissions
     */
    public List<TicketDTO> getAllTickets() {
        try {
            return apiClient.getAllTickets();
        } catch (IOException e) {
            System.err.println("Erreur lors de la récupération des tickets: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Récupère les tickets filtrés selon l'utilisateur actuel
     * Note: Le filtrage est maintenant fait côté serveur
     */
    public List<TicketDTO> getFilteredTickets() {
        // Le serveur filtre automatiquement selon les permissions
        return getAllTickets();
    }

    /**
     * Récupère un ticket par son ID
     */
    public TicketDTO getTicketById(int ticketID) {
        try {
            return apiClient.getTicketById(ticketID);
        } catch (IOException e) {
            System.err.println("Erreur lors de la récupération du ticket #" + ticketID + ": " + e.getMessage());
            return null;
        }
    }

    /**
     * Assigne un ticket à un utilisateur
     */
    public void assignTicket(int ticketID, int userID) {
        try {
            apiClient.assignTicket(ticketID, userID);
        } catch (IOException e) {
            System.err.println("Erreur lors de l'assignation du ticket #" + ticketID + ": " + e.getMessage());
        }
    }

    /**
     * Change le statut d'un ticket
     */
    public void changeTicketStatus(int ticketID, String newStatus) {
        try {
            // Convertir le statut d'affichage en format API si nécessaire
            String apiStatus = convertToApiStatus(newStatus);
            apiClient.changeTicketStatus(ticketID, apiStatus);
        } catch (IOException e) {
            System.err.println("Erreur lors du changement de statut du ticket #" + ticketID + ": " + e.getMessage());
        }
    }

    /**
     * Retourne les transitions possibles pour un ticket
     */
    public List<String> getAvailableTransitions(int ticketID) {
        try {
            return apiClient.getAvailableTransitions(ticketID);
        } catch (IOException e) {
            System.err.println("Erreur lors de la récupération des transitions pour le ticket #" + ticketID + ": " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Ajoute un commentaire à un ticket
     */
    public void addComment(int ticketID, String comment) {
        try {
            apiClient.addComment(ticketID, comment);
        } catch (IOException e) {
            System.err.println("Erreur lors de l'ajout du commentaire au ticket #" + ticketID + ": " + e.getMessage());
        }
    }

    /**
     * Récupère les commentaires d'un ticket
     */
    public List<String> getTicketComments(int ticketID) {
        try {
            return apiClient.getTicketComments(ticketID);
        } catch (IOException e) {
            System.err.println("Erreur lors de la récupération des commentaires du ticket #" + ticketID + ": " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Modifie un ticket avec une liste de ContentItemDTO
     */
    public void updateTicket(int ticketID, String title, String priority, List<ContentItemDTO> contentItems) {
        try {
            apiClient.updateTicket(ticketID, title, priority, contentItems);
        } catch (IOException e) {
            System.err.println("Erreur lors de la modification du ticket #" + ticketID + ": " + e.getMessage());
        }
    }

    /**
     * @deprecated Utilisez updateTicket() avec List<ContentItemDTO> à la place
     */
    @Deprecated
    public void updateTicketWithContentItems(int ticketID, String title, String priority, List<ContentItemDTO> contentItems) {
        updateTicket(ticketID, title, priority, contentItems);
    }

    /**
     * Exporte un ticket en format PDF (texte)
     */
    public String exportTicketToText(int ticketID) {
        try {
            return apiClient.exportTicketToPDF(ticketID);
        } catch (IOException e) {
            System.err.println("Erreur lors de l'export du ticket #" + ticketID + ": " + e.getMessage());
            return "";
        }
    }

    /**
     * Retourne les détails complets d'un ticket (pour affichage)
     */
    public String getTicketDetails(int ticketID) {
        try {
            TicketDTO ticket = apiClient.getTicketById(ticketID);
            if (ticket == null) return "";

            StringBuilder details = new StringBuilder();
            details.append("Ticket #").append(ticket.getTicketID()).append("\n\n");
            details.append("Titre: ").append(ticket.getTitle()).append("\n");
            details.append("Statut: ").append(ticket.getStatus()).append("\n");
            details.append("Priorite: ").append(ticket.getPriority()).append("\n");
            details.append("Cree le: ").append(ticket.getCreationDate()).append("\n");

            if (ticket.getAssignedToName() != null && !ticket.getAssignedToName().isEmpty()) {
                details.append("Assigne a: ").append(ticket.getAssignedToName()).append("\n");
            } else {
                details.append("Assigne a: Non assigne\n");
            }

            details.append("\nDescription:\n");
            details.append(ticket.getDescription());

            // Récupérer les commentaires via l'API
            List<String> comments = getTicketComments(ticketID);
            if (!comments.isEmpty()) {
                details.append("\n\nCommentaires (").append(comments.size()).append("):\n");
                int i = 1;
                for (String comment : comments) {
                    details.append("[").append(i++).append("] ").append(comment).append("\n");
                }
            }

            return details.toString();
        } catch (IOException e) {
            System.err.println("Erreur lors de la récupération des détails du ticket #" + ticketID + ": " + e.getMessage());
            return "Erreur lors du chargement des détails du ticket.";
        }
    }

    /**
     * Supprime un ticket (admin seulement)
     */
    public boolean deleteTicket(int ticketID) {
        try {
            apiClient.deleteTicket(ticketID);
            return true;
        } catch (IOException e) {
            System.err.println("Erreur lors de la suppression du ticket #" + ticketID + ": " + e.getMessage());
            return false;
        }
    }

    // ========== Méthodes utilitaires privées ==========

    /**
     * Convertit un nom de statut d'affichage en format API
     */
    private String convertToApiStatus(String displayStatus) {
        // Mapper les noms d'affichage vers les noms API
        switch (displayStatus) {
            case "Ouvert":
                return "OUVERT";
            case "Assigne":
                return "ASSIGNE";
            case "En validation":
                return "VALIDATION";
            case "Termine":
                return "TERMINE";
            case "Ferme":
                return "FERME";
            default:
                return displayStatus;
        }
    }

    /**
     * Récupère le nom d'un utilisateur par son ID
     */
    public String getUserNameById(Integer userID) {
        if (userID == null) {
            return "Non assigne";
        }

        try {
            UserDTO user = apiClient.getUserById(userID);
            return user != null ? user.getName() : "User #" + userID;
        } catch (IOException e) {
            return "User #" + userID;
        }
    }
}

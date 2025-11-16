package gui.controllers;

import gui.models.*;
import java.util.*;

import core.content.*;
import core.entities.*;

/**
 * TicketController
 * Gere la logique metier liee aux tickets
 * Pont entre la View (qui utilise des DTOs) et le Model (entites domain)
 *
 * Responsabilites:
 * - Convertir les entites domain en DTOs pour le View
 * - Convertir les donnees du View en operations sur les entites domain
 * - Gerer la logique metier (filtrage, permissions, etc.)
 */
public class TicketController {
    private ApplicationState state;

    public TicketController() {
        this.state = ApplicationState.getInstance();
    }

    /**
     * Retourne l'utilisateur actuel sous forme de DTO
     */
    public UserDTO getCurrentUser() {
        User user = state.getCurrentUser();
        return convertToUserDTO(user);
    }

    /**
     * Retourne tous les utilisateurs sous forme de DTOs
     */
    public List<UserDTO> getAllUsers() {
        List<UserDTO> dtos = new ArrayList<>();
        for (User user : state.getAllUsers()) {
            dtos.add(convertToUserDTO(user));
        }
        return dtos;
    }

    /**
     * Change l'utilisateur actuel
     */
    public void setCurrentUser(int userID) {
        List<User> users = state.getAllUsers();
        for (User user : users) {
            if (user.getUserID() == userID) {
                state.setCurrentUser(user);
                return;
            }
        }
    }

    /**
     * Cree un nouveau ticket avec un contenu specifique
     */
    public int createTicketWithContent(String title, Content content, String priority) {
        User currentUser = state.getCurrentUser();
        Ticket ticket = currentUser.createTicket(title, content, priority);
        state.addTicket(ticket);
        return ticket.getTicketID();
    }

    /**
     * NOUVEAU: Cree un nouveau ticket avec une liste de ContentItemDTO
     * Cette méthode convertit les DTOs en objets métier
     */
    public int createTicketWithContentItems(String title, List<ContentItemDTO> contentItems, String priority) {
        User currentUser = state.getCurrentUser();

        // Le Controller crée les objets métier à partir des DTOs
        Content content = buildContentFromDTOs(contentItems);

        Ticket ticket = currentUser.createTicket(title, content, priority);
        state.addTicket(ticket);
        return ticket.getTicketID();
    }

    /**
     * NOUVEAU: Construit un objet Content à partir d'une liste de ContentItemDTO
     */
    private Content buildContentFromDTOs(List<ContentItemDTO> items) {
        if (items == null || items.isEmpty()) {
            return new TextContent("");
        }

        if (items.size() == 1) {
            return createSingleContent(items.get(0));
        }

        // Plusieurs items: créer un composite
        CompositeContent composite = new CompositeContent();
        for (ContentItemDTO item : items) {
            composite.add(createSingleContent(item));
        }
        return composite;
    }

    /**
     * NOUVEAU: Crée un objet Content simple à partir d'un ContentItemDTO
     */
    private Content createSingleContent(ContentItemDTO dto) {
        switch (dto.getType()) {
            case TEXT:
                return new TextContent(dto.getData());

            case IMAGE:
                return new ImageContent(dto.getData(), dto.getMetadata());

            case VIDEO:
                int duration = 0;
                try {
                    if (dto.getMetadata() != null && !dto.getMetadata().trim().isEmpty()) {
                        duration = Integer.parseInt(dto.getMetadata());
                    }
                } catch (NumberFormatException e) {
                    // Durée par défaut: 0
                }
                return new VideoContent(dto.getData(), duration);

            default:
                throw new IllegalArgumentException("Type de contenu inconnu: " + dto.getType());
        }
    }

    /**
     * Recupere tous les tickets sous forme de DTOs
     */
    public List<TicketDTO> getAllTickets() {
        return convertToTicketDTOs(state.getAllTickets());
    }

    /**
     * Recupere les tickets filtres selon l'utilisateur actuel
     */
    public List<TicketDTO> getFilteredTickets() {
        UserDTO currentUser = getCurrentUser();
        List<Ticket> tickets;

        // Admin et Developpeur voient tous les tickets
        if (currentUser.hasFullAccess()) {
            tickets = state.getAllTickets();
        } else {
            // Utilisateur normal voit seulement ses propres tickets
            tickets = new ArrayList<>();
            for (Ticket ticket : state.getAllTickets()) {
                if (ticket.getCreatedByUserID() != null &&
                    ticket.getCreatedByUserID() == currentUser.getUserID()) {
                    tickets.add(ticket);
                }
            }
        }

        return convertToTicketDTOs(tickets);
    }

    /**
     * Recupere un ticket par son ID
     */
    public TicketDTO getTicketById(int ticketID) {
        for (Ticket ticket : state.getAllTickets()) {
            if (ticket.getTicketID() == ticketID) {
                return convertToTicketDTO(ticket);
            }
        }
        return null;
    }

    /**
     * Assigne un ticket a un utilisateur
     */
    public void assignTicket(int ticketID, int userID) {
        Ticket ticket = findTicketById(ticketID);
        if (ticket != null) {
            ticket.assignTo(userID);
        }
    }

    /**
     * Change le statut d'un ticket
     */
    public void changeTicketStatus(int ticketID, String newStatusStr) {
        Ticket ticket = findTicketById(ticketID);
        if (ticket != null) {
            TicketStatus newStatus = TicketStatus.valueOf(newStatusStr);
            ticket.updateStatus(newStatus);
        }
    }

    /**
     * Retourne les transitions possibles pour un ticket
     */
    public List<String> getAvailableTransitions(int ticketID) {
        Ticket ticket = findTicketById(ticketID);
        if (ticket == null) return new ArrayList<>();

        List<String> transitions = new ArrayList<>();
        for (TicketStatus status : ticket.getStatus().getAvailableTransitionsList()) {
            transitions.add(status.toString());
        }
        return transitions;
    }

    /**
     * Ajoute un commentaire a un ticket
     */
    public void addComment(int ticketID, String comment) {
        Ticket ticket = findTicketById(ticketID);
        if (ticket != null) {
            ticket.addComment(comment);
        }
    }

    /**
     * NOUVEAU: Récupère les commentaires d'un ticket
     * Retourne la liste directement sans formatage (pour TicketDetailPanel)
     */
    public List<String> getTicketComments(int ticketID) {
        Ticket ticket = findTicketById(ticketID);
        if (ticket == null) {
            return new ArrayList<>();
        }
        return ticket.getComments();
    }

    /**
     * Modifie un ticket existant
     */
    public void updateTicket(int ticketID, String title, String priority, Content newDescription) {
        Ticket ticket = findTicketById(ticketID);
        if (ticket != null) {
            ticket.setTitle(title);
            ticket.setPriority(priority);
            if (newDescription != null) {
                ticket.setDescription(newDescription);
            }
        }
    }

    /**
     * NOUVEAU: Modifie un ticket avec une liste de ContentItemDTO
     */
    public void updateTicketWithContentItems(int ticketID, String title, String priority, List<ContentItemDTO> contentItems) {
        Ticket ticket = findTicketById(ticketID);
        if (ticket != null) {
            ticket.setTitle(title);
            ticket.setPriority(priority);
            if (contentItems != null && !contentItems.isEmpty()) {
                Content newContent = buildContentFromDTOs(contentItems);
                ticket.setDescription(newContent);
            }
        }
    }

    /**
     * Exporte un ticket en format texte
     */
    public String exportTicketToText(int ticketID) {
        Ticket ticket = findTicketById(ticketID);
        if (ticket != null) {
            return ticket.exportToPDF();
        }
        return "";
    }

    /**
     * Retourne les details complets d'un ticket (pour affichage)
     */
    public String getTicketDetails(int ticketID) {
        Ticket ticket = findTicketById(ticketID);
        if (ticket == null) return "";

        StringBuilder details = new StringBuilder();
        details.append("Ticket #").append(ticket.getTicketID()).append("\n\n");
        details.append("Titre: ").append(ticket.getTitle()).append("\n");
        details.append("Statut: ").append(ticket.getStatus()).append("\n");
        details.append("Priorite: ").append(ticket.getPriority()).append("\n");
        details.append("Cree le: ").append(ticket.getCreationDate()).append("\n");

        if (ticket.getAssignedToUserID() != null) {
            String assignedName = getUserNameById(ticket.getAssignedToUserID());
            details.append("Assigne a: ").append(assignedName).append("\n");
        } else {
            details.append("Assigne a: Non assigne\n");
        }

        details.append("\nDescription:\n");
        details.append(ticket.getDescription().display());

        if (!ticket.getComments().isEmpty()) {
            details.append("\n\nCommentaires (").append(ticket.getComments().size()).append("):\n");
            int i = 1;
            for (String comment : ticket.getComments()) {
                details.append("[").append(i++).append("] ").append(comment).append("\n");
            }
        }

        return details.toString();
    }

    // ========== Methodes privees de conversion ==========

    private UserDTO convertToUserDTO(User user) {
        boolean isAdmin = user instanceof Admin;
        String role = isAdmin ? "Admin" : user.getRole();
        return new UserDTO(user.getUserID(), user.getName(), role, isAdmin);
    }

    private TicketDTO convertToTicketDTO(Ticket ticket) {
        String createdByName = getUserNameById(ticket.getCreatedByUserID());
        String assignedToName = getUserNameById(ticket.getAssignedToUserID());
        String description = ticket.getDescription().display();
        String creationDate = ticket.getCreationDate().toString();

        return new TicketDTO(
            ticket.getTicketID(),
            ticket.getTitle(),
            ticket.getStatus().toString(),
            ticket.getPriority(),
            createdByName,
            assignedToName,
            description,
            creationDate
        );
    }

    private List<TicketDTO> convertToTicketDTOs(List<Ticket> tickets) {
        List<TicketDTO> dtos = new ArrayList<>();
        for (Ticket ticket : tickets) {
            dtos.add(convertToTicketDTO(ticket));
        }
        return dtos;
    }

    private Ticket findTicketById(int ticketID) {
        for (Ticket ticket : state.getAllTickets()) {
            if (ticket.getTicketID() == ticketID) {
                return ticket;
            }
        }
        return null;
    }

    private String getUserNameById(Integer userID) {
        if (userID == null) {
            return "Non assigne";
        }

        for (User user : state.getAllUsers()) {
            if (user.getUserID() == userID) {
                return user.getName();
            }
        }

        return "User #" + userID;
    }
}

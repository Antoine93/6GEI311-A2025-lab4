package api.server.services;

import core.entities.*;
import core.content.*;
import api.server.models.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ApplicationState - Singleton côté serveur
 * Gère l'état global de l'application côté serveur REST
 *
 * Cette classe est la version serveur de gui.controllers.ApplicationState
 * Elle maintient les données en mémoire et fournit les services métier
 *
 * Thread-safe avec ConcurrentHashMap pour gérer plusieurs requêtes simultanées
 */
public class ApplicationState {
    private static ApplicationState instance;

    private List<Ticket> allTickets;
    private List<User> allUsers;
    private Map<String, User> sessions; // token -> User

    private ApplicationState() {
        allTickets = Collections.synchronizedList(new ArrayList<>());
        allUsers = Collections.synchronizedList(new ArrayList<>());
        sessions = new ConcurrentHashMap<>();
        initTestData();
    }

    public static synchronized ApplicationState getInstance() {
        if (instance == null) {
            instance = new ApplicationState();
        }
        return instance;
    }

    /**
     * Initialise les données de test
     */
    private void initTestData() {
        // Créer des utilisateurs de test
        User user1 = new User(1, "Utilisateur1", "utilisateur1@uqac.ca", "Developpeur");
        User user2 = new User(2, "Utilisateur2", "utilisateur2@uqac.ca", "Testeur");
        Admin admin = new Admin(100, "Admin1", "admin@uqac.ca");

        allUsers.add(user1);
        allUsers.add(user2);
        allUsers.add(admin);

        // Créer quelques tickets de test
        TextContent desc1 = new TextContent(
                "L'application crash lorsqu'on clique sur le bouton de connexion après 3 tentatives échouées."
        );
        Ticket ticket1 = user1.createTicket(
                "Bug critique - Crash à la connexion",
                desc1,
                "Haute"
        );
        allTickets.add(ticket1);

        TextContent desc2 = new TextContent(
                "L'interface utilisateur n'est pas responsive sur mobile. Les boutons sont trop petits."
        );
        Ticket ticket2 = user1.createTicket(
                "Amélioration UI - Responsive design",
                desc2,
                "Moyenne"
        );
        allTickets.add(ticket2);

        // Ticket avec contenu composite
        CompositeContent richDesc = new CompositeContent();
        richDesc.add(new TextContent("Problème de validation du code 2FA après plusieurs tentatives"));
        richDesc.add(new ImageContent("/captures/2fa_error.png", "Écran d'erreur 2FA"));
        richDesc.add(new VideoContent("/videos/demo_bug.mp4", 125));

        Ticket ticket3 = user2.createTicket(
                "Bug 2FA - Validation incorrecte",
                richDesc,
                "Critique"
        );
        allTickets.add(ticket3);

        System.out.println("[INIT] ApplicationState initialisé avec " + allUsers.size() + " utilisateurs et " + allTickets.size() + " tickets");
    }

    // ========================================================================
    // Gestion des sessions
    // ========================================================================

    public String createSession(User user) {
        String token = "session_" + UUID.randomUUID().toString();
        sessions.put(token, user);
        return token;
    }

    public User getUserFromSession(String token) {
        return sessions.get(token);
    }

    public void invalidateSession(String token) {
        sessions.remove(token);
    }

    // ========================================================================
    // Recherche d'entités
    // ========================================================================

    public User findUserById(int userId) {
        synchronized (allUsers) {
            for (User user : allUsers) {
                if (user.getUserID() == userId) {
                    return user;
                }
            }
        }
        return null;
    }

    public Ticket findTicketById(int ticketId) {
        synchronized (allTickets) {
            for (Ticket ticket : allTickets) {
                if (ticket.getTicketID() == ticketId) {
                    return ticket;
                }
            }
        }
        return null;
    }

    // ========================================================================
    // Conversion Entity -> DTO
    // ========================================================================

    public UserDTO convertToUserDTO(User user) {
        boolean isAdmin = user instanceof Admin;
        String role = isAdmin ? "Admin" : user.getRole();
        return new UserDTO(
                user.getUserID(),
                user.getName(),
                user.getEmail(),
                role,
                isAdmin
        );
    }

    public TicketDTO convertToTicketDTO(Ticket ticket) {
        String createdByName = getUserNameById(ticket.getCreatedByUserID());
        String assignedToName = getUserNameById(ticket.getAssignedToUserID());
        String description = ticket.getDescription().display();

        // Convertir le contenu en liste de ContentItemDTO
        List<ContentItemDTO> contentItems = convertContentToDTO(ticket.getDescription());

        return new TicketDTO(
                ticket.getTicketID(),
                ticket.getTitle(),
                ticket.getStatus().toString(),
                ticket.getPriority(),
                createdByName,
                assignedToName,
                description,
                contentItems,
                ticket.getCreationDate().toString(),
                ticket.getUpdateDate().toString()
        );
    }

    /**
     * Convertit un objet Content en liste de ContentItemDTO
     */
    private List<ContentItemDTO> convertContentToDTO(Content content) {
        List<ContentItemDTO> items = new ArrayList<>();

        if (content instanceof CompositeContent) {
            CompositeContent composite = (CompositeContent) content;
            for (Content child : composite.getChildren()) {
                items.add(convertSingleContentToDTO(child));
            }
        } else {
            items.add(convertSingleContentToDTO(content));
        }

        return items;
    }

    /**
     * Convertit un Content simple en ContentItemDTO
     */
    private ContentItemDTO convertSingleContentToDTO(Content content) {
        if (content instanceof TextContent) {
            TextContent text = (TextContent) content;
            return new ContentItemDTO(
                    ContentItemDTO.ContentType.TEXT,
                    text.getText(),
                    null
            );
        } else if (content instanceof ImageContent) {
            ImageContent image = (ImageContent) content;
            return new ContentItemDTO(
                    ContentItemDTO.ContentType.IMAGE,
                    image.getImagePath(),
                    image.getCaption()
            );
        } else if (content instanceof VideoContent) {
            VideoContent video = (VideoContent) content;
            return new ContentItemDTO(
                    ContentItemDTO.ContentType.VIDEO,
                    video.getVideoPath(),
                    String.valueOf(video.getDuration())
            );
        }

        return null;
    }

    /**
     * Convertit une liste de ContentItemDTO en objet Content
     */
    public Content convertDTOToContent(List<ContentItemDTO> items) {
        if (items == null || items.isEmpty()) {
            return new TextContent("");
        }

        if (items.size() == 1) {
            return convertDTOToSingleContent(items.get(0));
        }

        // Plusieurs items : créer un composite
        CompositeContent composite = new CompositeContent();
        for (ContentItemDTO item : items) {
            composite.add(convertDTOToSingleContent(item));
        }
        return composite;
    }

    /**
     * Convertit un ContentItemDTO en Content simple
     */
    private Content convertDTOToSingleContent(ContentItemDTO dto) {
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
                return new TextContent("");
        }
    }

    private String getUserNameById(Integer userId) {
        if (userId == null) {
            return "Non assigné";
        }

        User user = findUserById(userId);
        return user != null ? user.getName() : "User #" + userId;
    }

    // ========================================================================
    // Méthodes DTO (pour les endpoints)
    // ========================================================================

    public List<UserDTO> getAllUsersDTO() {
        List<UserDTO> dtos = new ArrayList<>();
        synchronized (allUsers) {
            for (User user : allUsers) {
                dtos.add(convertToUserDTO(user));
            }
        }
        return dtos;
    }

    public UserDTO getUserDTOById(int userId) {
        User user = findUserById(userId);
        return user != null ? convertToUserDTO(user) : null;
    }

    public List<TicketDTO> getAllTicketsDTO() {
        List<TicketDTO> dtos = new ArrayList<>();
        synchronized (allTickets) {
            for (Ticket ticket : allTickets) {
                dtos.add(convertToTicketDTO(ticket));
            }
        }
        return dtos;
    }

    public TicketDTO getTicketDTOById(int ticketId) {
        Ticket ticket = findTicketById(ticketId);
        return ticket != null ? convertToTicketDTO(ticket) : null;
    }

    // ========================================================================
    // Opérations métier
    // ========================================================================

    public TicketDTO createTicket(CreateTicketRequest request) {
        // TODO: Récupérer l'utilisateur connecté depuis la session
        // Pour l'instant, on utilise le premier utilisateur
        User creator = allUsers.get(0);

        Content content = convertDTOToContent(request.getDescriptionContent());
        Ticket ticket = creator.createTicket(request.getTitle(), content, request.getPriority());

        synchronized (allTickets) {
            allTickets.add(ticket);
        }

        return convertToTicketDTO(ticket);
    }

    public TicketDTO updateTicket(int ticketId, UpdateTicketRequest request) {
        Ticket ticket = findTicketById(ticketId);
        if (ticket == null) {
            return null;
        }

        if (request.getTitle() != null) {
            ticket.setTitle(request.getTitle());
        }

        if (request.getPriority() != null) {
            ticket.setPriority(request.getPriority());
        }

        if (request.getDescriptionContent() != null && !request.getDescriptionContent().isEmpty()) {
            Content content = convertDTOToContent(request.getDescriptionContent());
            ticket.setDescription(content);
        }

        return convertToTicketDTO(ticket);
    }

    public boolean deleteTicket(int ticketId) {
        Ticket ticket = findTicketById(ticketId);
        if (ticket == null) {
            return false;
        }

        synchronized (allTickets) {
            return allTickets.remove(ticket);
        }
    }

    public List<String> getTicketComments(int ticketId) {
        Ticket ticket = findTicketById(ticketId);
        return ticket != null ? ticket.getComments() : null;
    }

    public String addComment(int ticketId, String commentText) {
        Ticket ticket = findTicketById(ticketId);
        if (ticket == null) {
            return null;
        }

        ticket.addComment(commentText);
        return commentText;
    }

    public List<String> getAvailableTransitions(int ticketId) {
        Ticket ticket = findTicketById(ticketId);
        if (ticket == null) {
            return null;
        }

        List<String> transitions = new ArrayList<>();
        for (TicketStatus status : ticket.getStatus().getAvailableTransitionsList()) {
            transitions.add(status.name());
        }
        return transitions;
    }

    public TicketDTO changeTicketStatus(int ticketId, String newStatusStr) throws IllegalStateException {
        Ticket ticket = findTicketById(ticketId);
        if (ticket == null) {
            return null;
        }

        TicketStatus newStatus = TicketStatus.valueOf(newStatusStr);
        ticket.updateStatus(newStatus); // Peut lancer IllegalStateException

        return convertToTicketDTO(ticket);
    }

    public TicketDTO assignTicket(int ticketId, int userId) {
        Ticket ticket = findTicketById(ticketId);
        User user = findUserById(userId);

        if (ticket == null || user == null) {
            return null;
        }

        ticket.assignTo(userId);
        return convertToTicketDTO(ticket);
    }

    public String exportTicketToPDF(int ticketId) {
        Ticket ticket = findTicketById(ticketId);
        if (ticket == null) {
            return null;
        }

        return ticket.exportToPDF();
    }
}

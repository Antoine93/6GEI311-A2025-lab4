package gui.services;

import gui.models.*;
import gui.services.SimpleHttpClient.HttpResponse;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * RestTicketService - Implémentation REST de ITicketService
 *
 * Communique avec l'API REST via HTTP/JSON
 * Utilise SimpleHttpClient pour les requêtes HTTP et Gson pour JSON
 */
public class RestTicketService implements ITicketService {

    private SimpleHttpClient httpClient;
    private Gson gson;
    private UserDTO currentUser;
    private String authToken;

    private static final String API_BASE_URL = "http://localhost:8080/api/v1";

    public RestTicketService() {
        this.httpClient = new SimpleHttpClient(API_BASE_URL);
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.currentUser = null;
        this.authToken = null;
    }

    /**
     * Constructeur avec URL personnalisée (pour tests)
     */
    public RestTicketService(String baseUrl) {
        this.httpClient = new SimpleHttpClient(baseUrl);
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.currentUser = null;
        this.authToken = null;
    }

    // ========================================================================
    // Authentification
    // ========================================================================

    @Override
    public UserDTO login(int userID) throws ServiceException {
        try {
            // Créer la requête de login
            LoginRequest request = new LoginRequest(userID);
            String jsonBody = gson.toJson(request);

            // Envoyer POST /auth/login
            HttpResponse response = httpClient.post("/auth/login", jsonBody);

            // Vérifier la réponse
            if (!response.isSuccess()) {
                throw parseErrorResponse(response);
            }

            // Parser la réponse
            AuthResponse authResponse = gson.fromJson(response.getBody(), AuthResponse.class);

            // Stocker le token et l'utilisateur
            this.authToken = authResponse.getToken();
            this.currentUser = authResponse.getUser();
            this.httpClient.setAuthToken(authToken);

            System.out.println("[REST] Login réussi pour: " + currentUser.getName());
            return currentUser;

        } catch (IOException e) {
            throw new ServiceException("Erreur réseau lors de la connexion: " + e.getMessage(), e);
        }
    }

    @Override
    public void logout() throws ServiceException {
        try {
            // Envoyer POST /auth/logout
            HttpResponse response = httpClient.post("/auth/logout", "");

            // Nettoyer le token et l'utilisateur (même si la requête échoue)
            this.authToken = null;
            this.currentUser = null;
            this.httpClient.clearAuthToken();

            System.out.println("[REST] Logout réussi");

        } catch (IOException e) {
            // Nettoyer quand même localement
            this.authToken = null;
            this.currentUser = null;
            this.httpClient.clearAuthToken();

            throw new ServiceException("Erreur réseau lors de la déconnexion: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean isAuthenticated() {
        return authToken != null && currentUser != null;
    }

    @Override
    public UserDTO getCurrentUser() {
        return currentUser;
    }

    // ========================================================================
    // Utilisateurs
    // ========================================================================

    @Override
    public List<UserDTO> getAllUsers() throws ServiceException {
        try {
            HttpResponse response = httpClient.get("/users");

            if (!response.isSuccess()) {
                throw parseErrorResponse(response);
            }

            // Parser le tableau d'utilisateurs
            Type listType = new TypeToken<List<UserDTO>>(){}.getType();
            List<UserDTO> users = gson.fromJson(response.getBody(), listType);

            return users != null ? users : new ArrayList<>();

        } catch (IOException e) {
            throw new ServiceException("Erreur réseau lors de la récupération des utilisateurs: " + e.getMessage(), e);
        }
    }

    @Override
    public UserDTO getUserById(int userID) throws ServiceException {
        try {
            HttpResponse response = httpClient.get("/users/" + userID);

            if (response.getStatusCode() == 404) {
                return null; // Utilisateur non trouvé
            }

            if (!response.isSuccess()) {
                throw parseErrorResponse(response);
            }

            return gson.fromJson(response.getBody(), UserDTO.class);

        } catch (IOException e) {
            throw new ServiceException("Erreur réseau lors de la récupération de l'utilisateur: " + e.getMessage(), e);
        }
    }

    // ========================================================================
    // Tickets (CRUD)
    // ========================================================================

    @Override
    public List<TicketDTO> getAllTickets() throws ServiceException {
        try {
            HttpResponse response = httpClient.get("/tickets");

            if (!response.isSuccess()) {
                throw parseErrorResponse(response);
            }

            // Parser le tableau de tickets
            Type listType = new TypeToken<List<api.server.models.TicketDTO>>(){}.getType();
            List<api.server.models.TicketDTO> serverTickets = gson.fromJson(response.getBody(), listType);

            // Convertir api.server.models.TicketDTO → gui.models.TicketDTO
            List<TicketDTO> guiTickets = new ArrayList<>();
            if (serverTickets != null) {
                for (api.server.models.TicketDTO serverTicket : serverTickets) {
                    guiTickets.add(convertServerTicketToGui(serverTicket));
                }
            }

            return guiTickets;

        } catch (IOException e) {
            throw new ServiceException("Erreur réseau lors de la récupération des tickets: " + e.getMessage(), e);
        }
    }

    @Override
    public TicketDTO getTicketById(int ticketID) throws ServiceException {
        try {
            HttpResponse response = httpClient.get("/tickets/" + ticketID);

            if (response.getStatusCode() == 404) {
                return null; // Ticket non trouvé
            }

            if (!response.isSuccess()) {
                throw parseErrorResponse(response);
            }

            api.server.models.TicketDTO serverTicket = gson.fromJson(response.getBody(),
                    api.server.models.TicketDTO.class);

            return convertServerTicketToGui(serverTicket);

        } catch (IOException e) {
            throw new ServiceException("Erreur réseau lors de la récupération du ticket: " + e.getMessage(), e);
        }
    }

    @Override
    public TicketDTO createTicket(String title, List<ContentItemDTO> contentItems, String priority)
            throws ServiceException {
        try {
            // Convertir gui.models.ContentItemDTO → api.server.models.ContentItemDTO
            List<api.server.models.ContentItemDTO> serverContentItems = new ArrayList<>();
            for (ContentItemDTO guiItem : contentItems) {
                serverContentItems.add(convertGuiContentToServer(guiItem));
            }

            // Créer la requête
            api.server.models.CreateTicketRequest request =
                    new api.server.models.CreateTicketRequest(title, priority, serverContentItems);

            String jsonBody = gson.toJson(request);

            // Envoyer POST /tickets
            HttpResponse response = httpClient.post("/tickets", jsonBody);

            if (!response.isSuccess()) {
                throw parseErrorResponse(response);
            }

            api.server.models.TicketDTO serverTicket = gson.fromJson(response.getBody(),
                    api.server.models.TicketDTO.class);

            return convertServerTicketToGui(serverTicket);

        } catch (IOException e) {
            throw new ServiceException("Erreur réseau lors de la création du ticket: " + e.getMessage(), e);
        }
    }

    @Override
    public TicketDTO updateTicket(int ticketID, String title, String priority,
                                  List<ContentItemDTO> contentItems) throws ServiceException {
        try {
            // Créer la requête
            api.server.models.UpdateTicketRequest request = new api.server.models.UpdateTicketRequest();
            request.setTitle(title);
            request.setPriority(priority);

            if (contentItems != null && !contentItems.isEmpty()) {
                List<api.server.models.ContentItemDTO> serverContentItems = new ArrayList<>();
                for (ContentItemDTO guiItem : contentItems) {
                    serverContentItems.add(convertGuiContentToServer(guiItem));
                }
                request.setDescriptionContent(serverContentItems);
            }

            String jsonBody = gson.toJson(request);

            // Envoyer PUT /tickets/{id}
            HttpResponse response = httpClient.put("/tickets/" + ticketID, jsonBody);

            if (!response.isSuccess()) {
                throw parseErrorResponse(response);
            }

            api.server.models.TicketDTO serverTicket = gson.fromJson(response.getBody(),
                    api.server.models.TicketDTO.class);

            return convertServerTicketToGui(serverTicket);

        } catch (IOException e) {
            throw new ServiceException("Erreur réseau lors de la modification du ticket: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteTicket(int ticketID) throws ServiceException {
        try {
            HttpResponse response = httpClient.delete("/tickets/" + ticketID);

            if (!response.isSuccess() && response.getStatusCode() != 204) {
                throw parseErrorResponse(response);
            }

        } catch (IOException e) {
            throw new ServiceException("Erreur réseau lors de la suppression du ticket: " + e.getMessage(), e);
        }
    }

    // ========================================================================
    // Commentaires
    // ========================================================================

    @Override
    public List<String> getTicketComments(int ticketID) throws ServiceException {
        try {
            HttpResponse response = httpClient.get("/tickets/" + ticketID + "/comments");

            if (!response.isSuccess()) {
                throw parseErrorResponse(response);
            }

            Type listType = new TypeToken<List<String>>(){}.getType();
            List<String> comments = gson.fromJson(response.getBody(), listType);

            return comments != null ? comments : new ArrayList<>();

        } catch (IOException e) {
            throw new ServiceException("Erreur réseau lors de la récupération des commentaires: " + e.getMessage(), e);
        }
    }

    @Override
    public String addComment(int ticketID, String comment) throws ServiceException {
        try {
            api.server.models.CommentRequest request = new api.server.models.CommentRequest(comment);
            String jsonBody = gson.toJson(request);

            HttpResponse response = httpClient.post("/tickets/" + ticketID + "/comments", jsonBody);

            if (!response.isSuccess()) {
                throw parseErrorResponse(response);
            }

            // Le serveur retourne le commentaire ajouté (String)
            return gson.fromJson(response.getBody(), String.class);

        } catch (IOException e) {
            throw new ServiceException("Erreur réseau lors de l'ajout du commentaire: " + e.getMessage(), e);
        }
    }

    // ========================================================================
    // Gestion du statut
    // ========================================================================

    @Override
    public List<String> getAvailableTransitions(int ticketID) throws ServiceException {
        try {
            HttpResponse response = httpClient.get("/tickets/" + ticketID + "/status");

            if (!response.isSuccess()) {
                throw parseErrorResponse(response);
            }

            Type listType = new TypeToken<List<String>>(){}.getType();
            List<String> transitions = gson.fromJson(response.getBody(), listType);

            return transitions != null ? transitions : new ArrayList<>();

        } catch (IOException e) {
            throw new ServiceException("Erreur réseau lors de la récupération des transitions: " + e.getMessage(), e);
        }
    }

    @Override
    public TicketDTO changeTicketStatus(int ticketID, String newStatus) throws ServiceException {
        try {
            api.server.models.StatusUpdateDTO request = new api.server.models.StatusUpdateDTO(newStatus);
            String jsonBody = gson.toJson(request);

            HttpResponse response = httpClient.patch("/tickets/" + ticketID + "/status", jsonBody);

            if (!response.isSuccess()) {
                throw parseErrorResponse(response);
            }

            api.server.models.TicketDTO serverTicket = gson.fromJson(response.getBody(),
                    api.server.models.TicketDTO.class);

            return convertServerTicketToGui(serverTicket);

        } catch (IOException e) {
            throw new ServiceException("Erreur réseau lors du changement de statut: " + e.getMessage(), e);
        }
    }

    // ========================================================================
    // Assignation
    // ========================================================================

    @Override
    public TicketDTO assignTicket(int ticketID, int userID) throws ServiceException {
        try {
            api.server.models.AssignmentDTO request = new api.server.models.AssignmentDTO(userID);
            String jsonBody = gson.toJson(request);

            HttpResponse response = httpClient.patch("/tickets/" + ticketID + "/assignment", jsonBody);

            if (!response.isSuccess()) {
                throw parseErrorResponse(response);
            }

            api.server.models.TicketDTO serverTicket = gson.fromJson(response.getBody(),
                    api.server.models.TicketDTO.class);

            return convertServerTicketToGui(serverTicket);

        } catch (IOException e) {
            throw new ServiceException("Erreur réseau lors de l'assignation: " + e.getMessage(), e);
        }
    }

    // ========================================================================
    // Export
    // ========================================================================

    @Override
    public String exportTicketToPDF(int ticketID) throws ServiceException {
        try {
            HttpResponse response = httpClient.get("/tickets/" + ticketID + "/export/pdf");

            if (!response.isSuccess()) {
                throw parseErrorResponse(response);
            }

            // Le serveur retourne du text/plain
            return response.getBody();

        } catch (IOException e) {
            throw new ServiceException("Erreur réseau lors de l'export PDF: " + e.getMessage(), e);
        }
    }

    // ========================================================================
    // Méthodes utilitaires de conversion
    // ========================================================================

    /**
     * Convertit api.server.models.TicketDTO → gui.models.TicketDTO
     */
    private TicketDTO convertServerTicketToGui(api.server.models.TicketDTO serverTicket) {
        return new TicketDTO(
                serverTicket.getTicketID(),
                serverTicket.getTitle(),
                serverTicket.getStatus(),
                serverTicket.getPriority(),
                serverTicket.getCreatedByName(),
                serverTicket.getAssignedToName(),
                serverTicket.getDescription(),
                serverTicket.getCreationDate()
        );
    }

    /**
     * Convertit gui.models.ContentItemDTO → api.server.models.ContentItemDTO
     */
    private api.server.models.ContentItemDTO convertGuiContentToServer(ContentItemDTO guiItem) {
        api.server.models.ContentItemDTO.ContentType serverType;

        switch (guiItem.getType()) {
            case TEXT:
                serverType = api.server.models.ContentItemDTO.ContentType.TEXT;
                break;
            case IMAGE:
                serverType = api.server.models.ContentItemDTO.ContentType.IMAGE;
                break;
            case VIDEO:
                serverType = api.server.models.ContentItemDTO.ContentType.VIDEO;
                break;
            default:
                throw new IllegalArgumentException("Type de contenu inconnu: " + guiItem.getType());
        }

        return new api.server.models.ContentItemDTO(serverType, guiItem.getData(), guiItem.getMetadata());
    }

    /**
     * Parse une réponse d'erreur HTTP et crée une ServiceException
     */
    private ServiceException parseErrorResponse(HttpResponse response) {
        try {
            api.server.models.ErrorResponse errorResponse = gson.fromJson(response.getBody(),
                    api.server.models.ErrorResponse.class);

            return new ServiceException(
                    response.getStatusCode(),
                    errorResponse.getError(),
                    errorResponse.getMessage()
            );

        } catch (Exception e) {
            // Si parsing échoue, retourner un message générique
            return new ServiceException(
                    response.getStatusCode(),
                    "HTTP_ERROR",
                    "Erreur HTTP " + response.getStatusCode() + ": " + response.getBody()
            );
        }
    }

    // ========================================================================
    // Classes internes pour les requêtes JSON
    // ========================================================================

    private static class LoginRequest {
        private int userID;

        public LoginRequest(int userID) {
            this.userID = userID;
        }

        public int getUserID() {
            return userID;
        }
    }

    private static class AuthResponse {
        private String token;
        private UserDTO user;

        public String getToken() {
            return token;
        }

        public UserDTO getUser() {
            return user;
        }
    }
}

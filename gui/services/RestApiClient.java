package gui.services;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import gui.models.*;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * RestApiClient - Client HTTP pour communiquer avec l'API REST
 *
 * Encapsule tous les appels HTTP vers le serveur REST
 * Gère l'authentification via token Bearer
 * Convertit les réponses JSON en DTOs
 */
public class RestApiClient {
    private static final String BASE_URL = "http://localhost:8080/api/v1";
    private static RestApiClient instance;

    private final Gson gson;
    private String authToken;

    private RestApiClient() {
        this.gson = new Gson();
        this.authToken = null;
    }

    public static RestApiClient getInstance() {
        if (instance == null) {
            instance = new RestApiClient();
        }
        return instance;
    }

    /**
     * Définit le token d'authentification
     */
    public void setAuthToken(String token) {
        this.authToken = token;
    }

    /**
     * Retourne le token d'authentification actuel
     */
    public String getAuthToken() {
        return authToken;
    }

    /**
     * Vérifie si le client est authentifié
     */
    public boolean isAuthenticated() {
        return authToken != null && !authToken.isEmpty();
    }

    // ========================================================================
    // AUTHENTIFICATION
    // ========================================================================

    /**
     * POST /auth/login
     * Authentifie un utilisateur et stocke le token
     */
    public UserDTO login(int userID) throws IOException {
        String requestBody = String.format("{\"userID\": %d}", userID);

        String response = sendRequest("POST", "/auth/login", requestBody, false);

        // Parser la réponse AuthResponse
        Map<String, Object> authResponse = gson.fromJson(response,
            new TypeToken<Map<String, Object>>(){}.getType());

        this.authToken = (String) authResponse.get("token");

        Map<String, Object> userMap = (Map<String, Object>) authResponse.get("user");
        return mapToUserDTO(userMap);
    }

    /**
     * GET /auth/session
     * Vérifie la session active
     */
    public UserDTO getSession() throws IOException {
        String response = sendRequest("GET", "/auth/session", null, true);
        return gson.fromJson(response, UserDTO.class);
    }

    /**
     * POST /auth/logout
     * Déconnexion
     */
    public void logout() throws IOException {
        sendRequest("POST", "/auth/logout", null, true);
        this.authToken = null;
    }

    // ========================================================================
    // UTILISATEURS
    // ========================================================================

    /**
     * GET /users
     * Liste tous les utilisateurs
     */
    public List<UserDTO> getAllUsers() throws IOException {
        String response = sendRequest("GET", "/users", null, true);
        return gson.fromJson(response, new TypeToken<List<UserDTO>>(){}.getType());
    }

    /**
     * GET /users/{id}
     * Détails d'un utilisateur
     */
    public UserDTO getUserById(int userID) throws IOException {
        String response = sendRequest("GET", "/users/" + userID, null, true);
        return gson.fromJson(response, UserDTO.class);
    }

    // ========================================================================
    // TICKETS (CRUD)
    // ========================================================================

    /**
     * GET /tickets
     * Liste des tickets (filtrés selon les permissions utilisateur)
     */
    public List<TicketDTO> getAllTickets() throws IOException {
        String response = sendRequest("GET", "/tickets", null, true);
        return gson.fromJson(response, new TypeToken<List<TicketDTO>>(){}.getType());
    }

    /**
     * GET /tickets?status=...&priority=...
     * Liste des tickets avec filtres
     */
    public List<TicketDTO> getTicketsWithFilters(String status, String priority, Integer assignedTo) throws IOException {
        StringBuilder endpoint = new StringBuilder("/tickets?");

        if (status != null && !status.isEmpty()) {
            endpoint.append("status=").append(status).append("&");
        }
        if (priority != null && !priority.isEmpty()) {
            endpoint.append("priority=").append(priority).append("&");
        }
        if (assignedTo != null) {
            endpoint.append("assignedTo=").append(assignedTo).append("&");
        }

        String response = sendRequest("GET", endpoint.toString(), null, true);
        return gson.fromJson(response, new TypeToken<List<TicketDTO>>(){}.getType());
    }

    /**
     * GET /tickets/{id}
     * Détails d'un ticket
     */
    public TicketDTO getTicketById(int ticketID) throws IOException {
        String response = sendRequest("GET", "/tickets/" + ticketID, null, true);
        return gson.fromJson(response, TicketDTO.class);
    }

    /**
     * POST /tickets
     * Créer un nouveau ticket
     */
    public TicketDTO createTicket(String title, String priority, List<ContentItemDTO> contentItems) throws IOException {
        Map<String, Object> request = new HashMap<>();
        request.put("title", title);
        request.put("priority", priority);
        request.put("descriptionContent", contentItems);

        String requestBody = gson.toJson(request);
        String response = sendRequest("POST", "/tickets", requestBody, true);

        return gson.fromJson(response, TicketDTO.class);
    }

    /**
     * PUT /tickets/{id}
     * Modifier un ticket
     */
    public TicketDTO updateTicket(int ticketID, String title, String priority, List<ContentItemDTO> contentItems) throws IOException {
        Map<String, Object> request = new HashMap<>();
        if (title != null) request.put("title", title);
        if (priority != null) request.put("priority", priority);
        if (contentItems != null) request.put("descriptionContent", contentItems);

        String requestBody = gson.toJson(request);
        String response = sendRequest("PUT", "/tickets/" + ticketID, requestBody, true);

        return gson.fromJson(response, TicketDTO.class);
    }

    /**
     * DELETE /tickets/{id}
     * Supprimer un ticket (admin seulement)
     */
    public void deleteTicket(int ticketID) throws IOException {
        sendRequest("DELETE", "/tickets/" + ticketID, null, true);
    }

    // ========================================================================
    // COMMENTAIRES
    // ========================================================================

    /**
     * GET /tickets/{id}/comments
     * Liste des commentaires d'un ticket
     */
    public List<String> getTicketComments(int ticketID) throws IOException {
        String response = sendRequest("GET", "/tickets/" + ticketID + "/comments", null, true);
        return gson.fromJson(response, new TypeToken<List<String>>(){}.getType());
    }

    /**
     * POST /tickets/{id}/comments
     * Ajouter un commentaire
     */
    public String addComment(int ticketID, String text) throws IOException {
        Map<String, String> request = new HashMap<>();
        request.put("text", text);

        String requestBody = gson.toJson(request);
        String response = sendRequest("POST", "/tickets/" + ticketID + "/comments", requestBody, true);

        return gson.fromJson(response, String.class);
    }

    // ========================================================================
    // STATUTS
    // ========================================================================

    /**
     * GET /tickets/{id}/status
     * Obtenir les transitions disponibles
     */
    public List<String> getAvailableTransitions(int ticketID) throws IOException {
        String response = sendRequest("GET", "/tickets/" + ticketID + "/status", null, true);
        return gson.fromJson(response, new TypeToken<List<String>>(){}.getType());
    }

    /**
     * PATCH /tickets/{id}/status
     * Changer le statut d'un ticket
     */
    public TicketDTO changeTicketStatus(int ticketID, String newStatus) throws IOException {
        Map<String, String> request = new HashMap<>();
        request.put("newStatus", newStatus);

        String requestBody = gson.toJson(request);
        String response = sendRequest("POST", "/tickets/" + ticketID + "/status", requestBody, true);

        return gson.fromJson(response, TicketDTO.class);
    }

    // ========================================================================
    // ASSIGNATION
    // ========================================================================

    /**
     * PATCH /tickets/{id}/assignment
     * Assigner un ticket à un utilisateur
     */
    public TicketDTO assignTicket(int ticketID, int userID) throws IOException {
        Map<String, Integer> request = new HashMap<>();
        request.put("userID", userID);

        String requestBody = gson.toJson(request);
        String response = sendRequest("POST", "/tickets/" + ticketID + "/assignment", requestBody, true);

        return gson.fromJson(response, TicketDTO.class);
    }

    // ========================================================================
    // EXPORT
    // ========================================================================

    /**
     * GET /tickets/{id}/export/pdf
     * Exporter un ticket en PDF
     */
    public String exportTicketToPDF(int ticketID) throws IOException {
        return sendRequest("GET", "/tickets/" + ticketID + "/export/pdf", null, true);
    }

    // ========================================================================
    // MÉTHODES UTILITAIRES PRIVÉES
    // ========================================================================

    /**
     * Envoie une requête HTTP au serveur
     *
     * @param method Méthode HTTP (GET, POST, PUT, DELETE, PATCH)
     * @param endpoint Chemin de l'endpoint (ex: "/tickets")
     * @param requestBody Corps de la requête JSON (null si GET)
     * @param requireAuth true si l'authentification est requise
     * @return La réponse du serveur (corps de la réponse)
     * @throws IOException En cas d'erreur réseau ou HTTP
     */
    private String sendRequest(String method, String endpoint, String requestBody, boolean requireAuth) throws IOException {
        URL url = new URL(BASE_URL + endpoint);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setRequestMethod(method);
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Accept", "application/json");

        // Ajouter le token d'authentification si requis
        if (requireAuth && authToken != null) {
            conn.setRequestProperty("Authorization", "Bearer " + authToken);
        }

        // Envoyer le corps de la requête si présent
        if (requestBody != null && !requestBody.isEmpty()) {
            conn.setDoOutput(true);
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = requestBody.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }
        }

        // Lire la réponse
        int statusCode = conn.getResponseCode();

        // Codes de succès
        if (statusCode >= 200 && statusCode < 300) {
            if (statusCode == 204) {
                // No Content
                return "";
            }

            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    response.append(line);
                }
                return response.toString();
            }
        } else {
            // Erreur HTTP
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8))) {
                StringBuilder errorResponse = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    errorResponse.append(line);
                }

                // Parser l'erreur JSON si possible
                try {
                    Map<String, Object> errorMap = gson.fromJson(errorResponse.toString(),
                        new TypeToken<Map<String, Object>>(){}.getType());
                    String message = (String) errorMap.get("message");
                    throw new IOException("Erreur HTTP " + statusCode + ": " + message);
                } catch (Exception e) {
                    throw new IOException("Erreur HTTP " + statusCode + ": " + errorResponse.toString());
                }
            }
        }
    }

    /**
     * Convertit une Map en UserDTO
     */
    private UserDTO mapToUserDTO(Map<String, Object> map) {
        int userID = ((Double) map.get("userID")).intValue();
        String name = (String) map.get("name");
        String role = (String) map.get("role");
        boolean isAdmin = (Boolean) map.get("isAdmin");

        return new UserDTO(userID, name, role, isAdmin);
    }
}

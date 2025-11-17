package api.server.resources;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import api.server.models.ErrorResponse;
import api.server.services.ApplicationState;
import core.entities.User;
import core.entities.Admin;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/**
 * BaseResource - Classe de base pour tous les handlers HTTP
 * Fournit des méthodes utilitaires pour la gestion des réponses JSON
 * et l'authentification
 */
public abstract class BaseResource implements HttpHandler {

    protected static final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .create();

    protected final ApplicationState appState = ApplicationState.getInstance();

    /**
     * Gère les requêtes OPTIONS pour CORS preflight
     */
    protected void handleOptionsRequest(HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, PUT, PATCH, DELETE, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization");
        exchange.getResponseHeaders().set("Access-Control-Max-Age", "3600");
        exchange.sendResponseHeaders(204, -1);
    }

    /**
     * Envoie une réponse JSON
     */
    protected void sendJsonResponse(HttpExchange exchange, int statusCode, Object data) throws IOException {
        String jsonResponse = gson.toJson(data);
        byte[] bytes = jsonResponse.getBytes(StandardCharsets.UTF_8);

        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.sendResponseHeaders(statusCode, bytes.length);

        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    /**
     * Envoie une réponse texte
     */
    protected void sendTextResponse(HttpExchange exchange, int statusCode, String text) throws IOException {
        byte[] bytes = text.getBytes(StandardCharsets.UTF_8);

        exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8");
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.sendResponseHeaders(statusCode, bytes.length);

        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    /**
     * Envoie une réponse vide (204 No Content)
     */
    protected void sendNoContent(HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.sendResponseHeaders(204, -1);
    }

    /**
     * Envoie une réponse d'erreur
     */
    protected void sendErrorResponse(HttpExchange exchange, int statusCode, String error, String message) throws IOException {
        ErrorResponse errorResponse = new ErrorResponse(error, message);
        sendJsonResponse(exchange, statusCode, errorResponse);
    }

    /**
     * Lit le corps de la requête
     */
    protected String readRequestBody(HttpExchange exchange) throws IOException {
        return new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
    }

    /**
     * Extrait l'ID depuis le chemin (ex: /tickets/123 -> 123)
     */
    protected Integer extractIdFromPath(String path) {
        try {
            String[] parts = path.split("/");
            // Dernier segment du path
            String lastPart = parts[parts.length - 1];
            return Integer.parseInt(lastPart);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Vérifie si le chemin correspond à un pattern (ex: /tickets/{id})
     */
    protected boolean matchesPattern(String path, String pattern) {
        String[] pathParts = path.split("/");
        String[] patternParts = pattern.split("/");

        if (pathParts.length != patternParts.length) {
            return false;
        }

        for (int i = 0; i < patternParts.length; i++) {
            if (!patternParts[i].startsWith("{") && !patternParts[i].equals(pathParts[i])) {
                return false;
            }
        }

        return true;
    }

    // ========================================================================
    // Méthodes d'authentification et permissions
    // ========================================================================

    /**
     * Extrait le token d'authentification depuis les headers
     * Format attendu: "Authorization: Bearer <token>"
     */
    protected String extractToken(HttpExchange exchange) {
        String authHeader = exchange.getRequestHeaders().getFirst("Authorization");

        if (authHeader == null || authHeader.isEmpty()) {
            return null;
        }

        // Retirer "Bearer " si présent
        if (authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }

        return authHeader;
    }

    /**
     * Récupère l'utilisateur authentifié depuis le token
     * Retourne null si le token est absent ou invalide
     */
    protected User getUserFromRequest(HttpExchange exchange) {
        String token = extractToken(exchange);

        if (token == null) {
            return null;
        }

        return appState.getUserFromSession(token);
    }

    /**
     * Vérifie que la requête est authentifiée
     * Envoie une erreur 401 si le token est absent ou invalide
     * @return L'utilisateur authentifié, ou null si erreur envoyée
     */
    protected User requireAuth(HttpExchange exchange) throws IOException {
        User user = getUserFromRequest(exchange);

        if (user == null) {
            sendErrorResponse(exchange, 401, "UNAUTHORIZED",
                "Authentification requise. Veuillez vous connecter.");
            return null;
        }

        return user;
    }

    /**
     * Vérifie que l'utilisateur authentifié est un administrateur
     * Envoie une erreur 403 si l'utilisateur n'est pas admin
     * @return true si admin, false si erreur envoyée
     */
    protected boolean requireAdmin(HttpExchange exchange, User user) throws IOException {
        if (!(user instanceof Admin)) {
            sendErrorResponse(exchange, 403, "FORBIDDEN",
                "Cette opération nécessite des privilèges administrateur.");
            return false;
        }
        return true;
    }

    /**
     * Vérifie si l'utilisateur a accès complet (Admin ou Développeur)
     */
    protected boolean hasFullAccess(User user) {
        if (user instanceof Admin) {
            return true;
        }
        String role = user.getRole();
        return "Developpeur".equals(role) || "Admin".equals(role);
    }

    /**
     * Vérifie si l'utilisateur peut modifier un ticket
     * Admin/Développeur peuvent tout modifier, les autres seulement leurs tickets
     */
    protected boolean canEditTicket(User user, Integer ticketCreatorID) {
        if (hasFullAccess(user)) {
            return true;
        }

        if (ticketCreatorID == null) {
            return false;
        }

        return user.getUserID() == ticketCreatorID;
    }
}

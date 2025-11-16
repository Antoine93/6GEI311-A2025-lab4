package api.server.resources;

import com.sun.net.httpserver.HttpExchange;
import api.server.models.*;
import api.server.services.ApplicationState;
import core.entities.User;

import java.io.IOException;

/**
 * AuthResource - Handler pour les endpoints d'authentification
 * POST /api/v1/auth/login
 * GET  /api/v1/auth/session
 * POST /api/v1/auth/logout
 */
public class AuthResource extends BaseResource {

    private final ApplicationState appState = ApplicationState.getInstance();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        try {
            if (path.endsWith("/login") && "POST".equals(method)) {
                handleLogin(exchange);
            } else if (path.endsWith("/session") && "GET".equals(method)) {
                handleGetSession(exchange);
            } else if (path.endsWith("/logout") && "POST".equals(method)) {
                handleLogout(exchange);
            } else {
                sendErrorResponse(exchange, 404, "NOT_FOUND", "Endpoint non trouvé");
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendErrorResponse(exchange, 500, "INTERNAL_ERROR", e.getMessage());
        }
    }

    /**
     * POST /auth/login
     */
    private void handleLogin(HttpExchange exchange) throws IOException {
        String requestBody = readRequestBody(exchange);
        LoginRequest loginRequest = gson.fromJson(requestBody, LoginRequest.class);

        if (loginRequest == null || loginRequest.getUserID() <= 0) {
            sendErrorResponse(exchange, 400, "VALIDATION_ERROR", "UserID invalide");
            return;
        }

        // Trouver l'utilisateur
        User user = appState.findUserById(loginRequest.getUserID());
        if (user == null) {
            sendErrorResponse(exchange, 401, "UNAUTHORIZED", "Utilisateur introuvable");
            return;
        }

        // Générer un token de session
        String token = appState.createSession(user);

        // Convertir User -> UserDTO
        UserDTO userDTO = appState.convertToUserDTO(user);

        // Créer la réponse
        AuthResponse response = new AuthResponse(token, userDTO);

        sendJsonResponse(exchange, 200, response);
        System.out.println("[AUTH] Login réussi pour l'utilisateur: " + user.getName());
    }

    /**
     * GET /auth/session
     */
    private void handleGetSession(HttpExchange exchange) throws IOException {
        String token = exchange.getRequestHeaders().getFirst("Authorization");

        if (token == null || token.isEmpty()) {
            sendErrorResponse(exchange, 401, "UNAUTHORIZED", "Token manquant");
            return;
        }

        // Retirer "Bearer " si présent
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        User user = appState.getUserFromSession(token);
        if (user == null) {
            sendErrorResponse(exchange, 401, "UNAUTHORIZED", "Session invalide ou expirée");
            return;
        }

        UserDTO userDTO = appState.convertToUserDTO(user);
        sendJsonResponse(exchange, 200, userDTO);
    }

    /**
     * POST /auth/logout
     */
    private void handleLogout(HttpExchange exchange) throws IOException {
        String token = exchange.getRequestHeaders().getFirst("Authorization");

        if (token != null && !token.isEmpty()) {
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            appState.invalidateSession(token);
            System.out.println("[AUTH] Session invalidée: " + token);
        }

        sendNoContent(exchange);
    }
}

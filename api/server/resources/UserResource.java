package api.server.resources;

import com.sun.net.httpserver.HttpExchange;
import api.server.models.UserDTO;
import core.entities.User;

import java.io.IOException;
import java.util.List;

/**
 * UserResource - Handler pour les endpoints utilisateurs
 * GET /api/v1/users      [Auth requis]
 * GET /api/v1/users/{id} [Auth requis]
 */
public class UserResource extends BaseResource {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        // Support CORS preflight
        if ("OPTIONS".equals(method)) {
            handleOptionsRequest(exchange);
            return;
        }

        try {
            if ("GET".equals(method)) {
                // Vérifier si c'est /users ou /users/{id}
                if (path.endsWith("/users")) {
                    handleGetAllUsers(exchange);
                } else {
                    handleGetUserById(exchange, path);
                }
            } else {
                sendErrorResponse(exchange, 405, "METHOD_NOT_ALLOWED", "Méthode non autorisée");
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendErrorResponse(exchange, 500, "INTERNAL_ERROR", e.getMessage());
        }
    }

    /**
     * GET /users
     * Authentification requise
     */
    private void handleGetAllUsers(HttpExchange exchange) throws IOException {
        User user = requireAuth(exchange);
        if (user == null) return;

        List<UserDTO> users = appState.getAllUsersDTO();
        sendJsonResponse(exchange, 200, users);
        System.out.println("[USERS] Liste de " + users.size() + " utilisateurs récupérée par " + user.getName());
    }

    /**
     * GET /users/{id}
     * Authentification requise
     */
    private void handleGetUserById(HttpExchange exchange, String path) throws IOException {
        User user = requireAuth(exchange);
        if (user == null) return;

        Integer userId = extractIdFromPath(path);

        if (userId == null) {
            sendErrorResponse(exchange, 400, "VALIDATION_ERROR", "ID utilisateur invalide");
            return;
        }

        UserDTO userDTO = appState.getUserDTOById(userId);

        if (userDTO == null) {
            sendErrorResponse(exchange, 404, "NOT_FOUND", "Utilisateur #" + userId + " introuvable");
            return;
        }

        sendJsonResponse(exchange, 200, userDTO);
        System.out.println("[USERS] Utilisateur #" + userId + " récupéré par " + user.getName());
    }
}

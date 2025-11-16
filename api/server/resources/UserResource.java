package api.server.resources;

import com.sun.net.httpserver.HttpExchange;
import api.server.models.UserDTO;
import api.server.services.ApplicationState;

import java.io.IOException;
import java.util.List;

/**
 * UserResource - Handler pour les endpoints utilisateurs
 * GET /api/v1/users
 * GET /api/v1/users/{id}
 */
public class UserResource extends BaseResource {

    private final ApplicationState appState = ApplicationState.getInstance();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

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
     */
    private void handleGetAllUsers(HttpExchange exchange) throws IOException {
        List<UserDTO> users = appState.getAllUsersDTO();
        sendJsonResponse(exchange, 200, users);
        System.out.println("[USERS] Liste de " + users.size() + " utilisateurs récupérée");
    }

    /**
     * GET /users/{id}
     */
    private void handleGetUserById(HttpExchange exchange, String path) throws IOException {
        Integer userId = extractIdFromPath(path);

        if (userId == null) {
            sendErrorResponse(exchange, 400, "VALIDATION_ERROR", "ID utilisateur invalide");
            return;
        }

        UserDTO user = appState.getUserDTOById(userId);

        if (user == null) {
            sendErrorResponse(exchange, 404, "NOT_FOUND", "Utilisateur #" + userId + " introuvable");
            return;
        }

        sendJsonResponse(exchange, 200, user);
        System.out.println("[USERS] Utilisateur #" + userId + " récupéré");
    }
}

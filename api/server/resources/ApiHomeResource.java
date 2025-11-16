package api.server.resources;

import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * ApiHomeResource - Handler pour la route racine de l'API (/api/v1)
 * Retourne les informations de base sur l'API
 */
public class ApiHomeResource extends BaseResource {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();

        if ("GET".equals(method)) {
            handleGetApiHome(exchange);
        } else {
            sendErrorResponse(exchange, 405, "Method Not Allowed",
                "Seule la méthode GET est autorisée sur cette route");
        }
    }

    /**
     * GET /api/v1 - Retourne les informations de l'API
     */
    private void handleGetApiHome(HttpExchange exchange) throws IOException {
        Map<String, Object> response = new HashMap<>();
        response.put("name", "Ticket Management API");
        response.put("version", "1.0");
        response.put("status", "running");
        response.put("description", "API REST pour le système de gestion de tickets");
        response.put("baseUrl", "http://localhost:8080/api/v1");

        sendJsonResponse(exchange, 200, response);
    }
}

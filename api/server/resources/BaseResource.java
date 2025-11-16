package api.server.resources;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import api.server.models.ErrorResponse;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/**
 * BaseResource - Classe de base pour tous les handlers HTTP
 * Fournit des méthodes utilitaires pour la gestion des réponses JSON
 */
public abstract class BaseResource implements HttpHandler {

    protected static final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .create();

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
}

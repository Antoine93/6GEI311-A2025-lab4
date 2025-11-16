package api.server.models;

import java.util.HashMap;
import java.util.Map;

/**
 * ErrorResponse - Réponse d'erreur standardisée
 * Basé sur le schéma OpenAPI ErrorResponse
 */
public class ErrorResponse {
    private String error;
    private String message;
    private Map<String, Object> details;

    // Constructeur par défaut
    public ErrorResponse() {
        this.details = new HashMap<>();
    }

    public ErrorResponse(String error, String message) {
        this.error = error;
        this.message = message;
        this.details = new HashMap<>();
    }

    public ErrorResponse(String error, String message, Map<String, Object> details) {
        this.error = error;
        this.message = message;
        this.details = details != null ? details : new HashMap<>();
    }

    // Getters et Setters
    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Map<String, Object> getDetails() {
        return details;
    }

    public void setDetails(Map<String, Object> details) {
        this.details = details;
    }

    public void addDetail(String key, Object value) {
        this.details.put(key, value);
    }
}

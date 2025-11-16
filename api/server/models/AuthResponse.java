package api.server.models;

/**
 * AuthResponse - Réponse d'authentification
 * Basé sur le schéma OpenAPI AuthResponse
 */
public class AuthResponse {
    private String token;
    private UserDTO user;

    // Constructeur par défaut
    public AuthResponse() {
    }

    public AuthResponse(String token, UserDTO user) {
        this.token = token;
        this.user = user;
    }

    // Getters et Setters
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public UserDTO getUser() {
        return user;
    }

    public void setUser(UserDTO user) {
        this.user = user;
    }
}

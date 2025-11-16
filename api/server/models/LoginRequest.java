package api.server.models;

/**
 * LoginRequest - Requête d'authentification
 * Basé sur le schéma OpenAPI LoginRequest
 */
public class LoginRequest {
    private int userID;

    // Constructeur par défaut
    public LoginRequest() {
    }

    public LoginRequest(int userID) {
        this.userID = userID;
    }

    // Getters et Setters
    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }
}

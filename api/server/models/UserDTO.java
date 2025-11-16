package api.server.models;

/**
 * UserDTO - Data Transfer Object pour l'API REST
 * Représentation JSON d'un utilisateur
 *
 * Basé sur le schéma OpenAPI UserDTO
 */
public class UserDTO {
    private int userID;
    private String name;
    private String email;
    private String role;
    private boolean isAdmin;

    // Constructeur par défaut (requis pour la désérialisation JSON)
    public UserDTO() {
    }

    public UserDTO(int userID, String name, String email, String role, boolean isAdmin) {
        this.userID = userID;
        this.name = name;
        this.email = email;
        this.role = role;
        this.isAdmin = isAdmin;
    }

    // Getters et Setters
    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public void setAdmin(boolean admin) {
        isAdmin = admin;
    }

    @Override
    public String toString() {
        return "UserDTO{" +
                "userID=" + userID +
                ", name='" + name + '\'' +
                ", role='" + role + '\'' +
                ", isAdmin=" + isAdmin +
                '}';
    }
}

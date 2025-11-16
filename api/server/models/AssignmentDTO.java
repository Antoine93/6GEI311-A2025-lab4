package api.server.models;

/**
 * AssignmentDTO - Requête d'assignation de ticket
 * Basé sur le schéma OpenAPI AssignmentDTO
 */
public class AssignmentDTO {
    private int userID;

    // Constructeur par défaut
    public AssignmentDTO() {
    }

    public AssignmentDTO(int userID) {
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

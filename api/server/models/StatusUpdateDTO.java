package api.server.models;

/**
 * StatusUpdateDTO - Requête de changement de statut
 * Basé sur le schéma OpenAPI StatusUpdateDTO
 */
public class StatusUpdateDTO {
    private String newStatus;

    // Constructeur par défaut
    public StatusUpdateDTO() {
    }

    public StatusUpdateDTO(String newStatus) {
        this.newStatus = newStatus;
    }

    // Getters et Setters
    public String getNewStatus() {
        return newStatus;
    }

    public void setNewStatus(String newStatus) {
        this.newStatus = newStatus;
    }
}

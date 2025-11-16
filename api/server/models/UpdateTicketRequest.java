package api.server.models;

import java.util.List;

/**
 * UpdateTicketRequest - Requête de modification de ticket
 * Basé sur le schéma OpenAPI UpdateTicketRequest
 */
public class UpdateTicketRequest {
    private String title;
    private String priority;
    private List<ContentItemDTO> descriptionContent;

    // Constructeur par défaut
    public UpdateTicketRequest() {
    }

    public UpdateTicketRequest(String title, String priority, List<ContentItemDTO> descriptionContent) {
        this.title = title;
        this.priority = priority;
        this.descriptionContent = descriptionContent;
    }

    // Getters et Setters
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public List<ContentItemDTO> getDescriptionContent() {
        return descriptionContent;
    }

    public void setDescriptionContent(List<ContentItemDTO> descriptionContent) {
        this.descriptionContent = descriptionContent;
    }
}

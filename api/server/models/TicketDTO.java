package api.server.models;

import java.util.List;
import java.util.ArrayList;

/**
 * TicketDTO - Data Transfer Object pour l'API REST
 * Représentation JSON d'un ticket
 *
 * Basé sur le schéma OpenAPI TicketDTO
 */
public class TicketDTO {
    private int ticketID;
    private String title;
    private String status;
    private String priority;
    private String createdByName;
    private String assignedToName;
    private String description;
    private List<ContentItemDTO> descriptionContent;
    private String creationDate;
    private String updateDate;

    // Constructeur par défaut
    public TicketDTO() {
        this.descriptionContent = new ArrayList<>();
    }

    public TicketDTO(int ticketID, String title, String status, String priority,
                     String createdByName, String assignedToName, String description,
                     List<ContentItemDTO> descriptionContent, String creationDate, String updateDate) {
        this.ticketID = ticketID;
        this.title = title;
        this.status = status;
        this.priority = priority;
        this.createdByName = createdByName;
        this.assignedToName = assignedToName;
        this.description = description;
        this.descriptionContent = descriptionContent != null ? descriptionContent : new ArrayList<>();
        this.creationDate = creationDate;
        this.updateDate = updateDate;
    }

    // Getters et Setters
    public int getTicketID() {
        return ticketID;
    }

    public void setTicketID(int ticketID) {
        this.ticketID = ticketID;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getCreatedByName() {
        return createdByName;
    }

    public void setCreatedByName(String createdByName) {
        this.createdByName = createdByName;
    }

    public String getAssignedToName() {
        return assignedToName;
    }

    public void setAssignedToName(String assignedToName) {
        this.assignedToName = assignedToName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<ContentItemDTO> getDescriptionContent() {
        return descriptionContent;
    }

    public void setDescriptionContent(List<ContentItemDTO> descriptionContent) {
        this.descriptionContent = descriptionContent;
    }

    public String getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(String creationDate) {
        this.creationDate = creationDate;
    }

    public String getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(String updateDate) {
        this.updateDate = updateDate;
    }

    @Override
    public String toString() {
        return "TicketDTO{" +
                "ticketID=" + ticketID +
                ", title='" + title + '\'' +
                ", status='" + status + '\'' +
                ", priority='" + priority + '\'' +
                ", createdByName='" + createdByName + '\'' +
                ", assignedToName='" + assignedToName + '\'' +
                '}';
    }
}

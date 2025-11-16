package gui.models;

/**
 * TicketDTO (Data Transfer Object)
 * Object de transfert pour afficher les informations de ticket dans le View
 * Separe le View du Model metier
 */
public class TicketDTO {
    private final int ticketID;
    private final String title;
    private final String status;
    private final String priority;
    private final String createdByName;
    private final String assignedToName;
    private final String description;
    private final String creationDate;

    public TicketDTO(int ticketID, String title, String status, String priority,
                     String createdByName, String assignedToName,
                     String description, String creationDate) {
        this.ticketID = ticketID;
        this.title = title;
        this.status = status;
        this.priority = priority;
        this.createdByName = createdByName;
        this.assignedToName = assignedToName;
        this.description = description;
        this.creationDate = creationDate;
    }

    public int getTicketID() {
        return ticketID;
    }

    public String getTitle() {
        return title;
    }

    public String getStatus() {
        return status;
    }

    public String getPriority() {
        return priority;
    }

    public String getCreatedByName() {
        return createdByName;
    }

    public String getAssignedToName() {
        return assignedToName;
    }

    public String getDescription() {
        return description;
    }

    public String getCreationDate() {
        return creationDate;
    }
}

package core.entities;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import core.content.Content;
import core.exporter.Exporter;
import core.exporter.PDFExporter;


public class Ticket {

    private int ticketID;
    private String title;
    private Content description;  // MODIFIE : String -> Content
    private TicketStatus status;  // MODIFIE : String -> TicketStatus
    private String priority;
    private Date creationDate;
    private Date updateDate;
    private Integer assignedToUserID;
    private Integer createdByUserID;  // AJOUTE : ID de l'utilisateur créateur
    private List<String> comments;  // AJOUTE : Stockage des commentaires

    // Constructeur
    public Ticket(int ticketID, String title, Content description, String priority) {
        this.ticketID = ticketID;
        this.title = title;
        this.description = description;
        this.priority = priority;
        this.status = TicketStatus.OUVERT;  // MODIFIE : Utilisation de l'enum
        this.creationDate = new Date();
        this.updateDate = new Date();
        this.assignedToUserID = null;
        this.createdByUserID = null;  // AJOUTE : Sera défini lors de la création
        this.comments = new ArrayList<>();  // AJOUTE : Initialisation de la liste
    }

    // Methodes
    public void assignTo(int userID) {
        this.assignedToUserID = userID;
        this.status = TicketStatus.ASSIGNE;  // MODIFIE : Utilisation de l'enum
        this.updateDate = new Date();
        System.out.println("Ticket #" + ticketID + " assigne a l'utilisateur ID: " + assignedToUserID);
    }

    /**
     * Met a jour le statut du ticket avec validation de transition
     * @param newStatus Le nouveau statut souhaite
     * @throws IllegalStateException si la transition n'est pas autorisee
     */
    public void updateStatus(TicketStatus newStatus) {
        if (newStatus == null) {
            throw new IllegalArgumentException("Le statut ne peut pas etre null");
        }

        // Validation de la transition
        if (!this.status.canTransitionTo(newStatus)) {
            throw new IllegalStateException(
                "Transition invalide : " + this.status + " -> " + newStatus + ". " +
                "Transitions autorisees : " + this.status.getAvailableTransitions()
            );
        }

        TicketStatus oldStatus = this.status;
        this.status = newStatus;
        this.updateDate = new Date();
        System.out.println("Statut du ticket #" + ticketID + " change : " +
                         oldStatus + " -> " + newStatus);
    }

    /**
     * Ajoute un commentaire au ticket
     * @param comment Le commentaire a ajouter
     */
    public void addComment(String comment) {
        if (comment == null || comment.trim().isEmpty()) {
            throw new IllegalArgumentException("Le commentaire ne peut pas etre vide");
        }

        this.comments.add(comment);
        this.updateDate = new Date();
        System.out.println("Commentaire ajoute au ticket #" + ticketID + ": " + comment);
    }

    /**
     * Retourne tous les commentaires du ticket
     * @return Liste des commentaires (copie defensive)
     */
    public List<String> getComments() {
        return new ArrayList<>(comments);
    }

    /**
     * Affiche tous les commentaires du ticket
     */
    public void displayComments() {
        if (comments.isEmpty()) {
            System.out.println("Aucun commentaire pour le ticket #" + ticketID);
            return;
        }

        System.out.println("\n" + "=".repeat(60));
        System.out.println("COMMENTAIRES DU TICKET #" + ticketID);
        System.out.println("=".repeat(60));
        for (int i = 0; i < comments.size(); i++) {
            System.out.println("[" + (i + 1) + "] " + comments.get(i));
        }
        System.out.println("=".repeat(60) + "\n");
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

    public Content getDescription() {
        return description;
    }

    public void setDescription(Content description) {
        this.description = description;
        this.updateDate = new Date();
    }

    /**
     * Affiche la description dans la plateforme
     */
    public void displayDescription() {
        if (description != null) {
            System.out.println("\n" + "=".repeat(60));
            System.out.println("DESCRIPTION DU TICKET #" + ticketID);
            System.out.println("=".repeat(60));
            System.out.println(description.display());
            System.out.println("=".repeat(60) + "\n");
        } else {
            System.out.println("[Aucune description]");
        }
    }

    /**
     * Exporte la description vers un format specifique
     * @param exporter La strategie d'export a utiliser
     * @return Le contenu exporte
     */
    public String exportTo(Exporter exporter) {
        if (description == null) {
            return "[Aucune description a exporter]";
        }
        return exporter.export(description);
    }

    /**
     * Exporte la description en PDF (methode de convenance)
     * @return Le PDF genere
     */
    public String exportToPDF() {
        PDFExporter pdfExporter = new PDFExporter();
        String pdfContent = exportTo(pdfExporter);
        String fileName = pdfExporter.generateFileName(ticketID);

        System.out.println("\n[OK] Export PDF genere : " + fileName);
        return pdfContent;
    }

    public TicketStatus getStatus() {
        return status;
    }

    public void setStatus(TicketStatus status) {
        this.status = status;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public Date getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(Date updateDate) {
        this.updateDate = updateDate;
    }

    public Integer getAssignedToUserID() {
        return assignedToUserID;
    }

    public void setAssignedToUserID(Integer assignedToUserID) {
        this.assignedToUserID = assignedToUserID;
    }

    public Integer getCreatedByUserID() {
        return createdByUserID;
    }

    public void setCreatedByUserID(Integer createdByUserID) {
        this.createdByUserID = createdByUserID;
    }

    @Override
    public String toString() {
        return "Ticket{" +
                "ID=" + ticketID +
                ", titre='" + title + '\'' +
                ", statut='" + status + '\'' +
                ", priorite='" + priority + '\'' +
                ", assigneA=" + (assignedToUserID != null ? assignedToUserID : "non assigne") +
                ", creation=" + creationDate +
                ", maj=" + updateDate +
                '}';
    }
}

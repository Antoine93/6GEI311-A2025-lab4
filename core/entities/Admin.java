package core.entities;
import java.util.List;

/**
 * Classe Admin - Represente un administrateur du systeme
 * Admin herite de User et possede des permissions supplementaires
 * (assignation de tickets, fermeture, consultation globale)
 */
public class Admin extends User {

    /**
     * Constructeur Admin
     * @param adminID Identifiant unique de l'administrateur
     * @param name Nom de l'administrateur
     * @param email Email de l'administrateur
     */
    public Admin(int adminID, String name, String email) {
        super(adminID, name, email, "Admin");
    }

    /**
     * Assigne un ticket a un utilisateur specifique
     * @param ticket Le ticket a assigner
     * @param userID L'identifiant de l'utilisateur a qui assigner le ticket
     */
    public void assignTicket(Ticket ticket, int userID) {
        ticket.assignTo(userID);
        System.out.println("Admin " + getName() + " assigne le ticket " +
                ticket.getTicketID() + " a l'utilisateur ID: " + userID);
    }

    /**
     * Ferme un ticket (passage au statut TERMINE)
     * @param ticket Le ticket a fermer
     */
    public void closeTicket(Ticket ticket) {
        ticket.updateStatus(TicketStatus.TERMINE);
        System.out.println("Admin " + getName() + " ferme le ticket " +
                ticket.getTicketID());
    }

    /**
     * Consulte et affiche tous les tickets du systeme
     * @param tickets Liste de tous les tickets
     * @return La liste des tickets
     */
    public List<Ticket> viewAllTickets(List<Ticket> tickets) {
        System.out.println("Admin " + getName() + " consulte tous les tickets");
        for (Ticket ticket : tickets) {
            System.out.println("  - Ticket #" + ticket.getTicketID() +
                    ": " + ticket.getTitle() + " [" + ticket.getStatus() + "]");
        }
        return tickets;
    }
}

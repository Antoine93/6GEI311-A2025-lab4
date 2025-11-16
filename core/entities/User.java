package core.entities;

import core.content.Content;

public class User {

    private int userID;
    private String name;
    private String email;
    private String role;
    private static int ticketIDCounter = 1000;

    public User(int userID, String name, String email, String role) {
        this.userID = userID;
        this.name = name;
        this.email = email;
        this.role = role;
    }

    public Ticket createTicket(String title, Content description, String priority){
        ticketIDCounter++;
        Ticket newTicket = new Ticket(ticketIDCounter, title, description, priority);
        newTicket.setCreatedByUserID(this.userID);  // Définir le créateur du ticket
        System.out.println("User " + name + " cree le ticket #" + newTicket.getTicketID() + ": " + title);
        return newTicket;
    }

    public void viewTicket(Ticket ticket) {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("User " + name + " consulte le ticket #" + ticket.getTicketID());
        System.out.println("=".repeat(60));
        System.out.println("Titre: " + ticket.getTitle());
        System.out.println("Statut: " + ticket.getStatus());
        System.out.println("Priorite: " + ticket.getPriority());
        System.out.println("Creation: " + ticket.getCreationDate());
        System.out.println("\nDescription:");
        ticket.displayDescription();
    }

    public void updateTicket(Ticket ticket) {
        System.out.println("User " + name + " met a jour le ticket #" + ticket.getTicketID());
        ticket.setUpdateDate(new java.util.Date());
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
}

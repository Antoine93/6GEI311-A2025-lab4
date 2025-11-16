package gui.controllers;

import java.util.*;

import core.content.*;
import core.entities.*;

/**
 * ApplicationState - Singleton
 * Gere l'etat global de l'application (utilisateur connecte, liste des tickets)
 * REFACTORISE: Implémente le pattern Observer pour notifier les changements d'état
 */
public class ApplicationState {
    private static ApplicationState instance;

    private User currentUser;
    private List<Ticket> allTickets;
    private List<User> allUsers;
    private List<TicketStateListener> listeners;  // NOUVEAU: Support du pattern Observer

    private ApplicationState() {
        allTickets = new ArrayList<>();
        allUsers = new ArrayList<>();
        listeners = new ArrayList<>();  // NOUVEAU
        initTestData();
    }

    public static ApplicationState getInstance() {
        if (instance == null) {
            instance = new ApplicationState();
        }
        return instance;
    }

    /**
     * Enregistre un listener pour recevoir les notifications de changement d'état
     */
    public void addListener(TicketStateListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    /**
     * Désenregistre un listener
     */
    public void removeListener(TicketStateListener listener) {
        listeners.remove(listener);
    }

    /**
     * Notifie tous les listeners qu'un changement de tickets a eu lieu
     */
    private void notifyTicketsChanged() {
        for (TicketStateListener listener : listeners) {
            listener.onTicketsChanged();
        }
    }

    /**
     * Notifie tous les listeners qu'un changement d'utilisateur a eu lieu
     */
    private void notifyCurrentUserChanged() {
        for (TicketStateListener listener : listeners) {
            listener.onCurrentUserChanged();
        }
    }

    /**
     * Initialise les donnees de test
     */
    private void initTestData() {
        // Creer des utilisateurs de test
        currentUser = new User(1, "Utilisateur1", "utilisateur1@uqac.ca", "Developpeur");
        allUsers.add(currentUser);
        allUsers.add(new User(2, "Utilisateur2", "utilisateur2@uqac.ca", "Testeur"));
        allUsers.add(new Admin(100, "Utilisateur3", "utilisateur3@uqac.ca"));

        // Creer quelques tickets de test
        TextContent desc1 = new TextContent(
            "L'application crash lorsqu'on clique sur le bouton de connexion apres 3 tentatives echouees."
        );
        Ticket ticket1 = currentUser.createTicket(
            "Bug critique - Crash a la connexion",
            desc1,
            "Haute"
        );
        allTickets.add(ticket1);

        TextContent desc2 = new TextContent(
            "L'interface utilisateur n'est pas responsive sur mobile. Les boutons sont trop petits."
        );
        Ticket ticket2 = currentUser.createTicket(
            "Amelioration UI - Responsive design",
            desc2,
            "Moyenne"
        );
        allTickets.add(ticket2);

        TextContent desc3 = new TextContent(
            "Le systeme 2FA ne valide pas correctement les codes apres plusieurs tentatives."
        );
        Ticket ticket3 = currentUser.createTicket(
            "Bug 2FA - Validation incorrecte",
            desc3,
            "Critique"
        );
        allTickets.add(ticket3);
    }

    // Getters
    public User getCurrentUser() {
        return currentUser;
    }

    public List<Ticket> getAllTickets() {
        return new ArrayList<>(allTickets);
    }

    public List<User> getAllUsers() {
        return new ArrayList<>(allUsers);
    }

    // Methodes de manipulation
    public void addTicket(Ticket ticket) {
        allTickets.add(ticket);
        notifyTicketsChanged();  // NOUVEAU: Notifier les observers
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
        notifyCurrentUserChanged();  // NOUVEAU: Notifier les observers
    }
}

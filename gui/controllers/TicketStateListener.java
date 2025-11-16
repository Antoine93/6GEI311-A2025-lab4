package gui.controllers;

/**
 * TicketStateListener
 * Interface pour le pattern Observer
 * Permet aux vues d'être notifiées automatiquement des changements d'état
 */
public interface TicketStateListener {

    /**
     * Appelée quand la liste des tickets a changé
     * (ticket créé, modifié, supprimé, statut changé, etc.)
     */
    void onTicketsChanged();

    /**
     * Appelée quand l'utilisateur courant a changé
     * (login, logout, changement d'utilisateur)
     */
    void onCurrentUserChanged();
}

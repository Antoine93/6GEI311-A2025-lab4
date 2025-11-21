package gui.controllers;

import java.util.*;

/**
 * ApplicationState - Singleton (DÉPRÉCIÉ en Lab 4)
 *
 * IMPORTANT: Ce fichier est conservé pour compatibilité, mais n'est PLUS utilisé
 * dans l'architecture REST du Lab 4.
 *
 * Dans Lab 2-3, cette classe gérait l'état global de l'application (utilisateur connecté,
 * liste des tickets) en mémoire côté client.
 *
 * Dans Lab 4, l'état est maintenant géré côté serveur (api.server.services.ApplicationState)
 * et le client (GUI) communique via REST API (RestApiClient).
 *
 * Ce fichier est conservé uniquement pour :
 * - Éviter les erreurs de compilation si du code legacy y fait référence
 * - Documenter la transition architecturale
 *
 * NOUVEAU FLUX (Lab 4):
 * GUI View → TicketController → RestApiClient → API Server → Server ApplicationState → Core Entities
 *
 * ANCIEN FLUX (Lab 2-3):
 * GUI View → TicketController → Client ApplicationState (ce fichier) → Core Entities
 */
public class ApplicationState {
    private static ApplicationState instance;

    private ApplicationState() {
        // Constructeur vide - cette classe n'est plus utilisée
    }

    public static ApplicationState getInstance() {
        if (instance == null) {
            instance = new ApplicationState();
        }
        return instance;
    }

    /**
     * @deprecated Utiliser TicketController.login() à la place
     */
    @Deprecated
    public void addListener(TicketStateListener listener) {
        System.err.println("AVERTISSEMENT: ApplicationState.addListener() est déprécié. " +
                         "L'état est maintenant géré côté serveur.");
    }

    /**
     * @deprecated Utiliser TicketController.logout() à la place
     */
    @Deprecated
    public void removeListener(TicketStateListener listener) {
        System.err.println("AVERTISSEMENT: ApplicationState.removeListener() est déprécié.");
    }

    /**
     * @deprecated Utiliser TicketController.getCurrentUser() à la place
     */
    @Deprecated
    public Object getCurrentUser() {
        System.err.println("AVERTISSEMENT: ApplicationState.getCurrentUser() est déprécié. " +
                         "Utiliser TicketController.getCurrentUser()");
        return null;
    }

    /**
     * @deprecated Utiliser RestApiClient.getAllTickets() via TicketController à la place
     */
    @Deprecated
    public List<?> getAllTickets() {
        System.err.println("AVERTISSEMENT: ApplicationState.getAllTickets() est déprécié. " +
                         "Utiliser TicketController.getAllTickets()");
        return new ArrayList<>();
    }

    /**
     * @deprecated Utiliser RestApiClient.getAllUsers() via TicketController à la place
     */
    @Deprecated
    public List<?> getAllUsers() {
        System.err.println("AVERTISSEMENT: ApplicationState.getAllUsers() est déprécié. " +
                         "Utiliser TicketController.getAllUsers()");
        return new ArrayList<>();
    }

    /**
     * @deprecated Utiliser RestApiClient.createTicket() via TicketController à la place
     */
    @Deprecated
    public void addTicket(Object ticket) {
        System.err.println("AVERTISSEMENT: ApplicationState.addTicket() est déprécié. " +
                         "Utiliser TicketController.createTicketWithContentItems()");
    }

    /**
     * @deprecated Utiliser TicketController.login() à la place
     */
    @Deprecated
    public void setCurrentUser(Object user) {
        System.err.println("AVERTISSEMENT: ApplicationState.setCurrentUser() est déprécié. " +
                         "Utiliser TicketController.login()");
    }
}

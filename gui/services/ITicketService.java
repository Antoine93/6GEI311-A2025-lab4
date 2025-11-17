package gui.services;

import gui.models.*;
import core.content.Content;
import java.util.List;

/**
 * ITicketService - Interface d'abstraction pour les opérations sur les tickets
 *
 * Permet de découpler la GUI de l'implémentation concrète (locale ou REST)
 *
 * Implementations:
 * - RestTicketService: Appels HTTP vers l'API REST
 * - LocalTicketService: Accès direct à ApplicationState (pour tests)
 */
public interface ITicketService {

    // ========================================================================
    // Authentification
    // ========================================================================

    /**
     * Authentifie un utilisateur
     * @param userID ID de l'utilisateur
     * @return UserDTO de l'utilisateur authentifié
     * @throws ServiceException si l'authentification échoue
     */
    UserDTO login(int userID) throws ServiceException;

    /**
     * Déconnecte l'utilisateur actuel
     * @throws ServiceException si la déconnexion échoue
     */
    void logout() throws ServiceException;

    /**
     * Vérifie si l'utilisateur est authentifié
     * @return true si une session valide existe
     */
    boolean isAuthenticated();

    /**
     * Retourne l'utilisateur actuellement connecté
     * @return UserDTO de l'utilisateur actuel ou null si non connecté
     */
    UserDTO getCurrentUser();

    // ========================================================================
    // Utilisateurs
    // ========================================================================

    /**
     * Récupère tous les utilisateurs
     * @return Liste de tous les utilisateurs
     * @throws ServiceException si la récupération échoue
     */
    List<UserDTO> getAllUsers() throws ServiceException;

    /**
     * Récupère un utilisateur par son ID
     * @param userID ID de l'utilisateur
     * @return UserDTO de l'utilisateur ou null si non trouvé
     * @throws ServiceException si la récupération échoue
     */
    UserDTO getUserById(int userID) throws ServiceException;

    // ========================================================================
    // Tickets (CRUD)
    // ========================================================================

    /**
     * Récupère tous les tickets (filtrés selon permissions)
     * @return Liste de tickets
     * @throws ServiceException si la récupération échoue
     */
    List<TicketDTO> getAllTickets() throws ServiceException;

    /**
     * Récupère un ticket par son ID
     * @param ticketID ID du ticket
     * @return TicketDTO ou null si non trouvé
     * @throws ServiceException si la récupération échoue
     */
    TicketDTO getTicketById(int ticketID) throws ServiceException;

    /**
     * Crée un nouveau ticket
     * @param title Titre du ticket
     * @param contentItems Contenu structuré (ContentItemDTO)
     * @param priority Priorité
     * @return TicketDTO du ticket créé
     * @throws ServiceException si la création échoue
     */
    TicketDTO createTicket(String title, List<ContentItemDTO> contentItems, String priority)
        throws ServiceException;

    /**
     * Modifie un ticket existant
     * @param ticketID ID du ticket à modifier
     * @param title Nouveau titre (peut être null pour ne pas modifier)
     * @param priority Nouvelle priorité (peut être null)
     * @param contentItems Nouveau contenu (peut être null)
     * @return TicketDTO du ticket modifié
     * @throws ServiceException si la modification échoue
     */
    TicketDTO updateTicket(int ticketID, String title, String priority,
                          List<ContentItemDTO> contentItems) throws ServiceException;

    /**
     * Supprime un ticket
     * @param ticketID ID du ticket à supprimer
     * @throws ServiceException si la suppression échoue
     */
    void deleteTicket(int ticketID) throws ServiceException;

    // ========================================================================
    // Commentaires
    // ========================================================================

    /**
     * Récupère les commentaires d'un ticket
     * @param ticketID ID du ticket
     * @return Liste des commentaires
     * @throws ServiceException si la récupération échoue
     */
    List<String> getTicketComments(int ticketID) throws ServiceException;

    /**
     * Ajoute un commentaire à un ticket
     * @param ticketID ID du ticket
     * @param comment Texte du commentaire
     * @return Le commentaire ajouté
     * @throws ServiceException si l'ajout échoue
     */
    String addComment(int ticketID, String comment) throws ServiceException;

    // ========================================================================
    // Gestion du statut
    // ========================================================================

    /**
     * Récupère les transitions disponibles pour un ticket
     * @param ticketID ID du ticket
     * @return Liste des statuts vers lesquels une transition est possible
     * @throws ServiceException si la récupération échoue
     */
    List<String> getAvailableTransitions(int ticketID) throws ServiceException;

    /**
     * Change le statut d'un ticket
     * @param ticketID ID du ticket
     * @param newStatus Nouveau statut (OUVERT, ASSIGNE, VALIDATION, TERMINE, FERME)
     * @return TicketDTO du ticket modifié
     * @throws ServiceException si le changement échoue
     */
    TicketDTO changeTicketStatus(int ticketID, String newStatus) throws ServiceException;

    // ========================================================================
    // Assignation
    // ========================================================================

    /**
     * Assigne un ticket à un utilisateur
     * @param ticketID ID du ticket
     * @param userID ID de l'utilisateur à qui assigner
     * @return TicketDTO du ticket modifié
     * @throws ServiceException si l'assignation échoue
     */
    TicketDTO assignTicket(int ticketID, int userID) throws ServiceException;

    // ========================================================================
    // Export
    // ========================================================================

    /**
     * Exporte un ticket en format PDF (texte simulé)
     * @param ticketID ID du ticket
     * @return Contenu PDF sous forme de texte
     * @throws ServiceException si l'export échoue
     */
    String exportTicketToPDF(int ticketID) throws ServiceException;
}

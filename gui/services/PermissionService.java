package gui.services;

import gui.models.UserDTO;

/**
 * PermissionService
 * Service de gestion centralisée des permissions utilisateurs
 * Évite de dupliquer la logique de permissions dans les vues
 */
public class PermissionService {

    /**
     * Énumération des permissions disponibles dans l'application
     */
    public enum Permission {
        CREATE_TICKET,      // Créer un nouveau ticket
        EDIT_TICKET,        // Modifier un ticket
        DELETE_TICKET,      // Supprimer un ticket
        ASSIGN_TICKET,      // Assigner un ticket à un utilisateur
        CHANGE_STATUS,      // Changer le statut d'un ticket
        ADD_COMMENT,        // Ajouter un commentaire
        EXPORT_TICKET,      // Exporter un ticket
        VIEW_ALL_TICKETS    // Voir tous les tickets (sinon uniquement les siens)
    }

    /**
     * Vérifie si un utilisateur a une permission spécifique
     *
     * @param user L'utilisateur à vérifier
     * @param permission La permission demandée
     * @return true si l'utilisateur a la permission, false sinon
     */
    public static boolean hasPermission(UserDTO user, Permission permission) {
        if (user == null) {
            return false;
        }

        boolean hasFullAccess = user.hasFullAccess();

        switch (permission) {
            case CREATE_TICKET:
                // Tous les utilisateurs peuvent créer des tickets
                return true;

            case EDIT_TICKET:
            case ASSIGN_TICKET:
            case CHANGE_STATUS:
            case ADD_COMMENT:
            case EXPORT_TICKET:
            case VIEW_ALL_TICKETS:
                // Administrateurs et Développeurs ont un accès complet
                return hasFullAccess;

            case DELETE_TICKET:
                // Seuls les administrateurs peuvent supprimer
                return user.isAdmin();

            default:
                return false;
        }
    }

    /**
     * Vérifie si un utilisateur peut modifier un ticket spécifique
     * Les admins et développeurs peuvent modifier tous les tickets
     * Les autres utilisateurs ne peuvent modifier que leurs propres tickets
     *
     * @param user L'utilisateur
     * @param ticketCreatorID L'ID du créateur du ticket
     * @return true si l'utilisateur peut modifier ce ticket
     */
    public static boolean canEditTicket(UserDTO user, int ticketCreatorID) {
        if (user == null) {
            return false;
        }

        // Admin et Développeur peuvent tout modifier
        if (user.hasFullAccess()) {
            return true;
        }

        // Les autres ne peuvent modifier que leurs propres tickets
        return user.getUserID() == ticketCreatorID;
    }

    /**
     * Vérifie si un utilisateur peut supprimer un ticket spécifique
     *
     * @param user L'utilisateur
     * @return true si l'utilisateur peut supprimer des tickets
     */
    public static boolean canDeleteTicket(UserDTO user) {
        return user != null && user.isAdmin();
    }
}

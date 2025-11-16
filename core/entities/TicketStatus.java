package core.entities;

/**
 * Enumeration TicketStatus - Gestion type-safe des statuts de tickets
 *
 * Cycle de vie d'un ticket :
 * OUVERT -> ASSIGNE -> VALIDATION -> TERMINE
 *        ↘ FERME
 *
 * Transitions autorisees :
 * - OUVERT -> ASSIGNE, FERME
 * - ASSIGNE -> VALIDATION, FERME
 * - VALIDATION -> TERMINE, ASSIGNE (retour pour correction)
 * - TERMINE -> (etat final)
 * - FERME -> (etat final)
 */
public enum TicketStatus {
    /** Ticket cree, en attente d'assignation */
    OUVERT("Ouvert"),

    /** Ticket assigne a un developpeur */
    ASSIGNE("Assigne"),

    /** Ticket en cours de validation par l'equipe */
    VALIDATION("En validation"),

    /** Ticket resolu et valide */
    TERMINE("Termine"),

    /** Ticket ferme sans resolution (non prioritaire, specifique utilisateur) */
    FERME("Ferme");

    private final String displayName;

    TicketStatus(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Retourne le nom d'affichage du statut
     * @return Nom formate pour l'affichage
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Verifie si une transition vers un nouveau statut est valide
     * @param newStatus Le nouveau statut souhaite
     * @return true si la transition est autorisee, false sinon
     */
    public boolean canTransitionTo(TicketStatus newStatus) {
        if (newStatus == null) {
            return false;
        }

        switch (this) {
            case OUVERT:
                // Depuis OUVERT : peut aller vers ASSIGNE ou FERME
                return newStatus == ASSIGNE || newStatus == FERME;

            case ASSIGNE:
                // Depuis ASSIGNE : peut aller vers VALIDATION ou FERME
                return newStatus == VALIDATION || newStatus == FERME;

            case VALIDATION:
                // Depuis VALIDATION : peut aller vers TERMINE ou retourner ASSIGNE
                return newStatus == TERMINE || newStatus == ASSIGNE;

            case TERMINE:
            case FERME:
                // Etats finaux : aucune transition autorisee
                return false;

            default:
                return false;
        }
    }

    /**
     * Retourne la description des transitions possibles depuis ce statut
     * @return Description textuelle des transitions
     */
    public String getAvailableTransitions() {
        switch (this) {
            case OUVERT:
                return "ASSIGNE, FERME";
            case ASSIGNE:
                return "VALIDATION, FERME";
            case VALIDATION:
                return "TERMINE, ASSIGNE";
            case TERMINE:
            case FERME:
                return "(aucune - etat final)";
            default:
                return "(inconnu)";
        }
    }

    /**
     * Retourne la liste des statuts vers lesquels une transition est possible
     * @return Liste des statuts de destination possibles
     */
    public java.util.List<TicketStatus> getAvailableTransitionsList() {
        java.util.List<TicketStatus> transitions = new java.util.ArrayList<>();

        switch (this) {
            case OUVERT:
                transitions.add(ASSIGNE);
                transitions.add(FERME);
                break;
            case ASSIGNE:
                transitions.add(VALIDATION);
                transitions.add(FERME);
                break;
            case VALIDATION:
                transitions.add(TERMINE);
                transitions.add(ASSIGNE);
                break;
            case TERMINE:
            case FERME:
                // Etats finaux : liste vide
                break;
        }

        return transitions;
    }

    @Override
    public String toString() {
        return displayName;
    }
}

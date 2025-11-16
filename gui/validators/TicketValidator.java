package gui.validators;

import java.util.Arrays;
import java.util.List;

/**
 * TicketValidator
 * Centralise toutes les règles de validation pour les tickets
 * Permet de maintenir les règles métier en un seul endroit
 */
public class TicketValidator {
    private static final int MAX_TITLE_LENGTH = 100;
    private static final List<String> VALID_PRIORITIES = Arrays.asList("Basse", "Moyenne", "Haute", "Critique");

    /**
     * Résultat d'une validation
     */
    public static class ValidationResult {
        private final boolean valid;
        private final String errorMessage;

        public ValidationResult(boolean valid, String errorMessage) {
            this.valid = valid;
            this.errorMessage = errorMessage;
        }

        public boolean isValid() {
            return valid;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public static ValidationResult success() {
            return new ValidationResult(true, null);
        }

        public static ValidationResult error(String message) {
            return new ValidationResult(false, message);
        }
    }

    /**
     * Valide le titre d'un ticket
     */
    public static ValidationResult validateTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            return ValidationResult.error("Le titre ne peut pas être vide");
        }

        if (title.length() > MAX_TITLE_LENGTH) {
            return ValidationResult.error(
                "Le titre ne peut pas dépasser " + MAX_TITLE_LENGTH + " caractères");
        }

        return ValidationResult.success();
    }

    /**
     * Valide la priorité d'un ticket
     */
    public static ValidationResult validatePriority(String priority) {
        if (priority == null || priority.trim().isEmpty()) {
            return ValidationResult.error("La priorité ne peut pas être vide");
        }

        if (!VALID_PRIORITIES.contains(priority)) {
            return ValidationResult.error("Priorité invalide. Valeurs acceptées: " +
                String.join(", ", VALID_PRIORITIES));
        }

        return ValidationResult.success();
    }

    /**
     * Retourne la liste des priorités valides
     */
    public static List<String> getValidPriorities() {
        return VALID_PRIORITIES;
    }
}

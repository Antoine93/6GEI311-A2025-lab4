package gui.utils;

import javax.swing.*;
import java.awt.*;

/**
 * ErrorHandler
 * Gestionnaire centralisé des messages d'erreur et dialogues utilisateur
 * Uniformise l'affichage des messages dans toute l'application
 */
public class ErrorHandler {

    /**
     * Affiche une erreur utilisateur (message simple)
     *
     * @param parent Composant parent pour le dialogue
     * @param message Message d'erreur à afficher
     */
    public static void showUserError(Component parent, String message) {
        JOptionPane.showMessageDialog(parent,
            message,
            "Erreur",
            JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Affiche une erreur technique avec détails de l'exception
     *
     * @param parent Composant parent pour le dialogue
     * @param message Message d'erreur principal
     * @param ex Exception à détailler
     */
    public static void showTechnicalError(Component parent, String message, Exception ex) {
        String detailedMessage = message + "\n\n" +
            "Détails techniques:\n" + ex.getMessage();

        JOptionPane.showMessageDialog(parent,
            detailedMessage,
            "Erreur technique",
            JOptionPane.ERROR_MESSAGE);

        // Log pour le debugging
        ex.printStackTrace();
    }

    /**
     * Affiche un avertissement
     *
     * @param parent Composant parent pour le dialogue
     * @param message Message d'avertissement
     */
    public static void showWarning(Component parent, String message) {
        JOptionPane.showMessageDialog(parent,
            message,
            "Attention",
            JOptionPane.WARNING_MESSAGE);
    }

    /**
     * Affiche une information
     *
     * @param parent Composant parent pour le dialogue
     * @param message Message d'information
     */
    public static void showInfo(Component parent, String message) {
        JOptionPane.showMessageDialog(parent,
            message,
            "Information",
            JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Affiche un message de succès
     *
     * @param parent Composant parent pour le dialogue
     * @param message Message de succès
     */
    public static void showSuccess(Component parent, String message) {
        JOptionPane.showMessageDialog(parent,
            message,
            "Succès",
            JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Demande une confirmation à l'utilisateur
     *
     * @param parent Composant parent pour le dialogue
     * @param message Message de confirmation
     * @return true si l'utilisateur a confirmé, false sinon
     */
    public static boolean confirm(Component parent, String message) {
        int result = JOptionPane.showConfirmDialog(parent,
            message,
            "Confirmation",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE);
        return result == JOptionPane.YES_OPTION;
    }

    /**
     * Demande une confirmation avec un titre personnalisé
     *
     * @param parent Composant parent pour le dialogue
     * @param title Titre du dialogue
     * @param message Message de confirmation
     * @return true si l'utilisateur a confirmé, false sinon
     */
    public static boolean confirm(Component parent, String title, String message) {
        int result = JOptionPane.showConfirmDialog(parent,
            message,
            title,
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE);
        return result == JOptionPane.YES_OPTION;
    }

    /**
     * Demande une saisie de texte à l'utilisateur
     *
     * @param parent Composant parent pour le dialogue
     * @param title Titre du dialogue
     * @param message Message de la demande
     * @return Le texte saisi, ou null si annulé
     */
    public static String promptText(Component parent, String title, String message) {
        return JOptionPane.showInputDialog(parent, message, title, JOptionPane.QUESTION_MESSAGE);
    }
}

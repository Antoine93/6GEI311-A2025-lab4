package gui.services;

import javax.swing.*;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;

/**
 * FileService
 * Service de gestion des opérations de fichiers
 * Centralise la logique d'I/O pour la découpler des vues
 */
public class FileService {

    /**
     * Demande à l'utilisateur de sélectionner un emplacement de sauvegarde
     * et sauvegarde le contenu dans un fichier texte
     *
     * @param parent Fenêtre parente pour le dialogue
     * @param suggestedFileName Nom de fichier suggéré
     * @param content Contenu à sauvegarder
     * @return Le chemin du fichier sauvegardé, ou null si l'utilisateur a annulé
     * @throws Exception en cas d'erreur d'écriture
     */
    public static Path saveTextFile(JFrame parent, String suggestedFileName, String content) throws Exception {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Sauvegarder le rapport");
        chooser.setSelectedFile(new File(suggestedFileName));

        int result = chooser.showSaveDialog(parent);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            Path path = file.toPath();
            Files.write(path, content.getBytes(StandardCharsets.UTF_8));
            return path;
        }

        return null;  // Annulé par l'utilisateur
    }

    /**
     * Génère un nom de fichier standardisé pour l'export d'un ticket
     *
     * @param ticketID ID du ticket
     * @return Nom de fichier suggéré
     */
    public static String generateTicketReportFileName(int ticketID) {
        return "ticket_" + ticketID + "_rapport.txt";
    }

    /**
     * Demande à l'utilisateur de sélectionner un fichier image
     *
     * @param parent Fenêtre parente pour le dialogue
     * @return Le chemin du fichier sélectionné, ou null si annulé
     */
    public static String selectImageFile(JFrame parent) {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Sélectionner une image");
        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
            "Images (JPG, PNG, GIF)", "jpg", "jpeg", "png", "gif"));

        int result = chooser.showOpenDialog(parent);
        if (result == JFileChooser.APPROVE_OPTION) {
            return chooser.getSelectedFile().getAbsolutePath();
        }

        return null;
    }

    /**
     * Demande à l'utilisateur de sélectionner un fichier vidéo
     *
     * @param parent Fenêtre parente pour le dialogue
     * @return Le chemin du fichier sélectionné, ou null si annulé
     */
    public static String selectVideoFile(JFrame parent) {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Sélectionner une vidéo");
        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
            "Vidéos (MP4, AVI, MOV)", "mp4", "avi", "mov", "mkv"));

        int result = chooser.showOpenDialog(parent);
        if (result == JFileChooser.APPROVE_OPTION) {
            return chooser.getSelectedFile().getAbsolutePath();
        }

        return null;
    }
}

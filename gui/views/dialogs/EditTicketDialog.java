package gui.views.dialogs;

import gui.controllers.TicketController;
import gui.models.TicketDTO;
import gui.models.ContentItemDTO;
import javax.swing.*;

import core.content.Content;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * EditTicketDialog
 * Dialogue modal pour modifier un ticket existant
 * Permet de modifier le titre, la priorite et la description
 */
public class EditTicketDialog extends JDialog {
    private int ticketID;
    private TicketController ticketController;
    private JTextField titleField;
    private JComboBox<String> priorityCombo;
    private ContentBuilderPanel contentBuilder;
    private JButton saveButton;
    private JButton cancelButton;

    public EditTicketDialog(JFrame parent, int ticketID) {
        super(parent, "Modifier le ticket #" + ticketID, true);
        this.ticketID = ticketID;
        this.ticketController = new TicketController();
        initComponents();
        loadTicketData();
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setSize(700, 550);
        setLocationRelativeTo(getParent());

        // CENTER: Formulaire
        JPanel formPanel = createFormPanel();
        add(formPanel, BorderLayout.CENTER);

        // SOUTH: Boutons
        JPanel buttonPanel = createButtonPanel();
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private JPanel createFormPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Titre
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        panel.add(new JLabel("Titre:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        titleField = new JTextField(30);
        panel.add(titleField, gbc);

        // Priorite
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        panel.add(new JLabel("Priorite:"), gbc);

        gbc.gridx = 1;
        priorityCombo = new JComboBox<>(new String[]{"Basse", "Moyenne", "Haute", "Critique"});
        panel.add(priorityCombo, gbc);

        // Description (ContentBuilderPanel)
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        panel.add(new JLabel("Description:"), gbc);

        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;
        contentBuilder = new ContentBuilderPanel();
        panel.add(contentBuilder, gbc);

        // Note
        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        JLabel noteLabel = new JLabel(
            "<html><i>Note: La nouvelle description remplacera l'ancienne</i></html>");
        noteLabel.setForeground(Color.GRAY);
        panel.add(noteLabel, gbc);

        return panel;
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));

        saveButton = new JButton("Sauvegarder");
        saveButton.addActionListener(e -> onSave());

        cancelButton = new JButton("Annuler");
        cancelButton.addActionListener(e -> dispose());

        panel.add(saveButton);
        panel.add(cancelButton);

        getRootPane().setDefaultButton(saveButton);

        return panel;
    }

    /**
     * Charge les donnees du ticket dans le formulaire
     */
    private void loadTicketData() {
        TicketDTO ticket = ticketController.getTicketById(ticketID);
        if (ticket == null) {
            JOptionPane.showMessageDialog(this,
                "Erreur: Ticket introuvable",
                "Erreur",
                JOptionPane.ERROR_MESSAGE);
            dispose();
            return;
        }

        titleField.setText(ticket.getTitle());
        priorityCombo.setSelectedItem(ticket.getPriority());

        // Note: On ne peut pas facilement "decompiler" un Content en elements individuels
        // L'utilisateur devra recreer la description
        // Afficher un message explicatif
        JOptionPane.showMessageDialog(this,
            "Vous pouvez modifier le titre et la priorite.\n\n" +
            "Pour la description, vous devez la recreer en ajoutant\n" +
            "les elements (Texte, Image, Video).\n\n" +
            "Description actuelle:\n" + ticket.getDescription(),
            "Information",
            JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Sauvegarde les modifications
     */
    private void onSave() {
        String title = titleField.getText().trim();
        String priority = (String) priorityCombo.getSelectedItem();

        // Validation
        if (title.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Le titre ne peut pas etre vide",
                "Erreur de validation",
                JOptionPane.ERROR_MESSAGE);
            titleField.requestFocus();
            return;
        }

        if (title.length() > 100) {
            JOptionPane.showMessageDialog(this,
                "Le titre ne peut pas depasser 100 caracteres",
                "Erreur de validation",
                JOptionPane.ERROR_MESSAGE);
            titleField.requestFocus();
            return;
        }

        // Recuperer la nouvelle description si l'utilisateur en a cree une
        List<ContentItemDTO> newContentItems = null;
        if (!contentBuilder.isEmpty()) {
            newContentItems = contentBuilder.getContentItems();
        }

        // Appeler le controller pour sauvegarder les modifications
        try {
            // Utiliser updateTicket dans tous les cas (avec liste vide si pas de contenu)
            if (newContentItems == null || newContentItems.isEmpty()) {
                newContentItems = new ArrayList<>();
            }
            ticketController.updateTicket(ticketID, title, priority, newContentItems);

            JOptionPane.showMessageDialog(this,
                "Ticket modifie avec succes!",
                "Succes",
                JOptionPane.INFORMATION_MESSAGE);

            dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                "Erreur lors de la modification: " + ex.getMessage(),
                "Erreur",
                JOptionPane.ERROR_MESSAGE);
        }
    }
}

package gui.views.dialogs;

import gui.controllers.TicketController;
import gui.models.ContentItemDTO;
import gui.validators.TicketValidator;
import gui.utils.ErrorHandler;
import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * CreateTicketDialog
 * Dialogue modal pour creer un nouveau ticket
 * REFACTORISE: Utilise TicketValidator, ErrorHandler et ContentItemDTO
 */
public class CreateTicketDialog extends JDialog {
    private JTextField titleField;
    private JComboBox<String> priorityCombo;
    private ContentBuilderPanel contentBuilder;
    private JButton createButton;
    private JButton cancelButton;

    private TicketController controller;

    /**
     * Constructeur avec injection de dépendances du controller
     */
    public CreateTicketDialog(JFrame parent, TicketController controller) {
        super(parent, "Creer un nouveau ticket", true);
        this.controller = controller;
        initComponents();
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
        priorityCombo.setSelectedIndex(1); // Par defaut: Moyenne
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

        return panel;
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));

        createButton = new JButton("Creer");
        createButton.addActionListener(e -> onCreate());

        cancelButton = new JButton("Annuler");
        cancelButton.addActionListener(e -> dispose());

        panel.add(createButton);
        panel.add(cancelButton);

        // Rendre le bouton "Creer" par defaut (activable avec Enter)
        getRootPane().setDefaultButton(createButton);

        return panel;
    }

    /**
     * Gere la creation du ticket
     * REFACTORISE: Utilise TicketValidator, ErrorHandler et ContentItemDTO
     */
    private void onCreate() {
        String title = titleField.getText().trim();
        String priority = (String) priorityCombo.getSelectedItem();

        // Validation via TicketValidator
        TicketValidator.ValidationResult titleValidation = TicketValidator.validateTitle(title);
        if (!titleValidation.isValid()) {
            ErrorHandler.showUserError(this, titleValidation.getErrorMessage());
            titleField.requestFocus();
            return;
        }

        TicketValidator.ValidationResult priorityValidation = TicketValidator.validatePriority(priority);
        if (!priorityValidation.isValid()) {
            ErrorHandler.showUserError(this, priorityValidation.getErrorMessage());
            return;
        }

        // Récupérer les DTOs (pas les objets métier)
        List<ContentItemDTO> contentItems = contentBuilder.getContentItems();

        // Si pas de contenu, demander confirmation
        if (contentItems.isEmpty()) {
            boolean confirmed = ErrorHandler.confirm(this,
                "Confirmation",
                "Aucun contenu ajouté. Voulez-vous créer un ticket vide?");

            if (!confirmed) {
                return;
            }
        }

        // Creer le ticket via le controller (qui convertit les DTOs en objets métier)
        try {
            int newTicketID = controller.createTicketWithContentItems(title, contentItems, priority);

            ErrorHandler.showSuccess(this,
                "Ticket créé avec succès!\n\n" +
                "ID: " + newTicketID + "\n" +
                "Titre: " + title);
            dispose();

        } catch (Exception ex) {
            ErrorHandler.showTechnicalError(this,
                "Erreur lors de la création du ticket",
                ex);
        }
    }
}

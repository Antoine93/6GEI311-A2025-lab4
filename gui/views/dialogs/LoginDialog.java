package gui.views.dialogs;

import gui.controllers.TicketController;
import gui.models.UserDTO;
import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * LoginDialog
 * Dialogue de connexion permettant de saisir l'ID utilisateur
 * IDs disponibles: 1 (Developpeur), 2 (Testeur), 100 (Admin)
 * REFACTORISE (Lab 4): Saisie manuelle de l'ID pour éviter l'appel API avant authentification
 */
public class LoginDialog extends JDialog {
    private JTextField userIDField;
    private JButton loginButton;
    private JButton cancelButton;
    private TicketController ticketController;
    private int selectedUserID;
    private boolean loginSuccessful = false;

    /**
     * Constructeur avec injection de dépendances du controller
     */
    public LoginDialog(JFrame parent, TicketController controller) {
        super(parent, "Connexion - Système de Gestion de Tickets", true);
        this.ticketController = controller;
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setSize(400, 200);
        setLocationRelativeTo(getParent());

        // CENTER: Formulaire de sélection
        JPanel formPanel = createFormPanel();
        add(formPanel, BorderLayout.CENTER);

        // SOUTH: Boutons
        JPanel buttonPanel = createButtonPanel();
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private JPanel createFormPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Label d'instruction
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        JLabel titleLabel = new JLabel("Entrez votre ID utilisateur:");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        panel.add(titleLabel, gbc);

        // Label "ID Utilisateur"
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        panel.add(new JLabel("ID Utilisateur:"), gbc);

        // Champ de texte pour l'ID utilisateur
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        userIDField = new JTextField(10);
        userIDField.setText("1"); // Valeur par défaut
        panel.add(userIDField, gbc);

        // Note explicative
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        JLabel noteLabel = new JLabel(
            "<html><i>IDs disponibles: 1 (Developpeur), 2 (Testeur), 100 (Admin)</i></html>");
        noteLabel.setForeground(Color.GRAY);
        panel.add(noteLabel, gbc);

        return panel;
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));

        loginButton = new JButton("Se connecter");
        loginButton.addActionListener(e -> onLogin());

        cancelButton = new JButton("Annuler");
        cancelButton.addActionListener(e -> onCancel());

        panel.add(loginButton);
        panel.add(cancelButton);

        getRootPane().setDefaultButton(loginButton);

        return panel;
    }

    private void onLogin() {
        String userIDText = userIDField.getText().trim();

        if (userIDText.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Veuillez entrer un ID utilisateur.",
                "Erreur",
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            selectedUserID = Integer.parseInt(userIDText);
            loginSuccessful = true;
            dispose();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                "L'ID utilisateur doit être un nombre entier.",
                "Erreur",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onCancel() {
        loginSuccessful = false;
        selectedUserID = -1;
        dispose();
    }

    /**
     * Retourne l'ID de l'utilisateur sélectionné
     */
    public int getSelectedUserID() {
        return selectedUserID;
    }

    /**
     * Retourne true si la connexion a réussi
     */
    public boolean isLoginSuccessful() {
        return loginSuccessful;
    }
}

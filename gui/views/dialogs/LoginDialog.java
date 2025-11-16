package gui.views.dialogs;

import gui.controllers.TicketController;
import gui.models.UserDTO;
import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * LoginDialog
 * Dialogue de connexion permettant de choisir l'utilisateur actif
 * Supporte User et Admin
 * REFACTORISE: Constructeur avec injection de dépendances du controller
 */
public class LoginDialog extends JDialog {
    private JComboBox<String> userCombo;
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
        JLabel titleLabel = new JLabel("Sélectionnez votre profil utilisateur:");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        panel.add(titleLabel, gbc);

        // Label "Utilisateur"
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        panel.add(new JLabel("Utilisateur:"), gbc);

        // ComboBox avec la liste des utilisateurs
        gbc.gridx = 1;
        gbc.weightx = 1.0;

        List<UserDTO> users = ticketController.getAllUsers();
        String[] userNames = users.stream()
            .map(u -> u.getName() + " (" + u.getRole() + ")")
            .toArray(String[]::new);

        userCombo = new JComboBox<>(userNames);
        panel.add(userCombo, gbc);

        // Note explicative
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        JLabel noteLabel = new JLabel(
            "<html><i>Note: Les Admins ont accès à toutes les fonctionnalités</i></html>");
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
        int selectedIndex = userCombo.getSelectedIndex();
        if (selectedIndex != -1) {
            List<UserDTO> users = ticketController.getAllUsers();
            selectedUserID = users.get(selectedIndex).getUserID();
            loginSuccessful = true;
            dispose();
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

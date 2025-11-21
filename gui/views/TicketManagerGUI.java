package gui.views;

import gui.controllers.*;
import gui.models.*;
import gui.views.dialogs.*;
import gui.views.components.TicketDetailPanel;
import gui.services.*;
import gui.utils.ErrorHandler;
import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * TicketManagerGUI
 * Fenetre principale de l'application de gestion de tickets
 * Architecture MVC - Ceci est la VIEW principale
 * REFACTORISE: Implémente TicketStateListener (Observer pattern)
 * Utilise les services (FileService, PermissionService) et ErrorHandler
 */
public class TicketManagerGUI extends JFrame implements TicketStateListener {
    // Components
    private JTable ticketTable;
    private TicketTableModel tableModel;
    private JButton newTicketButton;
    private JButton editTicketButton;
    private JButton assignTicketButton;
    private JButton refreshButton;
    private JButton exportPdfButton;
    private JButton addCommentButton;
    private JButton changeStatusButton;
    private JButton switchUserButton;
    private JLabel statusLabel;
    private JLabel userLabel;

    // Controllers
    private TicketController ticketController;

    public TicketManagerGUI() {
        ticketController = new TicketController();

        // Note: ApplicationState n'est plus utilisé en Lab 4 (architecture REST)
        // Les changements d'état sont maintenant gérés via le serveur REST

        // Afficher le dialogue de login au démarrage
        showLoginDialog();

        initComponents();
        loadTickets();
    }

    /**
     * NOUVEAU: Implémentation de TicketStateListener
     * Rafraîchit automatiquement l'affichage quand les tickets changent
     */
    @Override
    public void onTicketsChanged() {
        SwingUtilities.invokeLater(() -> loadTickets());
    }

    /**
     * NOUVEAU: Implémentation de TicketStateListener
     * Rafraîchit l'affichage et les permissions quand l'utilisateur change
     */
    @Override
    public void onCurrentUserChanged() {
        SwingUtilities.invokeLater(() -> {
            updateUserLabel();
            loadTickets();
            updateButtonPermissions();
        });
    }

    /**
     * Affiche le dialogue de connexion
     * REFACTORISE: Passe le controller au dialogue (injection de dépendances)
     */
    private void showLoginDialog() {
        LoginDialog loginDialog = new LoginDialog(this, ticketController);
        loginDialog.setVisible(true);

        if (loginDialog.isLoginSuccessful()) {
            int selectedUserID = loginDialog.getSelectedUserID();
            ticketController.setCurrentUser(selectedUserID);
        } else {
            // L'utilisateur a annulé - quitter l'application
            System.exit(0);
        }
    }

    /**
     * NOUVEAU: Met à jour le label utilisateur
     */
    private void updateUserLabel() {
        UserDTO currentUser = ticketController.getCurrentUser();
        if (currentUser != null && userLabel != null) {
            userLabel.setText("Connecte: " + currentUser.getName() + " (" + currentUser.getRole() + ")");
        }
    }

    private void initComponents() {
        // Configuration de la fenetre
        setTitle("Systeme de Gestion de Tickets - MVP");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(900, 600);
        setLocationRelativeTo(null);

        // Layout principal
        setLayout(new BorderLayout(10, 10));

        // NORTH: Header avec info utilisateur
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);

        // CENTER: Table des tickets
        JScrollPane scrollPane = createTablePanel();
        add(scrollPane, BorderLayout.CENTER);

        // SOUTH: Status bar
        statusLabel = new JLabel("Pret");
        statusLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        add(statusLabel, BorderLayout.SOUTH);
    }

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Panel supérieur avec info utilisateur et bouton changer utilisateur
        JPanel topPanel = new JPanel(new BorderLayout());

        // Info utilisateur à gauche
        UserDTO currentUser = ticketController.getCurrentUser();
        userLabel = new JLabel("Connecte: " + currentUser.getName() + " (" + currentUser.getRole() + ")");
        userLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        topPanel.add(userLabel, BorderLayout.WEST);

        panel.add(topPanel, BorderLayout.NORTH);

        // Toolbar avec boutons d'action
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.CENTER));

        newTicketButton = new JButton("\u2795 Nouveau");
        newTicketButton.addActionListener(e -> onNewTicket());

        editTicketButton = new JButton("\u270F\uFE0F Modifier");
        editTicketButton.addActionListener(e -> onEditTicket());

        assignTicketButton = new JButton("\uD83D\uDC64 Assigner");
        assignTicketButton.addActionListener(e -> onAssignTicket());

        exportPdfButton = new JButton("\uD83D\uDCC4 Exporter");
        exportPdfButton.addActionListener(e -> onExportPDF());

        addCommentButton = new JButton("\uD83D\uDCAC Commenter");
        addCommentButton.addActionListener(e -> onAddComment());

        changeStatusButton = new JButton("\uD83D\uDD04 Statut");
        changeStatusButton.addActionListener(e -> onChangeStatus());

        refreshButton = new JButton("\uD83D\uDD04 Rafraichir");
        refreshButton.addActionListener(e -> loadTickets());

        switchUserButton = new JButton("\uD83D\uDC64 Changer utilisateur");
        switchUserButton.addActionListener(e -> onSwitchUser());

        toolbar.add(newTicketButton);
        toolbar.add(editTicketButton);
        toolbar.add(assignTicketButton);
        toolbar.add(exportPdfButton);
        toolbar.add(addCommentButton);
        toolbar.add(changeStatusButton);
        toolbar.add(refreshButton);
        toolbar.add(switchUserButton);

        panel.add(toolbar, BorderLayout.CENTER);

        return panel;
    }

    private JScrollPane createTablePanel() {
        // Creer le modele de table
        tableModel = new TicketTableModel(new ArrayList<>());
        ticketTable = new JTable(tableModel);

        // Configuration de la table
        ticketTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        ticketTable.setRowHeight(25);
        ticketTable.getTableHeader().setReorderingAllowed(false);

        // Ajuster les largeurs de colonnes
        ticketTable.getColumnModel().getColumn(0).setPreferredWidth(50);  // ID
        ticketTable.getColumnModel().getColumn(1).setPreferredWidth(300); // Titre
        ticketTable.getColumnModel().getColumn(2).setPreferredWidth(100); // Statut
        ticketTable.getColumnModel().getColumn(3).setPreferredWidth(100); // Priorite
        ticketTable.getColumnModel().getColumn(4).setPreferredWidth(120); // Source
        ticketTable.getColumnModel().getColumn(5).setPreferredWidth(120); // Assigne

        // Double-clic pour voir les details
        ticketTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    onViewDetails();
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(ticketTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Liste des tickets"));

        return scrollPane;
    }

    /**
     * Ouvre le dialogue de creation de ticket
     * REFACTORISE: Passe le controller (injection), plus besoin de loadTickets() (Observer)
     */
    private void onNewTicket() {
        CreateTicketDialog dialog = new CreateTicketDialog(this, ticketController);
        dialog.setVisible(true);
        // loadTickets() sera appelé automatiquement via onTicketsChanged()
    }

    /**
     * Affiche les details du ticket selectionne
     */
    private void onViewDetails() {
        int row = ticketTable.getSelectedRow();
        if (row != -1) {
            int ticketID = tableModel.getTicketIdAt(row);
            if (ticketID != -1) {
                showTicketDetails(ticketID);
            }
        }
    }

    /**
     * Affiche les details d'un ticket dans un dialogue
     * REFACTORISE: Utilise TicketDetailPanel pour la présentation
     */
    private void showTicketDetails(int ticketID) {
        // Récupérer les données via le controller
        TicketDTO ticket = ticketController.getTicketById(ticketID);
        List<String> comments = ticketController.getTicketComments(ticketID);

        if (ticket == null) {
            ErrorHandler.showUserError(this, "Ticket introuvable");
            return;
        }

        // Créer le panel de détails
        TicketDetailPanel detailPanel = new TicketDetailPanel();
        detailPanel.displayTicket(ticket, comments);

        // Afficher dans un dialogue
        JOptionPane.showMessageDialog(this,
            detailPanel,
            "Details du ticket #" + ticketID,
            JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Charge tous les tickets depuis le controller
     */
    private void loadTickets() {
        UserDTO currentUser = ticketController.getCurrentUser();
        List<TicketDTO> tickets = ticketController.getFilteredTickets();

        tableModel.refresh(tickets);
        statusLabel.setText(tickets.size() + " ticket(s) | Double-cliquez pour voir les details");

        // Mettre à jour le label utilisateur et les permissions
        userLabel.setText("Connecte: " + currentUser.getName() + " (" + currentUser.getRole() + ")");

        // Mettre à jour les permissions des boutons
        updateButtonPermissions();
    }

    /**
     * Met à jour les permissions des boutons selon le rôle de l'utilisateur
     * REFACTORISE: Utilise PermissionService pour vérifier les permissions
     */
    private void updateButtonPermissions() {
        UserDTO currentUser = ticketController.getCurrentUser();

        newTicketButton.setEnabled(
            PermissionService.hasPermission(currentUser, PermissionService.Permission.CREATE_TICKET));
        editTicketButton.setEnabled(
            PermissionService.hasPermission(currentUser, PermissionService.Permission.EDIT_TICKET));
        assignTicketButton.setEnabled(
            PermissionService.hasPermission(currentUser, PermissionService.Permission.ASSIGN_TICKET));
        changeStatusButton.setEnabled(
            PermissionService.hasPermission(currentUser, PermissionService.Permission.CHANGE_STATUS));
        addCommentButton.setEnabled(
            PermissionService.hasPermission(currentUser, PermissionService.Permission.ADD_COMMENT));
        exportPdfButton.setEnabled(
            PermissionService.hasPermission(currentUser, PermissionService.Permission.EXPORT_TICKET));

        // Tous peuvent rafraîchir et changer d'utilisateur
        refreshButton.setEnabled(true);
        switchUserButton.setEnabled(true);
    }

    /**
     * Exporte le ticket selectionne en PDF
     * REFACTORISE: Utilise getSelectedTicketID(), FileService et ErrorHandler
     */
    private void onExportPDF() {
        int ticketID = getSelectedTicketID();
        if (ticketID == -1) {
            ErrorHandler.showUserError(this, "Veuillez sélectionner un ticket à exporter");
            return;
        }

        try {
            // Le controller génère le contenu
            String pdfContent = ticketController.exportTicketToText(ticketID);

            // Le service gère la sauvegarde
            String fileName = FileService.generateTicketReportFileName(ticketID);
            java.nio.file.Path savedPath = FileService.saveTextFile(this, fileName, pdfContent);

            if (savedPath != null) {
                ErrorHandler.showSuccess(this,
                    "Rapport exporté avec succès:\n" + savedPath.toString() + "\n\n" +
                    "Note: Le fichier est au format texte.");
            }

        } catch (Exception ex) {
            ErrorHandler.showTechnicalError(this, "Erreur lors de l'export", ex);
        }
    }

    /**
     * NOUVEAU: Méthode utilitaire pour obtenir l'ID du ticket sélectionné
     */
    private int getSelectedTicketID() {
        int row = ticketTable.getSelectedRow();
        if (row == -1) {
            return -1;
        }
        return tableModel.getTicketIdAt(row);
    }

    /**
     * Ajoute un commentaire au ticket selectionne
     */
    private void onAddComment() {
        int row = ticketTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this,
                "Veuillez selectionner un ticket",
                "Aucun ticket selectionne",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        int ticketID = tableModel.getTicketIdAt(row);
        if (ticketID == -1) return;

        String comment = JOptionPane.showInputDialog(this,
            "Entrez votre commentaire:",
            "Ajouter un commentaire au ticket #" + ticketID,
            JOptionPane.QUESTION_MESSAGE);

        if (comment != null && !comment.trim().isEmpty()) {
            try {
                ticketController.addComment(ticketID, comment);
                JOptionPane.showMessageDialog(this,
                    "Commentaire ajoute avec succes!",
                    "Succes",
                    JOptionPane.INFORMATION_MESSAGE);
                loadTickets();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                    "Erreur: " + ex.getMessage(),
                    "Erreur",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Change le statut du ticket selectionne
     */
    private void onChangeStatus() {
        int row = ticketTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this,
                "Veuillez selectionner un ticket",
                "Aucun ticket selectionne",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        int ticketID = tableModel.getTicketIdAt(row);
        if (ticketID == -1) return;

        TicketDTO ticket = ticketController.getTicketById(ticketID);
        if (ticket == null) return;

        String currentStatus = ticket.getStatus();
        List<String> available = ticketController.getAvailableTransitions(ticketID);

        if (available.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Aucune transition possible depuis le statut " + currentStatus,
                "Information",
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String newStatus = (String) JOptionPane.showInputDialog(
            this,
            "Statut actuel: " + currentStatus + "\nChoisissez le nouveau statut:",
            "Changer le statut du ticket #" + ticketID,
            JOptionPane.QUESTION_MESSAGE,
            null,
            available.toArray(),
            available.get(0)
        );

        if (newStatus != null) {
            try {
                ticketController.changeTicketStatus(ticketID, newStatus);
                JOptionPane.showMessageDialog(this,
                    "Statut modifie: " + currentStatus + " -> " + newStatus,
                    "Succes",
                    JOptionPane.INFORMATION_MESSAGE);
                loadTickets();
            } catch (IllegalStateException ex) {
                JOptionPane.showMessageDialog(this,
                    "Erreur de transition: " + ex.getMessage(),
                    "Erreur",
                    JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                    "Erreur inattendue: " + ex.getMessage(),
                    "Erreur",
                    JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        }
    }

    /**
     * Modifie le ticket selectionne
     */
    private void onEditTicket() {
        int row = ticketTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this,
                "Veuillez selectionner un ticket a modifier",
                "Aucun ticket selectionne",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        int ticketID = tableModel.getTicketIdAt(row);
        if (ticketID == -1) return;

        gui.views.dialogs.EditTicketDialog dialog = new gui.views.dialogs.EditTicketDialog(this, ticketID);
        dialog.setVisible(true);

        // Rafraichir apres modification
        loadTickets();
    }

    /**
     * Assigne le ticket selectionne a un utilisateur
     */
    private void onAssignTicket() {
        int row = ticketTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this,
                "Veuillez selectionner un ticket a assigner",
                "Aucun ticket selectionne",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        int ticketID = tableModel.getTicketIdAt(row);
        if (ticketID == -1) return;

        // Recuperer la liste des utilisateurs
        List<UserDTO> users = ticketController.getAllUsers();
        if (users.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Aucun utilisateur disponible",
                "Erreur",
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Creer un array pour le dropdown
        String[] userNames = users.stream()
            .map(u -> u.getName() + " (ID: " + u.getUserID() + ")")
            .toArray(String[]::new);

        String selectedUser = (String) JOptionPane.showInputDialog(
            this,
            "Assigner le ticket #" + ticketID + " a:",
            "Assignation de ticket",
            JOptionPane.QUESTION_MESSAGE,
            null,
            userNames,
            userNames[0]
        );

        if (selectedUser != null) {
            // Trouver l'utilisateur selectionne
            int selectedIndex = java.util.Arrays.asList(userNames).indexOf(selectedUser);
            UserDTO user = users.get(selectedIndex);

            try {
                ticketController.assignTicket(ticketID, user.getUserID());
                JOptionPane.showMessageDialog(this,
                    "Ticket assigne a " + user.getName() + " avec succes!",
                    "Succes",
                    JOptionPane.INFORMATION_MESSAGE);
                loadTickets();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                    "Erreur lors de l'assignation: " + ex.getMessage(),
                    "Erreur",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Change l'utilisateur actuel
     * REFACTORISE: Passe le controller au dialogue
     */
    private void onSwitchUser() {
        LoginDialog loginDialog = new LoginDialog(this, ticketController);
        loginDialog.setVisible(true);

        if (loginDialog.isLoginSuccessful()) {
            int selectedUserID = loginDialog.getSelectedUserID();
            ticketController.setCurrentUser(selectedUserID);

            // Rafraîchir l'interface pour refléter le changement (sera fait automatiquement via Observer)
            // loadTickets();

            UserDTO newUser = ticketController.getCurrentUser();
            JOptionPane.showMessageDialog(this,
                "Utilisateur changé avec succès!\nConnecté en tant que: " + newUser.getName(),
                "Changement d'utilisateur",
                JOptionPane.INFORMATION_MESSAGE);
        }
    }
}

import gui.controllers.*;
import gui.models.*;
import gui.services.*;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

/**
 * MainGUI_REST
 * Version REST de l'interface graphique - communique avec l'API REST
 *
 * DIFFÉRENCES avec MainGUI :
 * - Utilise TicketControllerREST au lieu de TicketController
 * - Pas de dépendance à ApplicationState local
 * - Gestion des erreurs réseau
 * - Nécessite que le serveur REST soit démarré
 */
public class MainGUI_REST extends JFrame {
    // Controllers
    private TicketControllerREST ticketController;

    // Components
    private JTable ticketTable;
    private DefaultTableModel tableModel;
    private JLabel statusLabel;
    private JLabel userLabel;
    private JTextArea detailsArea;

    // Current user
    private UserDTO currentUser;

    public MainGUI_REST() {
        ticketController = new TicketControllerREST();

        // Afficher le dialogue de login au démarrage
        if (!showLoginDialog()) {
            System.exit(0); // L'utilisateur a annulé
        }

        initComponents();
        loadTickets();
    }

    /**
     * Affiche le dialogue de connexion
     */
    private boolean showLoginDialog() {
        // Pour simplifier, demander l'ID utilisateur
        String input = JOptionPane.showInputDialog(
                this,
                "Entrez votre ID utilisateur (1, 2, ou 100 pour admin):",
                "Connexion au serveur REST",
                JOptionPane.PLAIN_MESSAGE
        );

        if (input == null || input.trim().isEmpty()) {
            return false; // Annulé
        }

        try {
            int userID = Integer.parseInt(input.trim());
            currentUser = ticketController.login(userID);

            if (currentUser == null) {
                JOptionPane.showMessageDialog(
                        this,
                        "Échec de la connexion.\nVérifiez que le serveur REST est démarré (port 8080).",
                        "Erreur de connexion",
                        JOptionPane.ERROR_MESSAGE
                );
                return false;
            }

            JOptionPane.showMessageDialog(
                    this,
                    "Bienvenue " + currentUser.getName() + " (" + currentUser.getRole() + ")",
                    "Connexion réussie",
                    JOptionPane.INFORMATION_MESSAGE
            );

            return true;

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(
                    this,
                    "ID utilisateur invalide.",
                    "Erreur",
                    JOptionPane.ERROR_MESSAGE
            );
            return false;
        }
    }

    /**
     * Initialise les composants de l'interface
     */
    private void initComponents() {
        setTitle("Système de Gestion de Tickets - API REST");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Panel principal avec BorderLayout
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // ===== NORTH: Barre d'information =====
        JPanel infoPanel = new JPanel(new BorderLayout());
        userLabel = new JLabel("Connecté: " + currentUser.getName() + " (" + currentUser.getRole() + ")");
        userLabel.setFont(new Font("Arial", Font.BOLD, 14));
        statusLabel = new JLabel("Prêt");
        infoPanel.add(userLabel, BorderLayout.WEST);
        infoPanel.add(statusLabel, BorderLayout.EAST);
        mainPanel.add(infoPanel, BorderLayout.NORTH);

        // ===== CENTER: Split pane (table + details) =====
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setDividerLocation(300);

        // Table des tickets
        String[] columnNames = {"ID", "Titre", "Statut", "Priorité", "Créé par", "Assigné à"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Table non éditable
            }
        };
        ticketTable = new JTable(tableModel);
        ticketTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        ticketTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                showTicketDetails();
            }
        });
        JScrollPane tableScrollPane = new JScrollPane(ticketTable);
        splitPane.setTopComponent(tableScrollPane);

        // Zone de détails
        detailsArea = new JTextArea();
        detailsArea.setEditable(false);
        detailsArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane detailsScrollPane = new JScrollPane(detailsArea);
        detailsScrollPane.setBorder(BorderFactory.createTitledBorder("Détails du ticket"));
        splitPane.setBottomComponent(detailsScrollPane);

        mainPanel.add(splitPane, BorderLayout.CENTER);

        // ===== SOUTH: Boutons d'action =====
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JButton refreshButton = new JButton("Rafraîchir");
        refreshButton.addActionListener(e -> loadTickets());

        JButton createButton = new JButton("Créer un ticket");
        createButton.addActionListener(e -> createTicket());

        JButton addCommentButton = new JButton("Ajouter commentaire");
        addCommentButton.addActionListener(e -> addComment());

        JButton changeStatusButton = new JButton("Changer statut");
        changeStatusButton.addActionListener(e -> changeStatus());

        JButton assignButton = new JButton("Assigner");
        assignButton.addActionListener(e -> assignTicket());

        JButton exportButton = new JButton("Export PDF");
        exportButton.addActionListener(e -> exportToPDF());

        buttonPanel.add(refreshButton);
        buttonPanel.add(createButton);
        buttonPanel.add(addCommentButton);
        buttonPanel.add(changeStatusButton);
        buttonPanel.add(assignButton);
        buttonPanel.add(exportButton);

        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(mainPanel);
    }

    /**
     * Charge les tickets depuis l'API REST
     */
    private void loadTickets() {
        statusLabel.setText("Chargement...");
        tableModel.setRowCount(0); // Vider la table

        List<TicketDTO> tickets = ticketController.getAllTickets();

        for (TicketDTO ticket : tickets) {
            tableModel.addRow(new Object[]{
                    ticket.getTicketID(),
                    ticket.getTitle(),
                    ticket.getStatus(),
                    ticket.getPriority(),
                    ticket.getCreatedByName(),
                    ticket.getAssignedToName()
            });
        }

        statusLabel.setText(tickets.size() + " ticket(s) chargé(s)");
    }

    /**
     * Affiche les détails du ticket sélectionné
     */
    private void showTicketDetails() {
        int selectedRow = ticketTable.getSelectedRow();
        if (selectedRow == -1) {
            detailsArea.setText("");
            return;
        }

        int ticketID = (int) tableModel.getValueAt(selectedRow, 0);
        String details = ticketController.getTicketDetails(ticketID);
        detailsArea.setText(details);
    }

    /**
     * Crée un nouveau ticket
     */
    private void createTicket() {
        String title = JOptionPane.showInputDialog(this, "Titre du ticket:");
        if (title == null || title.trim().isEmpty()) return;

        String description = JOptionPane.showInputDialog(this, "Description:");
        if (description == null || description.trim().isEmpty()) return;

        String[] priorities = {"Critique", "Haute", "Moyenne", "Basse"};
        String priority = (String) JOptionPane.showInputDialog(
                this,
                "Priorité:",
                "Sélection priorité",
                JOptionPane.PLAIN_MESSAGE,
                null,
                priorities,
                "Moyenne"
        );

        if (priority == null) return;

        // Créer un ContentItemDTO simple (texte)
        ContentItemDTO contentItem = new ContentItemDTO(
                ContentItemDTO.ContentType.TEXT,
                description,
                null
        );

        List<ContentItemDTO> contentItems = List.of(contentItem);
        int ticketID = ticketController.createTicketWithContentItems(title, contentItems, priority);

        if (ticketID > 0) {
            JOptionPane.showMessageDialog(this, "Ticket #" + ticketID + " créé avec succès!");
            loadTickets();
        } else {
            JOptionPane.showMessageDialog(this, "Échec de la création du ticket.", "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Ajoute un commentaire au ticket sélectionné
     */
    private void addComment() {
        int selectedRow = ticketTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Veuillez sélectionner un ticket.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int ticketID = (int) tableModel.getValueAt(selectedRow, 0);
        String comment = JOptionPane.showInputDialog(this, "Commentaire:");

        if (comment != null && !comment.trim().isEmpty()) {
            ticketController.addComment(ticketID, comment);
            showTicketDetails(); // Rafraîchir
        }
    }

    /**
     * Change le statut du ticket sélectionné
     */
    private void changeStatus() {
        int selectedRow = ticketTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Veuillez sélectionner un ticket.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int ticketID = (int) tableModel.getValueAt(selectedRow, 0);
        List<String> transitions = ticketController.getAvailableTransitions(ticketID);

        if (transitions.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Aucune transition disponible (état final).", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String newStatus = (String) JOptionPane.showInputDialog(
                this,
                "Nouveau statut:",
                "Changer statut",
                JOptionPane.PLAIN_MESSAGE,
                null,
                transitions.toArray(),
                transitions.get(0)
        );

        if (newStatus != null) {
            ticketController.changeTicketStatus(ticketID, newStatus);
            loadTickets();
        }
    }

    /**
     * Assigne le ticket sélectionné à un utilisateur
     */
    private void assignTicket() {
        int selectedRow = ticketTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Veuillez sélectionner un ticket.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        List<UserDTO> users = ticketController.getAllUsers();
        String[] userNames = users.stream().map(u -> u.getName() + " (ID: " + u.getUserID() + ")").toArray(String[]::new);

        String selected = (String) JOptionPane.showInputDialog(
                this,
                "Assigner à:",
                "Assignation",
                JOptionPane.PLAIN_MESSAGE,
                null,
                userNames,
                userNames[0]
        );

        if (selected != null) {
            // Extraire l'ID de la chaîne
            int userID = users.get(java.util.Arrays.asList(userNames).indexOf(selected)).getUserID();
            int ticketID = (int) tableModel.getValueAt(selectedRow, 0);

            ticketController.assignTicket(ticketID, userID);
            loadTickets();
        }
    }

    /**
     * Exporte le ticket sélectionné en PDF
     */
    private void exportToPDF() {
        int selectedRow = ticketTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Veuillez sélectionner un ticket.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int ticketID = (int) tableModel.getValueAt(selectedRow, 0);
        String pdfContent = ticketController.exportTicketToText(ticketID);

        if (!pdfContent.isEmpty()) {
            // Afficher dans une nouvelle fenêtre
            JFrame pdfFrame = new JFrame("Export PDF - Ticket #" + ticketID);
            JTextArea pdfArea = new JTextArea(pdfContent);
            pdfArea.setEditable(false);
            pdfArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
            pdfFrame.add(new JScrollPane(pdfArea));
            pdfFrame.setSize(700, 500);
            pdfFrame.setLocationRelativeTo(this);
            pdfFrame.setVisible(true);
        }
    }

    public static void main(String[] args) {
        // Définir le Look and Feel du système
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Lancer l'interface graphique
        SwingUtilities.invokeLater(() -> {
            MainGUI_REST gui = new MainGUI_REST();
            gui.setVisible(true);
        });
    }
}

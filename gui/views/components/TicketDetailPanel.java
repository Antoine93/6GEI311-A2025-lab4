package gui.views.components;

import gui.models.TicketDTO;
import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * TicketDetailPanel
 * Composant réutilisable pour afficher les détails complets d'un ticket
 * Sépare la logique de formatage/présentation du Controller
 */
public class TicketDetailPanel extends JPanel {
    private JLabel idLabel;
    private JLabel titleLabel;
    private JLabel statusLabel;
    private JLabel priorityLabel;
    private JLabel createdByLabel;
    private JLabel assignedToLabel;
    private JLabel creationDateLabel;
    private JTextArea descriptionArea;
    private JTextArea commentsArea;

    public TicketDetailPanel() {
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Panel des métadonnées (NORTH)
        JPanel metadataPanel = createMetadataPanel();
        add(metadataPanel, BorderLayout.NORTH);

        // Panel de description (CENTER)
        JPanel descPanel = createDescriptionPanel();
        add(descPanel, BorderLayout.CENTER);

        // Panel de commentaires (SOUTH)
        JPanel commentsPanel = createCommentsPanel();
        add(commentsPanel, BorderLayout.SOUTH);
    }

    private JPanel createMetadataPanel() {
        JPanel panel = new JPanel(new GridLayout(0, 2, 10, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Informations"));

        idLabel = new JLabel();
        titleLabel = new JLabel();
        statusLabel = new JLabel();
        priorityLabel = new JLabel();
        createdByLabel = new JLabel();
        assignedToLabel = new JLabel();
        creationDateLabel = new JLabel();

        panel.add(new JLabel("ID:"));
        panel.add(idLabel);
        panel.add(new JLabel("Titre:"));
        panel.add(titleLabel);
        panel.add(new JLabel("Statut:"));
        panel.add(statusLabel);
        panel.add(new JLabel("Priorité:"));
        panel.add(priorityLabel);
        panel.add(new JLabel("Créé par:"));
        panel.add(createdByLabel);
        panel.add(new JLabel("Assigné à:"));
        panel.add(assignedToLabel);
        panel.add(new JLabel("Date de création:"));
        panel.add(creationDateLabel);

        return panel;
    }

    private JPanel createDescriptionPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Description"));

        descriptionArea = new JTextArea();
        descriptionArea.setEditable(false);
        descriptionArea.setFont(new Font("SansSerif", Font.PLAIN, 12));
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        descriptionArea.setBackground(getBackground());

        JScrollPane scrollPane = new JScrollPane(descriptionArea);
        scrollPane.setPreferredSize(new Dimension(500, 200));
        panel.add(scrollPane);

        return panel;
    }

    private JPanel createCommentsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Commentaires"));

        commentsArea = new JTextArea();
        commentsArea.setEditable(false);
        commentsArea.setFont(new Font("SansSerif", Font.PLAIN, 11));
        commentsArea.setLineWrap(true);
        commentsArea.setWrapStyleWord(true);
        commentsArea.setBackground(getBackground());

        JScrollPane scrollPane = new JScrollPane(commentsArea);
        scrollPane.setPreferredSize(new Dimension(500, 100));
        panel.add(scrollPane);

        return panel;
    }

    /**
     * Affiche les données d'un ticket dans le panel
     *
     * @param ticket Le ticket à afficher
     * @param comments La liste des commentaires du ticket
     */
    public void displayTicket(TicketDTO ticket, List<String> comments) {
        if (ticket == null) {
            clearDisplay();
            return;
        }

        idLabel.setText(String.valueOf(ticket.getTicketID()));
        titleLabel.setText(ticket.getTitle());
        statusLabel.setText(ticket.getStatus());
        priorityLabel.setText(ticket.getPriority());
        createdByLabel.setText(ticket.getCreatedByName());
        assignedToLabel.setText(ticket.getAssignedToName());
        creationDateLabel.setText(ticket.getCreationDate());
        descriptionArea.setText(ticket.getDescription());

        // Formater les commentaires
        if (comments == null || comments.isEmpty()) {
            commentsArea.setText("Aucun commentaire");
        } else {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < comments.size(); i++) {
                sb.append("[").append(i + 1).append("] ").append(comments.get(i)).append("\n");
            }
            commentsArea.setText(sb.toString());
        }

        // Remettre le curseur au début
        descriptionArea.setCaretPosition(0);
        commentsArea.setCaretPosition(0);
    }

    /**
     * Efface l'affichage
     */
    private void clearDisplay() {
        idLabel.setText("");
        titleLabel.setText("");
        statusLabel.setText("");
        priorityLabel.setText("");
        createdByLabel.setText("");
        assignedToLabel.setText("");
        creationDateLabel.setText("");
        descriptionArea.setText("");
        commentsArea.setText("");
    }
}

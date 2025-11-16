package gui.views.dialogs;

import gui.models.ContentItemDTO;
import gui.services.FileService;
import gui.utils.ErrorHandler;
import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * ContentBuilderPanel
 * Panel pour construire une liste de ContentItemDTO graphiquement
 * Permet d'ajouter du texte, des images et des videos
 * REFACTORISE: N'utilise plus les classes du domaine métier (Content, TextContent, etc.)
 */
public class ContentBuilderPanel extends JPanel {
    private List<ContentItemDTO> contentItems;
    private DefaultListModel<String> listModel;
    private JList<String> contentJList;
    private JButton addTextButton;
    private JButton addImageButton;
    private JButton addVideoButton;
    private JButton removeButton;
    private JButton moveUpButton;
    private JButton moveDownButton;

    public ContentBuilderPanel() {
        contentItems = new ArrayList<>();
        listModel = new DefaultListModel<>();
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout(5, 5));
        setBorder(BorderFactory.createTitledBorder("Construction du contenu"));

        // NORTH: Boutons d'ajout
        JPanel addButtonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        addTextButton = new JButton("\uD83D\uDCDD Ajouter Texte");
        addTextButton.addActionListener(e -> onAddText());

        addImageButton = new JButton("\uD83D\uDDBC\uFE0F Ajouter Image");
        addImageButton.addActionListener(e -> onAddImage());

        addVideoButton = new JButton("\uD83C\uDFA5 Ajouter Video");
        addVideoButton.addActionListener(e -> onAddVideo());

        addButtonsPanel.add(addTextButton);
        addButtonsPanel.add(addImageButton);
        addButtonsPanel.add(addVideoButton);

        add(addButtonsPanel, BorderLayout.NORTH);

        // CENTER: Liste des contenus
        contentJList = new JList<>(listModel);
        contentJList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(contentJList);
        scrollPane.setPreferredSize(new Dimension(400, 150));
        add(scrollPane, BorderLayout.CENTER);

        // SOUTH: Boutons de manipulation
        JPanel manipPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        removeButton = new JButton("Supprimer");
        removeButton.addActionListener(e -> onRemove());

        moveUpButton = new JButton("\u2191 Monter");
        moveUpButton.addActionListener(e -> onMoveUp());

        moveDownButton = new JButton("\u2193 Descendre");
        moveDownButton.addActionListener(e -> onMoveDown());

        manipPanel.add(removeButton);
        manipPanel.add(moveUpButton);
        manipPanel.add(moveDownButton);

        add(manipPanel, BorderLayout.SOUTH);
    }

    private void onAddText() {
        JTextArea textArea = new JTextArea(5, 40);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(textArea);

        int result = JOptionPane.showConfirmDialog(
            this,
            scrollPane,
            "Entrez le texte:",
            JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.PLAIN_MESSAGE
        );

        if (result == JOptionPane.OK_OPTION) {
            String text = textArea.getText();
            if (text != null && !text.trim().isEmpty()) {
                // Créer un DTO, pas un objet métier !
                ContentItemDTO dto = new ContentItemDTO(
                    ContentItemDTO.ContentType.TEXT,
                    text,
                    null
                );
                contentItems.add(dto);

                String preview = text.length() > 50
                    ? text.substring(0, 50) + "..."
                    : text;
                listModel.addElement("\uD83D\uDCDD Texte: " + preview);
            }
        }
    }

    private void onAddImage() {
        // Utiliser le service pour sélectionner le fichier
        String path = FileService.selectImageFile(null);

        if (path != null) {
            String caption = ErrorHandler.promptText(
                this,
                "Légende de l'image",
                "Entrez une légende pour l'image:");

            if (caption == null) caption = "";

            // Créer un DTO
            ContentItemDTO dto = new ContentItemDTO(
                ContentItemDTO.ContentType.IMAGE,
                path,
                caption
            );
            contentItems.add(dto);

            String fileName = new java.io.File(path).getName();
            String display = caption.isEmpty()
                ? fileName
                : fileName + " - " + caption;
            listModel.addElement("\uD83D\uDDBC\uFE0F Image: " + display);
        }
    }

    private void onAddVideo() {
        // Utiliser le service pour sélectionner le fichier
        String path = FileService.selectVideoFile(null);

        if (path != null) {
            String durationStr = ErrorHandler.promptText(
                this,
                "Durée de la vidéo",
                "Entrez la durée en secondes:");

            int duration = 0;
            try {
                if (durationStr != null && !durationStr.trim().isEmpty()) {
                    duration = Integer.parseInt(durationStr);
                }
            } catch (NumberFormatException e) {
                ErrorHandler.showWarning(this, "Durée invalide, défaut: 0s");
            }

            // Créer un DTO (stocker la durée en string dans metadata)
            ContentItemDTO dto = new ContentItemDTO(
                ContentItemDTO.ContentType.VIDEO,
                path,
                String.valueOf(duration)
            );
            contentItems.add(dto);

            String fileName = new java.io.File(path).getName();
            listModel.addElement("\uD83C\uDFA5 Video: " + fileName + " (" + duration + "s)");
        }
    }

    private void onRemove() {
        int index = contentJList.getSelectedIndex();
        if (index != -1) {
            contentItems.remove(index);
            listModel.remove(index);
        }
    }

    private void onMoveUp() {
        int index = contentJList.getSelectedIndex();
        if (index > 0) {
            // Swap dans la liste
            ContentItemDTO temp = contentItems.get(index);
            contentItems.set(index, contentItems.get(index - 1));
            contentItems.set(index - 1, temp);

            // Swap dans le modele d'affichage
            String tempStr = listModel.get(index);
            listModel.set(index, listModel.get(index - 1));
            listModel.set(index - 1, tempStr);

            contentJList.setSelectedIndex(index - 1);
        }
    }

    private void onMoveDown() {
        int index = contentJList.getSelectedIndex();
        if (index != -1 && index < contentItems.size() - 1) {
            // Swap dans la liste
            ContentItemDTO temp = contentItems.get(index);
            contentItems.set(index, contentItems.get(index + 1));
            contentItems.set(index + 1, temp);

            // Swap dans le modele d'affichage
            String tempStr = listModel.get(index);
            listModel.set(index, listModel.get(index + 1));
            listModel.set(index + 1, tempStr);

            contentJList.setSelectedIndex(index + 1);
        }
    }

    /**
     * Retourne la liste des ContentItemDTO
     * Le Controller sera responsable de convertir ces DTOs en objets métier
     */
    public List<ContentItemDTO> getContentItems() {
        return new ArrayList<>(contentItems);
    }

    public boolean isEmpty() {
        return contentItems.isEmpty();
    }

    public void clear() {
        contentItems.clear();
        listModel.clear();
    }
}

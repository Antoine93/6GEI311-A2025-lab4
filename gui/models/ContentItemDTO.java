package gui.models;

/**
 * ContentItemDTO
 * DTO pour représenter un élément de contenu sans dépendre du domaine métier
 * Permet de découpler la vue (ContentBuilderPanel) des classes métier (Content, TextContent, etc.)
 */
public class ContentItemDTO {

    /**
     * Types de contenu supportés
     */
    public enum ContentType {
        TEXT,
        IMAGE,
        VIDEO
    }

    private final ContentType type;
    private final String data;
    private final String metadata;  // Caption pour image, durée pour vidéo, null pour texte

    /**
     * Constructeur
     * @param type Type de contenu
     * @param data Données (texte, chemin fichier, etc.)
     * @param metadata Métadonnées optionnelles (caption, durée, etc.)
     */
    public ContentItemDTO(ContentType type, String data, String metadata) {
        this.type = type;
        this.data = data;
        this.metadata = metadata;
    }

    // Getters
    public ContentType getType() {
        return type;
    }

    public String getData() {
        return data;
    }

    public String getMetadata() {
        return metadata;
    }

    @Override
    public String toString() {
        return "ContentItemDTO{type=" + type + ", data='" + data + "', metadata='" + metadata + "'}";
    }
}

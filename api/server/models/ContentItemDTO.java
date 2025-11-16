package api.server.models;

/**
 * ContentItemDTO - Data Transfer Object pour les éléments de contenu
 * Représentation JSON d'un élément de contenu (pattern Composite)
 *
 * Basé sur le schéma OpenAPI ContentItemDTO
 */
public class ContentItemDTO {

    public enum ContentType {
        TEXT,
        IMAGE,
        VIDEO
    }

    private ContentType type;
    private String data;
    private String metadata;

    // Constructeur par défaut (requis pour la désérialisation JSON)
    public ContentItemDTO() {
    }

    public ContentItemDTO(ContentType type, String data, String metadata) {
        this.type = type;
        this.data = data;
        this.metadata = metadata;
    }

    // Getters et Setters
    public ContentType getType() {
        return type;
    }

    public void setType(ContentType type) {
        this.type = type;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }

    @Override
    public String toString() {
        return "ContentItemDTO{" +
                "type=" + type +
                ", data='" + data + '\'' +
                ", metadata='" + metadata + '\'' +
                '}';
    }
}

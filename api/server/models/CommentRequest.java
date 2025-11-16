package api.server.models;

/**
 * CommentRequest - Requête d'ajout de commentaire
 * Basé sur le schéma OpenAPI CommentRequest
 */
public class CommentRequest {
    private String text;

    // Constructeur par défaut
    public CommentRequest() {
    }

    public CommentRequest(String text) {
        this.text = text;
    }

    // Getters et Setters
    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}

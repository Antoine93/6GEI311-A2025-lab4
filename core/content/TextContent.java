package core.content;

import core.exporter.Exporter;

/**
 * TextContent - Represente un contenu textuel
 */
public class TextContent implements Content {

    private String text;

    public TextContent(String text) {
        this.text = text;
    }

    @Override
    public String display() {
        return "[TEXTE] " + text;
    }

    @Override
    public String accept(Exporter exporter) {
        return exporter.exportText(this);
    }

    // Getters et Setters
    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return "TextContent{text='" + text + "'}";
    }
}

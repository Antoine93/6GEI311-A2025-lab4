package core.exporter;

import core.content.*;

/**
 * Interface Exporter - Patron Strategy
 * Definit les strategies d'export pour differents formats (PDF, HTML, etc.)
 */
public interface Exporter {

    /**
     * Exporte un contenu complet
     * @param content Le contenu a exporter
     * @return Le contenu exporte sous forme de String
     */
    String export(Content content);

    /**
     * Exporte un contenu textuel
     * @param textContent Le texte a exporter
     * @return Le texte formate pour l'export
     */
    String exportText(TextContent textContent);

    /**
     * Exporte un contenu image
     * @param imageContent L'image a exporter
     * @return L'image formatee pour l'export
     */
    String exportImage(ImageContent imageContent);

    /**
     * Exporte un contenu video
     * @param videoContent La video a exporter
     * @return La video formatee pour l'export
     */
    String exportVideo(VideoContent videoContent);

    /**
     * Exporte un contenu composite
     * @param compositeContent Le composite a exporter
     * @return Le composite formate pour l'export
     */
    String exportComposite(CompositeContent compositeContent);
}

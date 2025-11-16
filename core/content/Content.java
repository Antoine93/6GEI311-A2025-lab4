package core.content;

import core.exporter.Exporter;

/**
 * Interface Content - Patron Composite
 * Represente un element de contenu dans la description d'un ticket.
 * Peut etre un contenu simple (texte, image, video) ou composite (plusieurs contenus).
 */
public interface Content {

    /**
     * Affiche le contenu sous forme textuelle pour consultation dans la plateforme
     * @return Representation textuelle du contenu
     */
    String display();

    /**
     * Accepte un visiteur (Exporter) - Patron Visitor
     * Permet au contenu d'etre exporte dans differents formats
     * @param exporter L'exporteur qui va traiter ce contenu
     * @return Le contenu exporte sous forme de String
     */
    String accept(Exporter exporter);
}

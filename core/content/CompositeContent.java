package core.content;

import core.exporter.Exporter;
import java.util.ArrayList;
import java.util.List;

/**
 * CompositeContent - Patron Composite
 * Permet de composer plusieurs contenus ensemble (texte + image + video)
 */
public class CompositeContent implements Content {

    private List<Content> children;

    public CompositeContent() {
        this.children = new ArrayList<>();
    }

    /**
     * Ajoute un contenu a la composition
     * @param content Le contenu a ajouter
     */
    public void add(Content content) {
        if (content != null) {
            children.add(content);
        }
    }

    /**
     * Retire un contenu de la composition
     * @param content Le contenu a retirer
     */
    public void remove(Content content) {
        children.remove(content);
    }

    /**
     * Retourne tous les contenus enfants
     * @return Liste des contenus
     */
    public List<Content> getChildren() {
        return new ArrayList<>(children); // Retourne une copie pour protection
    }

    @Override
    public String display() {
        StringBuilder sb = new StringBuilder();
        sb.append("[COMPOSITE - ").append(children.size()).append(" element(s)]\n");
        for (Content child : children) {
            sb.append("  ").append(child.display()).append("\n");
        }
        return sb.toString();
    }

    @Override
    public String accept(Exporter exporter) {
        return exporter.exportComposite(this);
    }

    /**
     * Verifie si le composite est vide
     * @return true si aucun contenu, false sinon
     */
    public boolean isEmpty() {
        return children.isEmpty();
    }

    /**
     * Retourne le nombre de contenus
     * @return Nombre d'elements dans la composition
     */
    public int size() {
        return children.size();
    }

    @Override
    public String toString() {
        return "CompositeContent{children=" + children.size() + " element(s)}";
    }
}

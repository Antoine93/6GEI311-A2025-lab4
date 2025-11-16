import gui.views.TicketManagerGUI;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * MainGUI
 * Point d'entree principal de l'application - Lance l'interface graphique Swing
 *
 * Pour lancer les tests console (demonstration des patterns), utilisez MainConsole.java
 */
public class MainGUI {
    /**
     * Point d'entree de l'application
     * @param args Arguments de ligne de commande (non utilises)
     */
    public static void main(String[] args) {
        // Definir le Look and Feel du systeme pour une meilleure integration
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // Si le Look and Feel du systeme n'est pas disponible, utiliser le defaut
            e.printStackTrace();
        }

        // Lancer l'interface graphique dans l'Event Dispatch Thread
        // (thread dedie aux evenements Swing pour eviter les problemes de concurrence)
        SwingUtilities.invokeLater(() -> {
            TicketManagerGUI gui = new TicketManagerGUI();
            gui.setVisible(true);
        });
    }
}

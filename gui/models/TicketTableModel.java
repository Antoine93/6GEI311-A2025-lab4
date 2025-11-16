package gui.models;

import javax.swing.table.AbstractTableModel;
import java.util.*;

/**
 * TicketTableModel
 * Adapter pour afficher une liste de tickets dans une JTable
 * Ce n'est PAS le "Model" du MVC, mais un "View Model" (adapter Swing)
 */
public class TicketTableModel extends AbstractTableModel {
    private List<TicketDTO> tickets;
    private String[] columnNames = {"ID", "Titre", "Statut", "Priorite", "Source", "Assigne"};

    public TicketTableModel(List<TicketDTO> tickets) {
        this.tickets = tickets;
    }

    @Override
    public int getRowCount() {
        return tickets.size();
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public String getColumnName(int col) {
        return columnNames[col];
    }

    @Override
    public Object getValueAt(int row, int col) {
        TicketDTO ticket = tickets.get(row);
        switch (col) {
            case 0: return ticket.getTicketID();
            case 1: return ticket.getTitle();
            case 2: return ticket.getStatus();
            case 3: return ticket.getPriority();
            case 4: return ticket.getCreatedByName();  // Source
            case 5: return ticket.getAssignedToName();  // Assigné
            default: return null;
        }
    }

    /**
     * Recupere l'ID du ticket a une ligne donnee
     * @param row L'index de la ligne
     * @return L'ID du ticket correspondant
     */
    public int getTicketIdAt(int row) {
        if (row >= 0 && row < tickets.size()) {
            return tickets.get(row).getTicketID();
        }
        return -1;
    }

    /**
     * Rafraichit la table avec une nouvelle liste de tickets
     * @param newTickets La nouvelle liste
     */
    public void refresh(List<TicketDTO> newTickets) {
        this.tickets = newTickets;
        fireTableDataChanged();
    }
}

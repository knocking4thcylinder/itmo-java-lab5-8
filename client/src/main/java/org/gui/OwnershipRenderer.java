package org.gui;

import java.awt.Color;
import java.awt.Component;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

final class OwnershipRenderer extends DefaultTableCellRenderer {
    @Override
    public Component getTableCellRendererComponent(
        JTable table,
        Object value,
        boolean selected,
        boolean focus,
        int row,
        int column
    ) {
        Component component = super.getTableCellRendererComponent(table, value, selected, focus, row, column);
        MovieTableModel model = (MovieTableModel) table.getModel();
        MovieRow movie = model.rowAt(table.convertRowIndexToModel(row));
        if (!selected) {
            component.setBackground(movie.editable() ? Color.WHITE : new Color(229, 231, 235));
            component.setForeground(movie.editable() ? Color.BLACK : new Color(75, 85, 99));
        }
        return component;
    }
}

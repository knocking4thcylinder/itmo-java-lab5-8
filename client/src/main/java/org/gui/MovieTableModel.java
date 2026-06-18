package org.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

final class MovieTableModel extends AbstractTableModel {
    static final String[] COLUMNS = {
        "key", "id", "name", "x", "y", "genre", "oscars", "rating",
        "director", "weight", "country", "passportId", "locationX", "locationY",
        "locationName", "owner", "created"
    };
    private String[] columnLabels = COLUMNS;
    private List<MovieRow> rows = new ArrayList<>();
    private Locale locale = Locale.forLanguageTag("en-NZ");

    void setRows(List<MovieRow> rows) {
        this.rows = new ArrayList<>(rows);
        fireTableDataChanged();
    }

    List<MovieRow> rows() {
        return rows;
    }

    void setColumnLabels(String[] columnLabels) {
        this.columnLabels = columnLabels.clone();
        fireTableStructureChanged();
    }

    void setLocale(Locale locale) {
        this.locale = locale;
    }

    MovieRow rowAt(int row) {
        return rows.get(row);
    }

    void select(JTable table, MovieRow row) {
        int index = rows.indexOf(row);
        if (index >= 0) {
            table.setRowSelectionInterval(index, index);
        }
    }

    @Override
    public int getRowCount() {
        return rows.size();
    }

    @Override
    public int getColumnCount() {
        return COLUMNS.length;
    }

    @Override
    public String getColumnName(int column) {
        return columnLabels[column];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        return rows.get(rowIndex).displayValue(COLUMNS[columnIndex], locale);
    }
}

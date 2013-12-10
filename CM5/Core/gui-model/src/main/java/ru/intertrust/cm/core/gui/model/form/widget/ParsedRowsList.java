package ru.intertrust.cm.core.gui.model.form.widget;

import ru.intertrust.cm.core.business.api.dto.Dto;

import java.util.ArrayList;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 03.12.13
 *         Time: 13:15
 */
public class ParsedRowsList implements Dto {
    ArrayList<TableBrowserRowItem> filteredRows;

    public ArrayList<TableBrowserRowItem> getFilteredRows() {
        return filteredRows;
    }

    public void setFilteredRows(ArrayList<TableBrowserRowItem> filteredRows) {
        this.filteredRows = filteredRows;
    }
}

package ru.intertrust.cm.core.gui.model.form.widget;

import ru.intertrust.cm.core.business.api.dto.Dto;

import java.util.ArrayList;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 03.12.13
 *         Time: 13:15
 */
public class ParsedRowsList implements Dto {
    ArrayList<TableBrowserItem> filteredRows;

    public ArrayList<TableBrowserItem> getFilteredRows() {
        return filteredRows;
    }

    public void setFilteredRows(ArrayList<TableBrowserItem> filteredRows) {
        this.filteredRows = filteredRows;
    }
}

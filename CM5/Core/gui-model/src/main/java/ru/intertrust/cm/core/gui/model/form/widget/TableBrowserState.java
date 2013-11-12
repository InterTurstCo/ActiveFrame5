package ru.intertrust.cm.core.gui.model.form.widget;

import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.model.gui.form.widget.TableBrowserConfig;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 06.11.13
 *         Time: 16:15
 */
public class TableBrowserState extends LinkEditingWidgetState {
    private String collectionName;
    private List<TableBrowserRowItem> selectedRows;
    private LinkedHashMap<String, String> columnNamesAndDoFieldsMap;
    private TableBrowserConfig tableBrowserConfig;

    public String getCollectionName() {
        return collectionName;
    }

    public void setCollectionName(String collectionName) {
        this.collectionName = collectionName;
    }

    public List<TableBrowserRowItem> getSelectedRows() {
        return selectedRows;
    }

    public void setSelectedRows(List<TableBrowserRowItem> selectedRows) {
        this.selectedRows = selectedRows;
    }

    public TableBrowserConfig getTableBrowserConfig() {
        return tableBrowserConfig;
    }

    public void setTableBrowserConfig(TableBrowserConfig tableBrowserConfig) {
        this.tableBrowserConfig = tableBrowserConfig;
    }

    public LinkedHashMap<String, String> getColumnNamesAndDoFieldsMap() {
        return columnNamesAndDoFieldsMap;
    }

    public void setColumnNamesAndDoFieldsMap(LinkedHashMap<String, String> columnNamesAndDoFieldsMap) {
        this.columnNamesAndDoFieldsMap = columnNamesAndDoFieldsMap;
    }

    @Override
    public ArrayList<Id> getIds() {
        ArrayList<Id> selectedIds = new ArrayList<Id>();
        for (TableBrowserRowItem model : selectedRows) {
            selectedIds.add(model.getId());
        }

        return selectedIds;
    }
}

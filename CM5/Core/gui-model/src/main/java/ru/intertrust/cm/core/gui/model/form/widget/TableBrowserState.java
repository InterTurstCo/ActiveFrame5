package ru.intertrust.cm.core.gui.model.form.widget;

import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.gui.form.widget.TableBrowserConfig;

import java.util.ArrayList;
import java.util.LinkedHashMap;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 06.11.13
 *         Time: 16:15
 */
public class TableBrowserState extends LinkEditingWidgetState {
    private String collectionName;
    private ArrayList<TableBrowserRowItem> selectedItems;
    private LinkedHashMap<String, String> domainFieldOnColumnNameMap;
    private TableBrowserConfig tableBrowserConfig;

    public String getCollectionName() {
        return collectionName;
    }

    public void setCollectionName(String collectionName) {
        this.collectionName = collectionName;
    }

    public ArrayList<TableBrowserRowItem> getSelectedItems() {
        return selectedItems;
    }

    public void setSelectedItems(ArrayList<TableBrowserRowItem> selectedItems) {
        this.selectedItems = selectedItems;
    }

    public TableBrowserConfig getTableBrowserConfig() {
        return tableBrowserConfig;
    }

    public void setTableBrowserConfig(TableBrowserConfig tableBrowserConfig) {
        this.tableBrowserConfig = tableBrowserConfig;
    }

    public LinkedHashMap<String, String> getDomainFieldOnColumnNameMap() {
        return domainFieldOnColumnNameMap;
    }

    public void setDomainFieldOnColumnNameMap(LinkedHashMap<String, String> domainFieldOnColumnNameMap) {
        this.domainFieldOnColumnNameMap = domainFieldOnColumnNameMap;
    }

    @Override
    public ArrayList<Id> getIds() {
        ArrayList<Id> selectedIds = new ArrayList<Id>();
        for (TableBrowserRowItem model : selectedItems) {
            selectedIds.add(model.getId());
        }

        return selectedIds;
    }
}

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
    private Id rootId;
    private ArrayList<TableBrowserItem> tableBrowserItems;
    private LinkedHashMap<String, String> domainFieldOnColumnNameMap;
    private TableBrowserConfig tableBrowserConfig;

    public Id getRootId() {
        return rootId;
    }

    public void setRootId(Id rootId) {
        this.rootId = rootId;
    }

    public ArrayList<TableBrowserItem> getTableBrowserItems() {
        return tableBrowserItems;
    }

    public void setTableBrowserItems(ArrayList<TableBrowserItem> tableBrowserItems) {
        this.tableBrowserItems = tableBrowserItems;
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
        for (TableBrowserItem tableBrowserItem : tableBrowserItems) {
            selectedIds.add(tableBrowserItem.getId());
        }

        return selectedIds;
    }
}

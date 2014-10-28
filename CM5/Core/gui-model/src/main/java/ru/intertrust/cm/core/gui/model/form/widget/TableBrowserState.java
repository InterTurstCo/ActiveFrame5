package ru.intertrust.cm.core.gui.model.form.widget;

import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.gui.form.widget.TableBrowserConfig;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Set;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 06.11.13
 *         Time: 16:15
 */
public class TableBrowserState extends TooltipWidgetState<TableBrowserConfig> {

    private LinkedHashMap<String, String> domainFieldOnColumnNameMap;
    private TableBrowserConfig tableBrowserConfig;
    private Set<Id> selectedIds;
    private ArrayList<Id> temporarySelectedIds = new ArrayList<Id>();

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

    public ArrayList<Id> getTemporarySelectedIds() {
        return temporarySelectedIds;
    }

    public void resetTemporaryState(){
        temporarySelectedIds.clear();
    }

    public void applyChanges(){
        if(isSingleChoice()) {
            selectedIds = new HashSet<>(temporarySelectedIds);
        }else  {
            selectedIds.addAll(temporarySelectedIds);
        }
        resetTemporaryState();
    }

    public void clearState(){
        selectedIds.clear();
        temporarySelectedIds.clear();
        getListValues().clear();

    }

    public void addToTemporaryState(Id id){
        temporarySelectedIds.add(id);
    }

    public void removeFromTemporaryState(Id id){
        temporarySelectedIds.remove(id);
    }

    public void setSelectedIds(Set<Id> selectedIds) {
        this.selectedIds = selectedIds;
    }

    @Override
    public ArrayList<Id> getIds() {
        return selectedIds == null ? new ArrayList<Id>(0) : new ArrayList<>(selectedIds);
    }

    public Set<Id> getSelectedIds() {
        return selectedIds;
    }

    @Override
    public TableBrowserConfig getWidgetConfig() {
        return tableBrowserConfig;
    }
}

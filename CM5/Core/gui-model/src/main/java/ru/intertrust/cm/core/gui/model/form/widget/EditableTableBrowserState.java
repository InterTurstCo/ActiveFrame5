package ru.intertrust.cm.core.gui.model.form.widget;

import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.gui.form.widget.EditableTableBrowserConfig;
import ru.intertrust.cm.core.gui.model.util.WidgetUtil;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Set;

/**
 * Created by Ravil on 26.09.2017.
 */
public class EditableTableBrowserState extends TooltipWidgetState<EditableTableBrowserConfig> {
    private LinkedHashMap<String, String> domainFieldOnColumnNameMap;
    private EditableTableBrowserConfig editableTableBrowserConfig;
    private DomainObject rootObject;
    private String text;
    private Set<Id> selectedIds;
    private ArrayList<Id> temporarySelectedIds = new ArrayList<Id>();
    private boolean isTemporaryState;

    public EditableTableBrowserState(){
        selectedIds = new HashSet<>();
    }

    @Override
    public EditableTableBrowserConfig getWidgetConfig() {
        return editableTableBrowserConfig;
    }

    @Override
    public ArrayList<Id> getIds() {
        return selectedIds == null ? new ArrayList<Id>(0) : new ArrayList<>(selectedIds);
    }

    @Override
    public Set<Id> getSelectedIds() {
        return selectedIds;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public EditableTableBrowserConfig getEditableTableBrowserConfig() {
        return editableTableBrowserConfig;
    }

    public void setEditableTableBrowserConfig(EditableTableBrowserConfig editableTableBrowserConfig) {
        this.editableTableBrowserConfig = editableTableBrowserConfig;
    }

    public DomainObject getRootObject() {
        return rootObject;
    }

    public void setRootObject(DomainObject rootObject) {
        this.rootObject = rootObject;
    }

    public void setSelectedIds(Set<Id> selectedIds) {
        this.selectedIds = selectedIds;
    }

    public ArrayList<Id> getTemporarySelectedIds() {
        return temporarySelectedIds;
    }

    public void setTemporaryState(boolean isTemporaryState) {
        this.isTemporaryState = isTemporaryState;
    }

    public void setTemporarySelectedIds(ArrayList<Id> temporarySelectedIds) {
        this.temporarySelectedIds = temporarySelectedIds;
    }

    public void addToTemporaryState(Id id){
        temporarySelectedIds.add(id);
    }

    public void removeFromTemporaryState(Id id){
        temporarySelectedIds.remove(id);
    }

    public void resetTemporaryState(){
        temporarySelectedIds.clear();
        setTemporaryState(false);
    }

    public void applyChanges(){
        if(isSingleChoice()) {
            selectedIds = new HashSet<>(temporarySelectedIds);
        }else  {
            selectedIds.addAll(temporarySelectedIds);
        }
        resetTemporaryState();
    }
    public boolean isTableView(){
        return WidgetUtil.drawAsTable(getWidgetConfig().getSelectionStyleConfig());
    }

    public void clearState(){
        selectedIds.clear();
        temporarySelectedIds.clear();
        getListValues().clear();

    }
}

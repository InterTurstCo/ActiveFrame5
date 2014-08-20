package ru.intertrust.cm.core.gui.model.form.widget;


import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.gui.form.widget.LinkedDomainObjectsTableConfig;
import ru.intertrust.cm.core.gui.model.form.FormState;

import java.util.*;

public class LinkedDomainObjectsTableState extends LinkEditingWidgetState {

    private LinkedDomainObjectsTableConfig linkedDomainObjectsTableConfig;
    private List<RowItem> rowItems;
    private String objectTypeName;
    private ArrayList<Id> selectedIds = new ArrayList<>();

    private LinkedHashMap<String, FormState> newFormStates = new LinkedHashMap<>();
    private LinkedHashMap<String, FormState> editedFormStates = new LinkedHashMap<>();

    @Override
    public ArrayList<Id> getIds() {
        return selectedIds;
    }

    public void setIds(ArrayList<Id> ids) {
        this.selectedIds = ids;
    }

    public void setLinkedDomainObjectTableConfig(LinkedDomainObjectsTableConfig linkedDomainObjectsTableConfig) {
        this.linkedDomainObjectsTableConfig = linkedDomainObjectsTableConfig;
    }

    public boolean isShouldDrawTooltipButton() {
        return linkedDomainObjectsTableConfig.getSelectionFiltersConfig() != null &&
                linkedDomainObjectsTableConfig.getSelectionFiltersConfig().getRowLimit() != 0
                && selectedIds.size() > linkedDomainObjectsTableConfig.getSelectionFiltersConfig().getRowLimit()
                && linkedDomainObjectsTableConfig.getCollectionRefConfig() != null;

    }

    public LinkedDomainObjectsTableConfig getLinkedDomainObjectsTableConfig() {
        return linkedDomainObjectsTableConfig;
    }

    public void setRowItems(List<RowItem> rowItems) {
        this.rowItems = rowItems;
    }

    public List<RowItem> getRowItems() {
        return rowItems;
    }

    public void setObjectTypeName(String objectTypeName) {
        this.objectTypeName = objectTypeName;
    }

    public String getObjectTypeName() {
        return objectTypeName;
    }

    public void setLinkedFormConfig() {
    }

    public String addNewFormState(FormState formState) {
        String key = Long.toHexString(new Random().nextLong());
        newFormStates.put(key, formState);
        return key;
    }

    public void rewriteNewFormState(String key, FormState formState) {
        newFormStates.put(key, formState);
    }

    public void removeNewObjectState(String key) {
        newFormStates.remove(key);
    }

    public void removeEditedObjectState(String key) {
        editedFormStates.remove(key);
    }

    public void putEditedFormState(String id, FormState formState) {
        editedFormStates.put(id, formState);
    }

    public FormState getFromNewStates(String id) {
        return newFormStates.get(id);
    }

    public FormState getFromEditedStates(String id) {
        return editedFormStates.get(id);
    }

    public LinkedHashMap<String, FormState> getNewFormStates() {
        filterLabelStates(newFormStates);
        return newFormStates;
    }

    public LinkedHashMap<String, FormState> getEditedNestedFormStates() {
        filterLabelStates(editedFormStates);
        return editedFormStates;
    }

    @Override
    public boolean mayContainNestedFormStates() {
        return true;
    }

    private void filterLabelStates(LinkedHashMap<String, FormState> newFormStates) {
        for (Map.Entry<String, FormState> stringFormStateEntry : newFormStates.entrySet()) {
            FormState formStateEntry = stringFormStateEntry.getValue();
            HashMap<String, WidgetState> filteredWidgetStateMap = new HashMap<>();
            Map<String, WidgetState> fullWidgetsState = formStateEntry.getFullWidgetsState();
            for (Map.Entry<String, WidgetState> widgetState : fullWidgetsState.entrySet()) {
                WidgetState value = widgetState.getValue();
                if (!(value instanceof LabelState)) {
                    filteredWidgetStateMap.put(widgetState.getKey(), value);
                }
            }
            formStateEntry.setWidgetStateMap(filteredWidgetStateMap);
        }
    }

    public void clearPreviousStates() {
        newFormStates.clear();
        editedFormStates.clear();
    }

}

package ru.intertrust.cm.core.gui.model.form.widget;


import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.gui.form.FormConfig;
import ru.intertrust.cm.core.config.gui.form.widget.LinkedDomainObjectsTableConfig;
import ru.intertrust.cm.core.gui.model.form.FormState;

import java.util.*;

public class LinkedDomainObjectsTableState extends LinkEditingWidgetState {

    private LinkedDomainObjectsTableConfig linkedDomainObjectsTableConfig;
    private List<RowItem> rowItems;
    private String objectTypeName;
    private FormConfig linkedFormConfig;
    private ArrayList<Id> selectedIds = new ArrayList<Id>();

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

    public FormConfig getLinkedFormConfig() {
        return linkedFormConfig;
    }

    public void setLinkedFormConfig(FormConfig linkedFormConfig) {
        this.linkedFormConfig = linkedFormConfig;
    }


    LinkedHashMap<String, FormState> newFormStates = new LinkedHashMap<String, FormState>(); // <random-String,FormState>, map of object states are edited in widget
    LinkedHashMap<String, FormState> editedFormStates = new LinkedHashMap<String, FormState>();
    ArrayList<Id> idsToDelete = new ArrayList<Id>();

    public String addObjectState(FormState formState) {
        String key = Long.toHexString(new Random().nextLong());
        newFormStates.put(key, formState);
        return key;
    }

    public void removeObjectState(String key) {
        newFormStates.remove(key);
    }

    public void replaceObjectState(String id, FormState formState) {
        newFormStates.put(id, formState);
    }

    public FormState getFormState(String id) {
        return newFormStates.get(id);
    }

    public void addIdForDeletion(Id id) {
        idsToDelete.add(id);
    }

    public LinkedHashMap<String, FormState> getNewFormStates() {
        for (Map.Entry<String, FormState> stringFormStateEntry : newFormStates.entrySet()) {
            FormState formStateEntry = stringFormStateEntry.getValue();
            HashMap<String, WidgetState> filtereWidgetStateMap = new HashMap<String, WidgetState>();
            Map<String, WidgetState> fullWidgetsState = formStateEntry.getFullWidgetsState();
            for (Map.Entry<String, WidgetState> widgetState : fullWidgetsState.entrySet()) {
                WidgetState value = widgetState.getValue();
                if (!(value instanceof LabelState)) {
                    filtereWidgetStateMap.put(widgetState.getKey(), value);
                }
            }
            formStateEntry.setWidgetStateMap(filtereWidgetStateMap);
        }
        return newFormStates;
    }

    public LinkedHashMap<String, FormState> getEditedFormStates() {
        return editedFormStates;
    }


}

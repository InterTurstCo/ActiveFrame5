package ru.intertrust.cm.core.gui.model.form.widget;


import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.gui.form.FormConfig;
import ru.intertrust.cm.core.config.gui.form.widget.LinkedDomainObjectsTableConfig;
import ru.intertrust.cm.core.gui.model.form.FormState;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Random;

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

    public ArrayList<Id> getIdsToDelete() {
        return idsToDelete;
    }

    public LinkedHashMap<String, FormState> getNewFormStates() {
        return newFormStates;
    }

    public LinkedHashMap<String, FormState> getEditedFormStates() {
        return editedFormStates;
    }


}

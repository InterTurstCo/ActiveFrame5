package ru.intertrust.cm.core.gui.model.form.widget;

import ru.intertrust.cm.core.business.api.dto.Id;

import java.util.ArrayList;
import java.util.LinkedHashMap;

/**
 * @author Denis Mitavskiy
 *         Date: 15.10.13
 *         Time: 19:48
 */
public class ListBoxState extends LinkEditingWidgetState {
    private ArrayList<Id> selectedIds;
    private LinkedHashMap<Id, String> listValues;

    public ArrayList<Id> getSelectedIds() {
        return selectedIds;
    }

    public void setSelectedIds(ArrayList<Id> selectedIds) {
        this.selectedIds = selectedIds;
    }

    public LinkedHashMap<Id, String> getListValues() {
        return listValues;
    }

    public void setListValues(LinkedHashMap<Id, String> listValues) {
        this.listValues = listValues;
    }

    @Override
    public ArrayList<Id> getIds() {
        return selectedIds;
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }
}

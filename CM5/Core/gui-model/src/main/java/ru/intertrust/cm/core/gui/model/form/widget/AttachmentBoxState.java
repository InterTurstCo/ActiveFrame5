package ru.intertrust.cm.core.gui.model.form.widget;

import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.ReferenceValue;
import ru.intertrust.cm.core.business.api.dto.Value;

import java.util.ArrayList;
import java.util.LinkedHashMap;

/**
 * Created with IntelliJ IDEA.
 * User: User
 * Date: 22.10.13
 * Time: 18:48
 * To change this template use File | Settings | File Templates.
 */
public class AttachmentBoxState extends WidgetState {
    private ArrayList<Id> selectedIds;
    private LinkedHashMap<String, Id> listValues;

    @Override
    public Value toValue() {
        return null;
    }

    public ArrayList<Id> getSelectedIds() {
        return selectedIds;
    }

    public void setSelectedIds(ArrayList<Id> selectedIds) {
        this.selectedIds = selectedIds;
    }

    public LinkedHashMap<String, Id> getListValues() {
        return listValues;
    }

    public void setListValues(LinkedHashMap<String, Id> listValues) {
        this.listValues = listValues;
    }

    @Override
    public ArrayList<Value> toValues() {
        if (selectedIds == null) {
            return null;
        }
        ArrayList<Value> result = new ArrayList<Value>(selectedIds.size());
        for (Id id : selectedIds) {
            result.add(new ReferenceValue(id));
        }
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }
}

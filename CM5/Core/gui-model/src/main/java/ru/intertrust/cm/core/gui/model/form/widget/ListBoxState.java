package ru.intertrust.cm.core.gui.model.form.widget;

import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.ReferenceValue;
import ru.intertrust.cm.core.business.api.dto.Value;

import java.util.ArrayList;
import java.util.LinkedHashMap;

/**
 * @author Denis Mitavskiy
 *         Date: 15.10.13
 *         Time: 19:48
 */
public class ListBoxState extends WidgetState {
    private ArrayList<Id> selectedIds;
    private LinkedHashMap<Id, String> listValues;

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

    public LinkedHashMap<Id, String> getListValues() {
        return listValues;
    }

    public void setListValues(LinkedHashMap<Id, String> listValues) {
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
}

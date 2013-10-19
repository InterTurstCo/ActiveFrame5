package ru.intertrust.cm.core.gui.impl.client.form.widget;

import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Widget;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.form.widget.ListBoxState;
import ru.intertrust.cm.core.gui.model.form.widget.WidgetState;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;

/**
 * @author Denis Mitavskiy
 *         Date: 15.09.13
 *         Time: 22:40
 */
@ComponentName("list-box")
public class ListBoxWidget extends BaseWidget {
    private HashMap<String, Id> idMap;

    @Override
    public Component createNew() {
        return new ListBoxWidget();
    }

    public void setCurrentState(WidgetState currentState) {
        ListBoxState state = (ListBoxState) currentState;
        ArrayList<Id> selectedIds = state.getSelectedIds();
        HashSet<Id> selectedIdsSet = new HashSet<Id>(selectedIds);
        LinkedHashMap<Id,String> listValues = state.getListValues();

        idMap = new HashMap<String, Id>(listValues.size());
        ListBox listBox = (ListBox) impl;
        listBox.clear();
        int index = 0;
        for (Id id : listValues.keySet()) {
            String idString = id == null ? "" : id.toStringRepresentation();
            listBox.addItem(listValues.get(id), idString);
            idMap.put(idString, id);
            if (selectedIdsSet.contains(id)) {
                listBox.setItemSelected(index, true);
            }
            ++index;
        }
    }

    @Override
    public WidgetState getCurrentState() {
        ListBoxState state = new ListBoxState();
        ListBox listBox = (ListBox) impl;
        int listSize = listBox.getItemCount();
        if (listSize == 0) {
            return state;
        }

        ArrayList<Id> selectedIds = new ArrayList<Id>();
        for (int i = 0; i < listSize; ++i) {
            if (listBox.isItemSelected(i)) {
                selectedIds.add(idMap.get(listBox.getValue(i)));
            }
        }
        state.setSelectedIds(selectedIds);
        return state;
    }

    @Override
    protected Widget asEditableWidget() {
        return new ListBox(true);
    }

    @Override
    protected Widget asNonEditableWidget() {
        return new Label();
    }
}

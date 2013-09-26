package ru.intertrust.cm.core.gui.impl.client.form.widget;

import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Widget;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.form.widget.ComboBoxData;
import ru.intertrust.cm.core.gui.model.form.widget.WidgetData;

import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * @author Denis Mitavskiy
 *         Date: 15.09.13
 *         Time: 22:40
 */
@ComponentName("combo-box")
public class ComboBoxWidget extends BaseWidget {
    private HashMap<String, Id> idMap;

    @Override
    public Component createNew() {
        return new ComboBoxWidget();
    }

    public void setCurrentState(WidgetData state) {
        ComboBoxData comboBoxState = (ComboBoxData) state;
        Id selectedId = comboBoxState.getId();
        LinkedHashMap<Id,String> listValues = comboBoxState.getListValues();
        idMap = new HashMap<String, Id>(listValues.size());
        ListBox listBox = (ListBox) impl;
        int index = 0;
        for (Id id : listValues.keySet()) {
            String idString = id.toStringRepresentation();
            listBox.addItem(listValues.get(id), idString);
            idMap.put(idString, id);
            if (id.equals(selectedId)) {
                listBox.setSelectedIndex(index);
            }
            ++index;
        }
    }

    @Override
    public WidgetData getCurrentState() {
        ComboBoxData state = new ComboBoxData();
        ListBox listBox = (ListBox) impl;
        if (listBox.getItemCount() == 0) {
            return state;
        }
        state.setId(idMap.get(listBox.getValue(listBox.getSelectedIndex())));
        return state;
    }

    @Override
    protected Widget asEditableWidget() {
        return new ListBox(false);
    }

    @Override
    protected Widget asNonEditableWidget() {
        return new Label();
    }
}

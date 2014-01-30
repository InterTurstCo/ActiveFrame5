package ru.intertrust.cm.core.gui.impl.client.form.widget;

import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Widget;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.form.widget.ComboBoxState;
import ru.intertrust.cm.core.gui.model.form.widget.WidgetState;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Denis Mitavskiy
 *         Date: 15.09.13
 *         Time: 22:40
 */
@ComponentName("combo-box")
public class ComboBoxWidget extends BaseWidget {
    private HashMap<String, Id> idMap;
    private Id nonEditableId;

    @Override
    public Component createNew() {
        return new ComboBoxWidget();
    }

    @Override
    public void setCurrentState(WidgetState currentState) {
        ComboBoxState comboBoxState = (ComboBoxState) currentState;
        Id selectedId = comboBoxState.getSelectedId();
        Map<Id,String> listValues = comboBoxState.getListValues();
        if (!isEditable()) {
            if (selectedId != null) {
                nonEditableId = selectedId;
                ((Label) impl).setText(listValues.get(selectedId));
            }
            return;
        }
        idMap = new HashMap<String, Id>(listValues.size());
        ListBox listBox = (ListBox) impl;
        listBox.clear();
        int index = 0;
        for (Id id : listValues.keySet()) {
            String idString = id == null ? "" : id.toStringRepresentation();
            listBox.addItem(listValues.get(id), idString);
            idMap.put(idString, id);
            if (id == null && selectedId == null || id != null && id.equals(selectedId)) {
                listBox.setSelectedIndex(index);
            }
            ++index;
        }
    }

    @Override
    public WidgetState getCurrentState() {
        ComboBoxState state = new ComboBoxState();
        if (!isEditable()) {
            state.setSelectedId(nonEditableId);
            return state;
        }
        ListBox listBox = (ListBox) impl;
        if (listBox.getItemCount() == 0) {
            return state;
        }
        state.setSelectedId(idMap.get(listBox.getValue(listBox.getSelectedIndex())));
        return state;
    }

    @Override
    protected Widget asEditableWidget() {
        return new ListBox(false);
    }

    @Override
    protected Widget asNonEditableWidget() {
        Label noneEditableWidget = new Label();
        noneEditableWidget.removeStyleName("gwt-Label");
        return noneEditableWidget;
    }
}

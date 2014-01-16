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
        idMap = getStateHandler().setState(impl, (ListBoxState) currentState);
    }

    @Override
    public WidgetState getCurrentState() {
        final WidgetState result = getStateHandler().getState(impl, (ListBoxState) getInitialData(), idMap);
        return result;
    }

    @Override
    protected Widget asEditableWidget() {
        return new ListBox(true);
    }

    @Override
    protected Widget asNonEditableWidget() {
        return new Label();
    }

    private StateHandler getStateHandler() {
        return isEditable ? new ListBoxStateHandler() : new LabelStateHandler();
    }

    private interface StateHandler<T extends Widget> {
        WidgetState getState(T widget, ListBoxState initialState, HashMap<String, Id> idMap);
        HashMap<String, Id> setState(T widget, ListBoxState state);
    }

    private static class LabelStateHandler implements StateHandler<Label> {
        @Override
        public WidgetState getState(Label widget, ListBoxState initialState, HashMap<String, Id> idMap) {
            return initialState;
        }

        @Override
        public HashMap<String, Id> setState(Label widget, ListBoxState state) {
            final HashSet<Id> selectedIdsSet = state.getSelectedIdsSet();
            final StringBuilder builder = new StringBuilder();
            for (Id id : selectedIdsSet) {
                final String itemName = id == null ? "" : id.toStringRepresentation();
                builder.append(itemName).append(", ");
            }
            if (builder.length() > 0) {
                builder.setLength(builder.length() - 2);
            }
            widget.setText(builder.toString());
            return null;
        }
    }

    private static class ListBoxStateHandler implements StateHandler<ListBox> {
        @Override
        public WidgetState getState(
                final ListBox listBox, final ListBoxState initialState, final HashMap<String, Id> idMap) {
            final ListBoxState result = new ListBoxState();
            if (listBox.getItemCount() == 0) {
                return result;
            }
            final ArrayList<ArrayList<Id>> selectedIds =
                    new ArrayList<ArrayList<Id>>(initialState.getFieldPaths().length);
            for (int index = 0; index < initialState.getFieldPaths().length; index++) {
                selectedIds.add(new ArrayList<Id>());
            }
            for (int index = 0; index < listBox.getItemCount(); index++) {
                if (listBox.isItemSelected(index)) {
                    final Id id = idMap.get(listBox.getValue(index));
                    selectedIds.get(initialState.getFieldPathIndex(id)).add(id);
                }
            }
            result.setSelectedIds(selectedIds);
            return result;
        }

        @Override
        public HashMap<String, Id> setState(final ListBox listBox, final ListBoxState state) {
            boolean singleChoice = state.isSingleChoice();
            listBox.setMultipleSelect(singleChoice);
            final HashSet<Id> selectedIdsSet = state.getSelectedIdsSet();
            final LinkedHashMap<Id,String> listValues = state.getListValues();

            final HashMap<String, Id> idMap = new HashMap<String, Id>(listValues.size());
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
            return idMap;
        }
    }
}

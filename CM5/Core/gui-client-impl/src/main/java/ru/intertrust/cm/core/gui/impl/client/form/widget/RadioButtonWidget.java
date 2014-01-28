package ru.intertrust.cm.core.gui.impl.client.form.widget;

import com.google.gwt.user.client.ui.*;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.form.widget.RadioButtonState;
import ru.intertrust.cm.core.gui.model.form.widget.WidgetState;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author Lesia Puhova
 *         Date: 15.01.14
 *         Time: 10:39
 */
@ComponentName("radio-button")
public class RadioButtonWidget extends BaseWidget {
    private Map<String, Id> idMap;
    private Id nonEditableId;
    private RadioButtonState.Layout layout;
    private Panel verticalPanel = new VerticalPanel();
    private Panel horizontalPanel = new HorizontalPanel();

    @Override
    public Component createNew() {
        return new RadioButtonWidget();
    }

    @Override
    public void setCurrentState(WidgetState currentState) {
        RadioButtonState radioButtonState = (RadioButtonState) currentState;
        Id selectedId = radioButtonState.getSelectedId();
        Map<Id,String> listValues = radioButtonState.getListValues();
        layout = radioButtonState.getLayout();
        if (!isEditable()) {
            if (selectedId != null) {
                nonEditableId = selectedId;
                ((Label) impl).setText(listValues.get(selectedId));
            }
            return;
        }
        idMap = new HashMap<String, Id>(listValues.size());
        Panel panel = (Panel) impl;
        panel.clear();
        Panel layoutPanel;
        if (layout == RadioButtonState.Layout.HORIZONTAL) {
            layoutPanel = horizontalPanel;
        } else {
            layoutPanel = verticalPanel;
        }
        layoutPanel.clear();
        panel.add(layoutPanel);
        for (Id id : listValues.keySet()) {
            String text = listValues.get(id);
            String groupName = getDisplayConfig().getId();
            RadioButton rb = new RadioButton(groupName, text);
            layoutPanel.add(rb);
            idMap.put(text, id);
            if (id.equals(selectedId)) {
                rb.setValue(true);
            }
        }
    }

    @Override
    public WidgetState getCurrentState() {
        RadioButtonState state = new RadioButtonState();
        state.setLayout(layout);
        if (!isEditable()) {
            state.setSelectedId(nonEditableId);
            return state;
        }
        Panel panel = (Panel) impl;
        List<RadioButton> radioButtons = getRadioButtons(panel);

        if (radioButtons.isEmpty()) {
            return state;
        }
        for (RadioButton radioButton : radioButtons) {
            if (radioButton.getValue()) { // is selected
                state.setSelectedId(idMap.get(radioButton.getText()));
            }
        }
        return state;
    }

    private List<RadioButton> getRadioButtons(Panel panel) {
        List<RadioButton> radioButtons = new LinkedList<RadioButton>();
        Panel layoutPanel = (Panel)panel.iterator().next();
        for (Widget widget : layoutPanel) {
            if (widget instanceof RadioButton) {
                radioButtons.add((RadioButton)widget);
            }
        }
        return radioButtons;
    }

    @Override
    protected Widget asEditableWidget() {
        return new ScrollPanel();
    }

    @Override
    protected Widget asNonEditableWidget() {
        return new Label();
    }
}

package ru.intertrust.cm.core.gui.impl.client.form.widget;

import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Widget;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.form.widget.EnumBoxState;
import ru.intertrust.cm.core.gui.model.form.widget.WidgetState;

import java.util.Map;

/**
 * @author Lesia Puhova
 *         Date: 03.10.14
 *         Time: 19:35
 */
@ComponentName("enumeration-box")
public class EnumBoxWidget extends BaseWidget  {

    private Map<String, Value> displayTextToValue;

    @Override
    public void setCurrentState(WidgetState currentState) {
        EnumBoxState enumBoxState = (EnumBoxState)currentState;
        displayTextToValue = enumBoxState.getDisplayTextToValue();
        if (!isEditable()) {
            ((Label)impl).setText(enumBoxState.getSelectedText());
        } else {
            ListBox listBox = (ListBox) impl;
            listBox.clear();

            for (String text : displayTextToValue.keySet()) {
                listBox.addItem(text);
            }
            for (int i = 0; i < listBox.getItemCount(); i++) {
                if (listBox.getValue(i).equals(((EnumBoxState) currentState).getSelectedText())) {
                    listBox.setSelectedIndex(i);
                }
            }
        }

    }

    @Override
    protected boolean isChanged() {
        String currentText;
        if (!isEditable()) {
            currentText = ((Label)impl).getText();
        } else {
            ListBox listBox = (ListBox) impl;
            currentText = listBox.getValue(listBox.getSelectedIndex());
        }
        return !((EnumBoxState)getInitialData()).getSelectedText().equals(currentText);
    }

    @Override
    protected WidgetState createNewState() {
        final EnumBoxState state = new EnumBoxState();
        state.setDisplayTextToValue(displayTextToValue);
        if (!isEditable()) {
            state.setSelectedText(((Label)impl).getText());
            return state;
        }
        ListBox listBox = (ListBox) impl;
        state.setSelectedText(listBox.getValue(listBox.getSelectedIndex()));
        return state;
    }

    @Override
    protected Widget asEditableWidget(WidgetState state) {
        return new ListBox(false);
    }

    @Override
    protected Widget asNonEditableWidget(WidgetState state) {
        return  new Label();
    }

    @Override
    public Component createNew() {
        return new EnumBoxWidget();
    }


}

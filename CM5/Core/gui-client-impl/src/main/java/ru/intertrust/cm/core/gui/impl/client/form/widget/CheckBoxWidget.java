package ru.intertrust.cm.core.gui.impl.client.form.widget;

import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Widget;
import ru.intertrust.cm.core.business.api.dto.BooleanValue;
import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.form.widget.CheckBoxState;
import ru.intertrust.cm.core.gui.model.form.widget.WidgetState;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 06.10.13
 *         Time: 22:40
 */
@ComponentName("check-box")
public class CheckBoxWidget extends BaseWidget {

    @Override
    public Component createNew() {
        return new CheckBoxWidget();
    }

    public void setCurrentState(WidgetState currentState) {
        CheckBoxState checkBoxState = (CheckBoxState) currentState;
        BooleanValue isSelected = (BooleanValue)checkBoxState.getValue();
        ((CheckBox)impl).setValue(isSelected.get());

    }

    @Override
    public WidgetState getCurrentState() {
        CheckBoxState state = new CheckBoxState();
        Boolean isSelected = ((CheckBox)impl).getValue();
        state.setSelected(isSelected);
        return state;
    }

    @Override
    protected Widget asEditableWidget() {
        return new CheckBox();
    }

    @Override
    protected Widget asNonEditableWidget() {
        CheckBox checkBox = new CheckBox();
        checkBox.setEnabled(false);
        return checkBox;
    }
}


package ru.intertrust.cm.core.gui.impl.client.form.widget;

import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Widget;
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

    @Override
    public void setValue(Object value) {
        Boolean valueToSet = null;
        if (value instanceof Boolean) {
            valueToSet = (Boolean) value;
        } else if (value instanceof String) {
            valueToSet = Boolean.valueOf((String) value);
        }
        ((CheckBox)impl).setValue(valueToSet);
    }

    @Override
    public void disable(Boolean isDisabled) {
        ((CheckBox)impl).setEnabled(!isDisabled);
    }

    @Override
    public void reset() {
        ((CheckBox)impl).setValue(false);
    }

    @Override
    public void applyFilter(String value) {
        //TODO: Implementation required
    }

    @Override
    public Object getValueTextRepresentation() {
        return getValue();
    }

    public void setCurrentState(WidgetState currentState) {
        CheckBoxState checkBoxState = (CheckBoxState) currentState;
        ((CheckBox)impl).setValue(checkBoxState.isSelected());

    }

    @Override
    public Object getValue() {
        return ((CheckBoxState)getCurrentState()).isSelected();
    }

    @Override
    protected boolean isChanged() {
        final Boolean currentValue = ((CheckBox) impl).getValue();
        final CheckBoxState state = getInitialData();
        if (currentValue == null) {
            // старое значение есть, но нет нового - изменения есть
            return (state.isSelected() != null);
        } else if (state.isSelected() != null) {
            // есть оба значения, изменения есть, если они (значения) не совпадают
            return !currentValue.equals(state.isSelected());
        } else {
            // есть новое значение, но нет старого (т.е. в БД записано null) - считаем измененным только при установленном флаге,
            // в противном случае это означает, что его не меняли на форме
            return currentValue;
        }
    }

    @Override
    protected WidgetState createNewState() {
        CheckBoxState state = new CheckBoxState();
        Boolean isSelected = ((CheckBox)impl).getValue();
        state.setSelected(isSelected);
        return state;
    }

    @Override
    protected Widget asEditableWidget(WidgetState state) {
        CheckBox cBox = new CheckBox();
        cBox.setText(((CheckBoxState)state).getText());
        return cBox;
    }

    @Override
    protected Widget asNonEditableWidget(WidgetState state) {
        CheckBox checkBox = new CheckBox();
        checkBox.setText(((CheckBoxState)state).getText());
        checkBox.setEnabled(false);
        return checkBox;
    }
}


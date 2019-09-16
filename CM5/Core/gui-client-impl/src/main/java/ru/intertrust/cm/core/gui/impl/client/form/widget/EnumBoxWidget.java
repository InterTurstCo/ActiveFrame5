package ru.intertrust.cm.core.gui.impl.client.form.widget;

import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Widget;
import ru.intertrust.cm.core.business.api.dto.Constraint;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.form.widget.EnumBoxState;
import ru.intertrust.cm.core.gui.model.form.widget.WidgetState;
import ru.intertrust.cm.core.gui.model.validation.SimpleValidator;
import ru.intertrust.cm.core.gui.model.validation.Validator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Lesia Puhova
 * Date: 03.10.14
 * Time: 19:35
 */
@ComponentName("enumeration-box")
public class EnumBoxWidget extends BaseWidget {

    private Map<String, Value> displayTextToValue;

    @Override
    public void setValue(Object value) {
        //TODO: Implementation required
    }

    @Override
    public void disable(Boolean isDisabled) {
        //TODO: Implementation required
    }

    @Override
    public void reset() {
        //TODO: Implementation required
    }

    @Override
    public void applyFilter(String value) {
        //TODO: Implementation required
    }

    @Override
    public Object getValueTextRepresentation() {
        return getValue();
    }

    @Override
    public void setCurrentState(WidgetState currentState) {
        EnumBoxState enumBoxState = (EnumBoxState) currentState;
        displayTextToValue = enumBoxState.getDisplayTextToValue();
        if (!isEditable()) {
            ((Label) impl).setText(enumBoxState.getSelectedText());
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
            currentText = ((Label) impl).getText();
        } else {
            ListBox listBox = (ListBox) impl;
            currentText = listBox.getValue(listBox.getSelectedIndex());
        }
        return !currentText.equals(((EnumBoxState) getInitialData()).getSelectedText());
    }

    @Override
    protected WidgetState createNewState() {
        final EnumBoxState state = new EnumBoxState();
        state.setDisplayTextToValue(displayTextToValue);
        if (!isEditable()) {
            state.setSelectedText(((Label) impl).getText());
            return state;
        }
        ListBox listBox = (ListBox) impl;
        state.setSelectedText(listBox.getValue(listBox.getSelectedIndex()));
        return state;
    }

    @Override
    protected Widget asEditableWidget(WidgetState state) {
        ListBox listBox = new ListBox(false);
        listBox.addBlurHandler(new BlurHandler() {
            @Override
            public void onBlur(BlurEvent event) {
                validate();
            }
        });
        return listBox;
    }

    @Override
    protected Widget asNonEditableWidget(WidgetState state) {
        return new Label();
    }

    @Override
    public Component createNew() {
        return new EnumBoxWidget();
    }


    @Override
    public Object getValue() {
        if (impl instanceof ListBox) {
            ListBox listBox = (ListBox) impl;
            return listBox.getSelectedValue();
        } return ((Label)impl).getText();
    }

    @Override
    public List<Validator> getValidators() {
        // the only supported validator for this widget is not-empty validator, all the others are ignored
        List<Validator> validators = new ArrayList<>(1);
        for (Constraint constraint : getInitialData().getConstraints()) {
            if (constraint.getType() != Constraint.Type.SIMPLE) {
                continue;
            }
            if (Constraint.KEYWORD_NOT_EMPTY.equals(constraint.getParams().get(Constraint.PARAM_PATTERN))) {
                validators.add(new SimpleValidator(constraint));
            }
        }
        return validators;
    }
}

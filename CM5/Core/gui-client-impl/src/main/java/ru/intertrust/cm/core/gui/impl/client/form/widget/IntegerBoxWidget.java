package ru.intertrust.cm.core.gui.impl.client.form.widget;

import com.google.gwt.user.client.ui.HasText;
import ru.intertrust.cm.core.business.api.dto.Constraint;
import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.GuiException;
import ru.intertrust.cm.core.gui.model.form.widget.IntegerBoxState;
import ru.intertrust.cm.core.gui.model.form.widget.WidgetState;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author Denis Mitavskiy
 *         Date: 15.09.13
 *         Time: 22:47
 */
@ComponentName("integer-box")
public class IntegerBoxWidget extends TextBoxWidget {
    @Override
    public Component createNew() {
        return new IntegerBoxWidget();
    }

    public void setCurrentState(WidgetState currentState) {
        Long value = ((IntegerBoxState) currentState).getNumber();
        setTrimmedText((HasText) impl, value == null ? "" : value.toString());
    }

    @Override
    public WidgetState getCurrentState() {
        IntegerBoxState data = new IntegerBoxState();
        String text = getTrimmedText((HasText) impl);
        if (text == null) {
            return data;
        }
        Long value;
        try {
            value = Long.parseLong(text);
            data.setValue(value);
            return data;
        } catch (NumberFormatException e) {
            throw new GuiException(getMessageText("validate.integer"));
        }
    }

    @Override
    public List<Constraint> getConstraints() {
        List<Constraint> constraints = new ArrayList(super.getConstraints());
        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put(Constraint.PARAM_PATTERN, Constraint.KEYWORD_INTEGER);
        constraints.add(new Constraint(Constraint.CONSTRAINT_SIMPLE, params));
        return constraints;
    }
}

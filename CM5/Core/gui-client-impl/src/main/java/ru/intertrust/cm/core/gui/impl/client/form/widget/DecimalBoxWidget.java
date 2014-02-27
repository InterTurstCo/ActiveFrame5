package ru.intertrust.cm.core.gui.impl.client.form.widget;

import com.google.gwt.user.client.ui.HasText;
import ru.intertrust.cm.core.business.api.dto.Constraint;
import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.GuiException;
import ru.intertrust.cm.core.gui.model.form.widget.DecimalBoxState;
import ru.intertrust.cm.core.gui.model.form.widget.WidgetState;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author Denis Mitavskiy
 *         Date: 15.09.13
 *         Time: 22:47
 */
@ComponentName("decimal-box")
public class DecimalBoxWidget extends TextBoxWidget {
    @Override
    public Component createNew() {
        return new DecimalBoxWidget();
    }

    public void setCurrentState(WidgetState currentState) {
        BigDecimal value = ((DecimalBoxState) currentState).getNumber();
        setTrimmedText((HasText) impl, value == null ? "" : value.toString());
    }

    @Override
    public WidgetState getCurrentState() {
        DecimalBoxState data = new DecimalBoxState();
        String text = getTrimmedText((HasText) impl);
        if (text == null) {
            return data;
        }
        BigDecimal value;
        try {
            value = new BigDecimal(text);
            data.setNumber(value);
            return data;
        } catch (NumberFormatException e) {
            throw new GuiException(getMessageText("validate.decimal"));
        }
    }

    @Override
    public List<Constraint> getConstraints() {
        List<Constraint> constraints = new ArrayList(super.getConstraints());
        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put(Constraint.PARAM_PATTERN, Constraint.KEYWORD_DECIMAL);
        constraints.add(new Constraint(Constraint.CONSTRAINT_SIMPLE, params));
        return constraints;
    }
}

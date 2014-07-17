package ru.intertrust.cm.core.gui.impl.client.form.widget;

import com.google.gwt.user.client.ui.HasText;
import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.GuiException;
import ru.intertrust.cm.core.gui.model.form.widget.DecimalBoxState;
import ru.intertrust.cm.core.gui.model.form.widget.WidgetState;

import java.math.BigDecimal;

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
    protected boolean isChanged() {
        final BigDecimal initialNumber = ((DecimalBoxState) getInitialData()).getNumber();
        final String initialValue = initialNumber == null ? null : initialNumber.toString().trim();
        final String currentValue = getTrimmedText((HasText) impl);
        return initialValue == null ? currentValue != null : !initialValue.equals(currentValue);
    }

    @Override
    protected WidgetState createNewState() {
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
}

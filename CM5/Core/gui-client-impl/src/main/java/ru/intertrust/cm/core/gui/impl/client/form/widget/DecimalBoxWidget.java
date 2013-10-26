package ru.intertrust.cm.core.gui.impl.client.form.widget;

import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.GuiException;
import ru.intertrust.cm.core.gui.model.form.widget.DecimalBoxState;
import ru.intertrust.cm.core.gui.model.form.widget.IntegerBoxState;
import ru.intertrust.cm.core.gui.model.form.widget.WidgetState;

import java.math.BigDecimal;

/**
 * @author Denis Mitavskiy
 *         Date: 15.09.13
 *         Time: 22:47
 */
@ComponentName("decimal-box")
public class DecimalBoxWidget extends BaseWidget {
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
            throw new GuiException("Некорректный формат числа");
        }
    }

    @Override
    protected Widget asEditableWidget() {
        return new TextBox();
    }

    @Override
    protected Widget asNonEditableWidget() {
        return new Label();
    }

    private String getText() {
        Long value = this.<IntegerBoxState>getInitialData().getNumber();
        return value == null ? "" : value.toString();
    }
}

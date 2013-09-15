package ru.intertrust.cm.core.gui.impl.client.form.widget;

import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.GuiException;
import ru.intertrust.cm.core.gui.model.form.widget.IntegerBoxData;
import ru.intertrust.cm.core.gui.model.form.widget.WidgetData;

/**
 * @author Denis Mitavskiy
 *         Date: 15.09.13
 *         Time: 22:47
 */
@ComponentName("integer-box")
public class IntegerBoxWidget extends BaseWidget {
    @Override
    public Component createNew() {
        return new IntegerBoxWidget();
    }

    @Override
    public WidgetData getCurrentState() {
        IntegerBoxData data = new IntegerBoxData();
        String text = ((TextBox) impl).getText();
        if (text == null) {
            return data;
        }
        Long value;
        try {
            value = Long.parseLong(text);
            data.setValue(value);
            return data;
        } catch (NumberFormatException e) {
            throw new GuiException("Некорректный формат числа");
        }
    }

    @Override
    protected Widget asEditableWidget() {
        TextBox textBox = new TextBox();
        textBox.setValue(this.<IntegerBoxData>getInitialData().getValue().toString());
        return textBox;
    }

    @Override
    protected Widget asNonEditableWidget() {
        Label result = new Label();
        result.setText(this.<IntegerBoxData>getInitialData().getValue().toString());
        return result;
    }
}

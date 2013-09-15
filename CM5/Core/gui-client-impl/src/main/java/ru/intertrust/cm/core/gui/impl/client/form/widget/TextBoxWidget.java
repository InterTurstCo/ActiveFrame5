package ru.intertrust.cm.core.gui.impl.client.form.widget;

import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.form.widget.TextBoxData;
import ru.intertrust.cm.core.gui.model.form.widget.WidgetData;

/**
 * @author Denis Mitavskiy
 *         Date: 15.09.13
 *         Time: 22:40
 */
@ComponentName("text-box")
public class TextBoxWidget extends BaseWidget {
    @Override
    public Component createNew() {
        return new TextBoxWidget();
    }

    @Override
    public WidgetData getCurrentState() {
        TextBoxData data = new TextBoxData();
        data.setText(((TextBox) impl).getText());
        return data;
    }

    @Override
    protected Widget asEditableWidget() {
        TextBox textBox = new TextBox();
        textBox.setValue(this.<TextBoxData>getInitialData().getText());
        return textBox;
    }

    @Override
    protected Widget asNonEditableWidget() {
        Label result = new Label();
        result.setText(this.<TextBoxData>getInitialData().getText());
        return result;
    }
}

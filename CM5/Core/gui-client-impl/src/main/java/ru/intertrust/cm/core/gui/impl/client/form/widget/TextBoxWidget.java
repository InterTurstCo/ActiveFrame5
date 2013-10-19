package ru.intertrust.cm.core.gui.impl.client.form.widget;

import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.form.widget.TextBoxState;
import ru.intertrust.cm.core.gui.model.form.widget.WidgetState;

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

    public void setCurrentState(WidgetState currentState) {
        setTrimmedText((HasText) impl, ((TextBoxState) currentState).getText());
    }

    @Override
    public WidgetState getCurrentState() {
        TextBoxState data = new TextBoxState();
        data.setText(getTrimmedText((HasText) impl));
        return data;
    }

    @Override
    protected Widget asEditableWidget() {
        return new TextBox();
    }

    @Override
    protected Widget asNonEditableWidget() {
        return new Label();
    }
}

package ru.intertrust.cm.core.gui.impl.client.form.widget;

import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.Widget;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.form.widget.TextState;
import ru.intertrust.cm.core.gui.model.form.widget.WidgetState;

/**
 * @author Denis Mitavskiy
 *         Date: 26.09.13
 *         Time: 10:59
 */
@ComponentName("text-area")
public class TextAreaWidget extends TextBoxWidget {
    @Override
    public TextAreaWidget createNew() {
        return new TextAreaWidget();
    }

    @Override
    public TextState getCurrentState() {
        TextState data = new TextState();
        data.setText(getTrimmedText((HasText) impl));
        return data;
    }

    @Override
    protected Widget asEditableWidget(WidgetState state) {
        return new TextArea();
    }
}

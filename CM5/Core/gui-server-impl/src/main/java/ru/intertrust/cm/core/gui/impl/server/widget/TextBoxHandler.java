package ru.intertrust.cm.core.gui.impl.server.widget;

import ru.intertrust.cm.core.gui.api.server.widget.SingleObjectWidgetHandler;
import ru.intertrust.cm.core.gui.api.server.widget.WidgetContext;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.form.widget.TextBoxState;

/**
 * @author Denis Mitavskiy
 *         Date: 14.09.13
 *         Time: 16:59
 */
@ComponentName("text-box")
public class TextBoxHandler extends SingleObjectWidgetHandler {
    @Override
    public TextBoxState getInitialState(WidgetContext context) {
        return new TextBoxState(context.<String>getFieldPlainValue());
    }
}

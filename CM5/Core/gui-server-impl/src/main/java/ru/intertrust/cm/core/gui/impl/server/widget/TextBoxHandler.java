package ru.intertrust.cm.core.gui.impl.server.widget;

import ru.intertrust.cm.core.gui.api.server.widget.WidgetHandler;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.form.widget.TextBoxData;
import ru.intertrust.cm.core.gui.model.form.widget.WidgetContext;

/**
 * @author Denis Mitavskiy
 *         Date: 14.09.13
 *         Time: 16:59
 */
@ComponentName("text-box")
public class TextBoxHandler extends WidgetHandler {
    @Override
    public TextBoxData getInitialDisplayData(WidgetContext context) {
        return new TextBoxData(context.<String>getFieldPathPlainValue());
    }
}

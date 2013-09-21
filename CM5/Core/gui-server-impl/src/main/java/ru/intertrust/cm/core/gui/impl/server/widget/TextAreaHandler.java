package ru.intertrust.cm.core.gui.impl.server.widget;

import ru.intertrust.cm.core.gui.api.server.widget.WidgetHandler;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.form.FormData;
import ru.intertrust.cm.core.gui.model.form.widget.TextAreaData;
import ru.intertrust.cm.core.gui.model.form.widget.WidgetContext;

/**
 * @author Denis Mitavskiy
 *         Date: 21.09.13
 *         Time: 14:26
 */
@ComponentName("text-area")
public class TextAreaHandler extends WidgetHandler {
    @Override
    public TextAreaData getInitialDisplayData(WidgetContext context, FormData formData) {
        return new TextAreaData();
    }
}

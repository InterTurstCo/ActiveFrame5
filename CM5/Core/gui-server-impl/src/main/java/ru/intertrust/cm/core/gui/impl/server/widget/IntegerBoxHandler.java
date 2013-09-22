package ru.intertrust.cm.core.gui.impl.server.widget;

import ru.intertrust.cm.core.gui.api.server.widget.WidgetHandler;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.form.FormData;
import ru.intertrust.cm.core.gui.model.form.widget.IntegerBoxData;
import ru.intertrust.cm.core.gui.model.form.widget.WidgetContext;

/**
 * @author Denis Mitavskiy
 *         Date: 14.09.13
 *         Time: 17:00
 */
@ComponentName("integer-box")
public class IntegerBoxHandler extends WidgetHandler {
    @Override
    public IntegerBoxData getInitialDisplayData(WidgetContext context, FormData formData) {
        return new IntegerBoxData((Long) getFieldPathValue(context, formData));
    }
}

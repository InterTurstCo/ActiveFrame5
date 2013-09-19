package ru.intertrust.cm.core.gui.api.server.widget;

import ru.intertrust.cm.core.gui.api.server.ComponentHandler;
import ru.intertrust.cm.core.gui.model.form.FormData;
import ru.intertrust.cm.core.gui.model.form.widget.WidgetContext;
import ru.intertrust.cm.core.gui.model.form.widget.WidgetData;

/**
 * @author Denis Mitavskiy
 *         Date: 14.09.13
 *         Time: 16:58
 */
public abstract class WidgetHandler implements ComponentHandler {
    public abstract <T extends WidgetData> T getInitialDisplayData(WidgetContext context, FormData formData);
}

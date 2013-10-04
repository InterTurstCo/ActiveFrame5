package ru.intertrust.cm.core.gui.impl.server.widget;

import ru.intertrust.cm.core.gui.api.server.widget.WidgetHandler;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.form.FormObjects;
import ru.intertrust.cm.core.gui.model.form.widget.DateBoxData;
import ru.intertrust.cm.core.gui.model.form.widget.WidgetContext;

import java.util.Date;

/**
 * @author Denis Mitavskiy
 *         Date: 26.09.13
 *         Time: 12:41
 */
@ComponentName("date-box")
public class DateBoxHandler extends WidgetHandler {
    @Override
    public DateBoxData getInitialDisplayData(WidgetContext context, FormObjects formObjects) {
        return new DateBoxData((Date) getFieldPathValue(context, formObjects));
    }
}

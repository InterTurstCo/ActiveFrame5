package ru.intertrust.cm.core.gui.impl.server.widget;

import ru.intertrust.cm.core.gui.api.server.widget.WidgetHandler;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.form.FormData;
import ru.intertrust.cm.core.gui.model.form.widget.DecimalBoxData;
import ru.intertrust.cm.core.gui.model.form.widget.WidgetContext;

import java.math.BigDecimal;

/**
 * @author Denis Mitavskiy
 *         Date: 26.09.13
 *         Time: 11:40
 */
@ComponentName("decimal-box")
public class DecimalBoxHandler extends WidgetHandler {
    @Override
    public DecimalBoxData getInitialDisplayData(WidgetContext context, FormData formData) {
        return new DecimalBoxData((BigDecimal) getFieldPathValue(context, formData));
    }
}
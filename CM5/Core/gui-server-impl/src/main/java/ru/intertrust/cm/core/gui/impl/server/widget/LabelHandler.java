package ru.intertrust.cm.core.gui.impl.server.widget;

import ru.intertrust.cm.core.config.model.gui.form.widget.LabelConfig;
import ru.intertrust.cm.core.gui.api.server.widget.WidgetHandler;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.form.FormData;
import ru.intertrust.cm.core.gui.model.form.widget.LabelData;
import ru.intertrust.cm.core.gui.model.form.widget.WidgetContext;

/**
 * @author Denis Mitavskiy
 *         Date: 14.09.13
 *         Time: 17:04
 */
@ComponentName("label")
public class LabelHandler extends WidgetHandler {
    @Override
    public LabelData getInitialDisplayData(WidgetContext context, FormData formData) {
        LabelConfig widgetConfig = context.getWidgetConfig();
        return new LabelData(widgetConfig.getText());
    }
}

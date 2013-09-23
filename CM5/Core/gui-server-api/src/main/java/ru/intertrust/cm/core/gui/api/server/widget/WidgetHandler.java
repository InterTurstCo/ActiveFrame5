package ru.intertrust.cm.core.gui.api.server.widget;

import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.config.model.gui.form.widget.FieldPathConfig;
import ru.intertrust.cm.core.config.model.gui.form.widget.WidgetConfig;
import ru.intertrust.cm.core.gui.api.server.ComponentHandler;
import ru.intertrust.cm.core.gui.model.form.FieldPath;
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

    protected static FieldPath getFieldPath(WidgetConfig widgetConfig) {
        FieldPathConfig fieldPathConfig = widgetConfig.getFieldPathConfig();
        if (fieldPathConfig == null) {
            return null;
        }
        String fieldPath = fieldPathConfig.getValue();
        return fieldPath == null ? null : new FieldPath(fieldPath);
    }

    protected static <T> T getFieldPathValue(WidgetContext context, FormData formData) {
        Value fieldPathValue = formData.getFieldPathValue(getFieldPath(context.getWidgetConfig()));
        return fieldPathValue == null ? null : (T) fieldPathValue.get();
    }
}

package ru.intertrust.cm.core.gui.api.server.widget;

import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.ConfigurationService;
import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.config.model.gui.form.widget.FieldPathConfig;
import ru.intertrust.cm.core.config.model.gui.form.widget.WidgetConfig;
import ru.intertrust.cm.core.gui.api.server.ComponentHandler;
import ru.intertrust.cm.core.gui.api.server.GuiService;
import ru.intertrust.cm.core.gui.model.form.FieldPath;
import ru.intertrust.cm.core.gui.model.form.FormObjects;
import ru.intertrust.cm.core.gui.model.form.widget.WidgetContext;
import ru.intertrust.cm.core.gui.model.form.widget.WidgetData;

/**
 * @author Denis Mitavskiy
 *         Date: 14.09.13
 *         Time: 16:58
 */
public abstract class WidgetHandler implements ComponentHandler {

    @Autowired
    protected GuiService guiService;
    @Autowired
    protected CrudService crudService;
    @Autowired
    protected ConfigurationService configurationService;

    public abstract <T extends WidgetData> T getInitialDisplayData(WidgetContext context, FormObjects formObjects);

    protected static FieldPath getFieldPath(WidgetConfig widgetConfig) {
        FieldPathConfig fieldPathConfig = widgetConfig.getFieldPathConfig();
        if (fieldPathConfig == null) {
            return null;
        }
        String fieldPath = fieldPathConfig.getValue();
        return fieldPath == null ? null : new FieldPath(fieldPath);
    }

    protected static <T> T getFieldPathValue(WidgetContext context, FormObjects formObjects) {
        Value fieldPathValue = formObjects.getObjectValue(getFieldPath(context.getWidgetConfig()));
        return fieldPathValue == null ? null : (T) fieldPathValue.get();
    }
}

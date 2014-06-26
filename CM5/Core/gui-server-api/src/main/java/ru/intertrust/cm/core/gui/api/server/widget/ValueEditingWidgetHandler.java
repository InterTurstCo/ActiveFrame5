package ru.intertrust.cm.core.gui.api.server.widget;

import ru.intertrust.cm.core.config.FieldConfig;
import ru.intertrust.cm.core.gui.model.form.FieldPath;

/**
 * @author Denis Mitavskiy
 *         Date: 14.10.13
 *         Time: 14:58
 */
public abstract class ValueEditingWidgetHandler extends WidgetHandler {
    protected FieldConfig getFieldConfig(WidgetContext context) {
        final FieldPath fieldPath = context.getFirstFieldPath();
        String parentType = context.getFormObjects().getParentNode(fieldPath).getType();
        return configurationService.getFieldConfig(parentType, fieldPath.getFieldName());
    }
}

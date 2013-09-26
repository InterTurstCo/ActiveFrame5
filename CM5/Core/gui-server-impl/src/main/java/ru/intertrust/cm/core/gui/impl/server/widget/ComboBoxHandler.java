package ru.intertrust.cm.core.gui.impl.server.widget;

import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.ReferenceValue;
import ru.intertrust.cm.core.config.model.ReferenceFieldConfig;
import ru.intertrust.cm.core.config.model.ReferenceFieldTypeConfig;
import ru.intertrust.cm.core.config.model.gui.form.widget.ComboBoxConfig;
import ru.intertrust.cm.core.gui.api.server.widget.WidgetHandler;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.form.FieldPath;
import ru.intertrust.cm.core.gui.model.form.FormData;
import ru.intertrust.cm.core.gui.model.form.widget.ComboBoxData;
import ru.intertrust.cm.core.gui.model.form.widget.WidgetContext;

import java.util.List;

/**
 * @author Denis Mitavskiy
 *         Date: 26.09.13
 *         Time: 12:41
 */
@ComponentName("combo-box")
public class ComboBoxHandler extends WidgetHandler {
    @Override
    public ComboBoxData getInitialDisplayData(WidgetContext context, FormData formData) {
        ComboBoxConfig widgetConfig = context.getWidgetConfig();
        ReferenceValue fieldPathValue = formData.getFieldPathValue(getFieldPath(widgetConfig));
        Id selectedId = fieldPathValue == null ? null : fieldPathValue.get();
        FieldPath fieldPath = new FieldPath(widgetConfig.getFieldPathConfig().getValue());
        DomainObject rootObjectForComboBoxField = formData.getFieldPathObject(fieldPath.createFieldPathWithoutLastElement());
        String field = fieldPath.getLastElement();
        String rootObjectType = rootObjectForComboBoxField.getTypeName();
        ReferenceFieldConfig fieldConfig = (ReferenceFieldConfig) getConfigurationService().getFieldConfig(rootObjectType, field);
        List<ReferenceFieldTypeConfig> types = fieldConfig.getTypes();
        System.out.println("field = " + field);
        System.out.println(types);
        return new ComboBoxData();
    }
}

package ru.intertrust.cm.core.gui.model.form.widget;

import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.config.model.gui.form.widget.FieldPathConfig;
import ru.intertrust.cm.core.config.model.gui.form.widget.WidgetConfig;
import ru.intertrust.cm.core.gui.model.form.FieldPath;
import ru.intertrust.cm.core.gui.model.form.FormObjects;

import java.util.ArrayList;

/**
 * @author Denis Mitavskiy
 *         Date: 14.09.13
 *         Time: 17:54
 */
public class WidgetContext implements Dto {
    private WidgetConfig widgetConfig;
    private FormObjects formObjects;

    public WidgetContext() {
    }

    public WidgetContext(WidgetConfig widgetConfig, FormObjects formObjects) {
        this.widgetConfig = widgetConfig;
        this.formObjects = formObjects;
    }

    public <T extends WidgetConfig> T getWidgetConfig() {
        return (T) widgetConfig;
    }

    public void setWidgetConfig(WidgetConfig widgetConfig) {
        this.widgetConfig = widgetConfig;
    }

    public FormObjects getFormObjects() {
        return formObjects;
    }

    public void setFormObjects(FormObjects formObjects) {
        this.formObjects = formObjects;
    }

    public FieldPath getFieldPath() {
        FieldPathConfig fieldPathConfig = widgetConfig.getFieldPathConfig();
        if (fieldPathConfig == null) {
            return null;
        }
        String fieldPath = fieldPathConfig.getValue();
        return fieldPath == null ? null : new FieldPath(fieldPath);
    }

    public <T> T getFieldPathSinglePlainValue() {
        ArrayList<Value> objectValues = formObjects.getObjectValues(getFieldPath());
        if (objectValues == null) {
            return null;
        }
        if (objectValues.isEmpty()) {
            return null;
        }
        Value fieldPathValue = objectValues.get(0);
        return fieldPathValue == null ? null : (T) fieldPathValue.get();
    }

    public <T> T[] getFieldPathPlainValues() {
        ArrayList<Value> fieldPathValue = formObjects.getObjectValues(getFieldPath());
        if (fieldPathValue == null || fieldPathValue.isEmpty()) {
            return null;
        }
        int size = fieldPathValue.size();
        T[] result = (T[]) new Object[size];
        for (int i = 0; i < size; ++i) {
            Value value = fieldPathValue.get(i);
            result[i] = (T) value.get();
        }
        return result;
    }
}

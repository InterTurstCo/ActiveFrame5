package ru.intertrust.cm.core.gui.api.server.widget;

import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.config.gui.form.widget.FieldPathConfig;
import ru.intertrust.cm.core.config.gui.form.widget.WidgetConfig;
import ru.intertrust.cm.core.gui.model.form.FieldPath;
import ru.intertrust.cm.core.gui.model.form.FormObjects;

import java.util.ArrayList;

/**
 * @author Denis Mitavskiy
 *         Date: 14.09.13
 *         Time: 17:54
 */
public class WidgetContext implements Dto {
    private static final FieldPath NOT_INITIALIZED_FIELD_PATH = new FieldPath("+");

    private WidgetConfig widgetConfig;
    private FormObjects formObjects;
    private transient FieldPath fieldPath = NOT_INITIALIZED_FIELD_PATH;

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
        if (fieldPath != NOT_INITIALIZED_FIELD_PATH) {
            return fieldPath;
        }
        FieldPathConfig fieldPathConfig = widgetConfig.getFieldPathConfig();
        if (fieldPathConfig == null) {
            fieldPath = null;
            return null;
        }
        String fieldPathConfigValue = fieldPathConfig.getValue();
        fieldPath = fieldPathConfigValue == null ? null : new FieldPath(fieldPathConfigValue);
        return fieldPath;
    }

    public <T extends Value> T getValue() {
        return (T) formObjects.getFieldValue(getFieldPath());
    }

    public <T> T getFieldPlainValue() {
        Value fieldValue = getValue();
        return fieldValue == null ? null : (T) fieldValue.get();
    }

    public ArrayList<Id> getObjectIds() {
        return formObjects.getObjectIds(getFieldPath());
    }
}

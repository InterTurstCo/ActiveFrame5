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
    private static final FieldPath[] NOT_INITIALIZED_FIELD_PATHS = new FieldPath[0];
    private static final FieldPath[] EMPTY_PATHS = new FieldPath[1];

    private WidgetConfig widgetConfig;
    private FormObjects formObjects;
    private transient FieldPath[] fieldPaths = NOT_INITIALIZED_FIELD_PATHS;

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

    public FieldPath[] getFieldPaths() {
        if (fieldPaths != NOT_INITIALIZED_FIELD_PATHS) {
            return fieldPaths;
        }
        FieldPathConfig fieldPathConfig = widgetConfig.getFieldPathConfig();
        if (fieldPathConfig == null) {
            fieldPaths = EMPTY_PATHS;
            return fieldPaths;
        }
        fieldPaths = FieldPath.createPaths(fieldPathConfig.getValue());
        return fieldPaths;
    }

    public <T extends Value> T getValue() {
        return (T) formObjects.getFieldValue(getFieldPaths()[0]);
    }

    public <T> T getFieldPlainValue() {
        Value fieldValue = getValue();
        return fieldValue == null ? null : (T) fieldValue.get();
    }

    public ArrayList<Id> getObjectIds() {
        ArrayList<Id> result = new ArrayList<Id>();
        FieldPath[] fieldPaths = getFieldPaths();
        for (FieldPath fieldPath : fieldPaths) {
            ArrayList<Id> objectIds = formObjects.getObjectIds(fieldPath);
            if (objectIds == null) {
                continue;
            }
            // if field path a direct reference, then object id can be NULL
            result.addAll(objectIds);
        }
        return result;
    }
}

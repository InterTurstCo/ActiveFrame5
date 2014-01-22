package ru.intertrust.cm.core.gui.api.server.widget;

import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.config.gui.form.widget.FieldPathConfig;
import ru.intertrust.cm.core.config.gui.form.widget.WidgetConfig;
import ru.intertrust.cm.core.gui.model.form.FieldPath;
import ru.intertrust.cm.core.gui.model.form.FormObjects;

import java.util.ArrayList;
import java.util.Map;

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
    private transient Map<String, WidgetConfig> widgetConfigsById;
    private transient FieldPath[] fieldPaths = NOT_INITIALIZED_FIELD_PATHS;

    public WidgetContext() {
    }

    public WidgetContext(WidgetConfig widgetConfig, FormObjects formObjects) {
        this.widgetConfig = widgetConfig;
        this.formObjects = formObjects;
    }

    public WidgetContext(WidgetConfig widgetConfig, FormObjects formObjects, Map<String, WidgetConfig> widgetConfigsById) {
        this.widgetConfig = widgetConfig;
        this.formObjects = formObjects;
        this.widgetConfigsById = widgetConfigsById;
    }

    public <T extends WidgetConfig> T getWidgetConfig() {
        return (T) widgetConfig;
    }

    public void setWidgetConfig(WidgetConfig widgetConfig) {
        this.widgetConfig = widgetConfig;
    }

    public <T extends WidgetConfig> T getWidgetConfigById(String widgetId) {
        return widgetConfigsById == null ? null : (T) widgetConfigsById.get(widgetId);
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

    public <T extends Value> T getValue(FieldPath fieldPath) {
        return (T) formObjects.getFieldValue(fieldPath);
    }

    public Value[] getValues() {
        final FieldPath[] fieldPaths = getFieldPaths();
        Value[] result = new Value[fieldPaths.length];
        for (int i = 0; i < fieldPaths.length; ++i) {
            result[i] = formObjects.getFieldValue(fieldPaths[i]);
        }
        return result;
    }

    public <T> T getFieldPlainValue() {
        Value fieldValue = getValue();
        return fieldValue == null ? null : (T) fieldValue.get();
    }

    public <T> T getFieldPlainValue(FieldPath fieldPath) {
        Value fieldValue = getValue(fieldPath);
        return fieldValue == null ? null : (T) fieldValue.get();
    }

    public Object[] getFieldPlainValues() {
        final Value[] values = getValues();
        final Object[] result = new Object[values.length];
        for (int i = 0; i < values.length; ++i) {
            Value value = values[i];
            result[i] = value == null ? null : value.get();
        }
        return result;
    }

    public ArrayList<Id> getAllObjectIds() {
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

    public ArrayList<ArrayList<Id>> getObjectIds() {
        FieldPath[] fieldPaths = getFieldPaths();
        ArrayList<ArrayList<Id>> result = new ArrayList<ArrayList<Id>>(fieldPaths.length);
        for (int i = 0; i < fieldPaths.length; ++i) {
            FieldPath fieldPath = fieldPaths[i];
            ArrayList<Id> objectIds = formObjects.getObjectIds(fieldPath);
            if (objectIds == null) {
                result.add(new ArrayList<Id>(0));
            } else {
                // if field path a direct reference, then object id can be NULL
                result.add(objectIds);
            }
        }
        return result;
    }
}

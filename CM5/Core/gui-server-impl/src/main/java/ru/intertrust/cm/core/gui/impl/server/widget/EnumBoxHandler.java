package ru.intertrust.cm.core.gui.impl.server.widget;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import ru.intertrust.cm.core.business.api.dto.FieldType;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.business.api.util.ValueUtil;
import ru.intertrust.cm.core.config.FieldConfig;
import ru.intertrust.cm.core.config.gui.form.widget.EnumBoxConfig;
import ru.intertrust.cm.core.config.gui.form.widget.EnumMapConfig;
import ru.intertrust.cm.core.config.gui.form.widget.EnumMappingConfig;
import ru.intertrust.cm.core.gui.api.server.widget.ValueEditingWidgetHandler;
import ru.intertrust.cm.core.gui.api.server.widget.WidgetContext;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.form.widget.EnumBoxState;
import ru.intertrust.cm.core.gui.model.form.widget.WidgetState;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Lesia Puhova
 *         Date: 06.10.14
 *         Time: 12:36
 */
@ComponentName("enumeration-box")
public class EnumBoxHandler extends ValueEditingWidgetHandler {

    @Override
    public EnumBoxState getInitialState(WidgetContext context) {
        EnumBoxState enumBoxState = new EnumBoxState();
        final FieldConfig fieldConfig = getFieldConfig(context);

        FieldType fieldType = fieldConfig == null ? null : fieldConfig.getFieldType();
        enumBoxState.setFieldType(fieldType);

        EnumBoxConfig config = context.getWidgetConfig();
        Map<String, Value> displayTextToValue;
        if (config.getEnumMapProviderConfig() != null) {
            EnumerationMapProvider provider = (EnumerationMapProvider)applicationContext.getBean(config.getEnumMapProviderConfig().getComponent());
            displayTextToValue = provider.getMap(context);
        } else {
            displayTextToValue = populateMapping(config.getEnumMappingConfig(), fieldType);
        }

        Value dbValue = context.getValue();
        String selectedText = findTextForValue(dbValue, displayTextToValue);
        if (selectedText == null) {
            if (dbValue != null && dbValue.get() != null) {
                selectedText = dbValue.get().toString();
                displayTextToValue.put(selectedText, dbValue);
            } else if (fieldConfig == null || !fieldConfig.isNotNull()) {
                selectedText = "";
                displayTextToValue.put(selectedText, dbValue);
            }
        }
        enumBoxState.setDisplayTextToValue(displayTextToValue);
        enumBoxState.setSelectedText(selectedText);
        return enumBoxState;
    }

    private Map<String, Value> populateMapping(EnumMappingConfig mappingConfig, FieldType fieldType) {
        Map<String, Value> displayTextToValue = new LinkedHashMap<>();
        for (EnumMapConfig mapConfig : mappingConfig.getEnumMapConfigs()) {
            String displayText = mapConfig.getDisplayText() != null ? mapConfig.getDisplayText() :
                    mapConfig.getValue() != null ? mapConfig.getValue() : "";
            displayTextToValue.put(displayText, stringToValue(mapConfig.getValue(),
                    fieldType, mapConfig.isNullValue()));
        }
        return displayTextToValue;
    }

    private Value stringToValue(String string, FieldType fieldType, boolean isNull) {
        if (isNull || (string.isEmpty() && fieldType != FieldType.STRING)) {
            string = null;
        }
        if(fieldType == null){
            fieldType = FieldType.STRING;
        }
        return ValueUtil.stringValueToObject(string, fieldType);
    }

    private String findTextForValue(Value value, Map<String, Value> displayTextToValue) {
        for (Map.Entry<String, Value> entry : displayTextToValue.entrySet()) {
            if (value != null && value.equals(entry.getValue()) ||
                    value == null  && (entry.getValue() == null || entry.getValue().get() == null)) {
                return entry.getKey();
            }
        }
        return null;
    }

    @Override
    public Value getValue(WidgetState state) {
        EnumBoxState enumBoxState = (EnumBoxState) state;
        return  enumBoxState.getDisplayTextToValue().get(enumBoxState.getSelectedText());
    }
}

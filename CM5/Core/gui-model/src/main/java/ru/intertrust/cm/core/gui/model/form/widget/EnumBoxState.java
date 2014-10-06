package ru.intertrust.cm.core.gui.model.form.widget;

import ru.intertrust.cm.core.business.api.dto.FieldType;
import ru.intertrust.cm.core.business.api.dto.Value;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Lesia Puhova
 *         Date: 03.10.14
 *         Time: 19:30
 */
public class EnumBoxState extends WidgetState {

    private FieldType fieldType;
    private String selectedText;
    private Map<String, Value> displayTextToValue = new LinkedHashMap<>();

    public String getSelectedText() {
        return selectedText;
    }

    public void setSelectedText(String selectedText) {
        this.selectedText = selectedText;
    }

    public Map<String, Value> getDisplayTextToValue() {
        return displayTextToValue;
    }

    public void setDisplayTextToValue(Map<String, Value> displayTextToValue) {
        this.displayTextToValue = displayTextToValue;
    }

    public FieldType getFieldType() {
        return fieldType;
    }

    public void setFieldType(FieldType fieldType) {
        this.fieldType = fieldType;
    }
}

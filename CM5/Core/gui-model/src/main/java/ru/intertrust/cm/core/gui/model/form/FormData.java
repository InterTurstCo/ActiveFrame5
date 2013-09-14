package ru.intertrust.cm.core.gui.model.form;

import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.gui.model.GuiException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Denis Mitavskiy
 *         Date: 14.09.13
 *         Time: 18:09
 */
public class FormData implements Dto {
    private Map<String, List<Value>> fieldPathValues;

    public FormData() {
        fieldPathValues = new HashMap<>();
    }

    public void setFieldPathValue(String fieldPath, Value value) {
        ArrayList<Value> list = new ArrayList<>(1);
        list.add(value);
        this.fieldPathValues.put(fieldPath, list);
    }

    public void setFieldPathValues(String fieldPath, List<Value> values) {
        this.fieldPathValues.put(fieldPath, values);
    }

    public <T extends Value> T getFieldPathValue(String fieldPath) {
        List<Value> valueList = this.getFieldPathValues(fieldPath);
        if (valueList == null) {
            return null;
        }
        if (valueList.size() > 1) {
            throw new GuiException("Field path: " + fieldPath + " contains more than 1 value");
        }
        return (T) valueList.get(0);
    }

    public <T extends Value> List<T> getFieldPathValues(String fieldPath) {
        return (List<T>) this.fieldPathValues.get(fieldPath);
    }
}

package ru.intertrust.cm.core.business.api.dto;

import ru.intertrust.cm.core.business.api.util.BusinessUtil;

import java.util.ArrayList;
import java.util.LinkedHashMap;

/**
 * Author: Denis Mitavskiy
 * Date: 23.05.13
 * Time: 17:13
 */
public class GenericIdentifiableObject implements IdentifiableObject {
    private Id id;
    private LinkedHashMap<String, Value> fieldValues;

    /**
     * Создаёт объект
     */
    public GenericIdentifiableObject() {
        fieldValues = new LinkedHashMap<>();
    }

    @Override
    public Id getId() {
        return id;
    }

    @Override
    public void setId(Id id) {
        this.id = id;
    }

    @Override
    public void setValue(String field, Value value) {
        fieldValues.put(field, value);
    }

    @Override
    public Value getValue(String field) {
        return fieldValues.get(field);
    }

    @Override
    public ArrayList<String> getFields() {
        return new ArrayList<>(fieldValues.keySet());
    }

    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append('{').append('\n');
        result.append(BusinessUtil.getDetailedDescription(this));
        result.append('}');
        return result.toString();
    }
}

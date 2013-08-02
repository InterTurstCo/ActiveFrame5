package ru.intertrust.cm.core.business.api.dto;

import ru.intertrust.cm.core.business.api.util.ModelUtil;

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
        fieldValues = new LinkedHashMap<String, Value>(); // параметры специфицированы явно, для поддержки GWT
    }

    /**
     * Создаёт копию идентифицируемого объекта
     * 
     * @param source исходный объект
     */
    public GenericIdentifiableObject(IdentifiableObject source) {
        id = source.getId();
        ArrayList<String> sourceFields = source.getFields();
        fieldValues = new LinkedHashMap<String, Value>(sourceFields.size());
        for (String field : sourceFields) {
            fieldValues.put(field, source.getValue(field));
        }
    }

    @Override
    public Id getId() {
        return id;
    }

    //@Override
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
        return new ArrayList<String>(fieldValues.keySet());
    }

    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append('{').append('\n');
        result.append(ModelUtil.getDetailedDescription(this));
        result.append('}');
        return result.toString();
    }
}

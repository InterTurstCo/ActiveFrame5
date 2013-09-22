package ru.intertrust.cm.core.business.api.dto;

import ru.intertrust.cm.core.business.api.util.ModelUtil;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;

/**
 * Author: Denis Mitavskiy
 * Date: 23.05.13
 * Time: 17:13
 */
public class GenericIdentifiableObject implements IdentifiableObject {

    private Id id;
    protected LinkedHashMap<String, Value> fieldValues;

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
        this();
        setId(source.getId());
        ArrayList<String> sourceFields = source.getFields();
        fieldValues = new LinkedHashMap<String, Value>(sourceFields.size());
        for (String field : sourceFields) {
            setValue(field, source.getValue(field));
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
    public <T extends Value> T getValue(String field) {
        return (T) fieldValues.get(field);
    }

    @Override
    public ArrayList<String> getFields() {
        return new ArrayList<String>(fieldValues.keySet());
    }

    @Override
    public void setString(String field, String value) {
        if (value != null) {
            fieldValues.put(field, new StringValue(value));
        }
    }

    @Override
    public String getString(String field) {
        StringValue value = getValue(field);
        return value == null ? null : value.get();
    }

    @Override
    public void setLong(String field, Long value) {
        if (value != null) {
            fieldValues.put(field, new LongValue(value));
        }
    }

    @Override
    public Long getLong(String field) {
        LongValue value = getValue(field);
        return value == null ? null : value.get();
    }

    @Override
    public void setBoolean(String field, Boolean value) {
        if (value != null) {
            fieldValues.put(field, new BooleanValue(value));
        }
    }

    @Override
    public Boolean getBoolean(String field) {
        BooleanValue value = getValue(field);
        return value == null ? null : value.get();
    }

    @Override
    public void setDecimal(String field, BigDecimal value) {
        if (value != null) {
            fieldValues.put(field, new DecimalValue(value));
        }
    }

    @Override
    public BigDecimal getDecimal(String field) {
        DecimalValue value = getValue(field);
        return value == null ? null : value.get();
    }

    @Override
    public void setTimestamp(String field, Date value) {
        if (value != null) {
            fieldValues.put(field, new TimestampValue(value));
        }
    }

    @Override
    public Date getTimestamp(String field) {
        TimestampValue value = getValue(field);
        return value == null ? null : value.get();
    }

    @Override
    public void setReference(String field, DomainObject domainObject) {
        if (domainObject != null) {
            Id id = domainObject.getId();
            if (id != null) {
                fieldValues.put(field, new ReferenceValue(id));
            }
        }
    }

    @Override
    public void setReference(String field, Id id) {
        if (id != null) {
            fieldValues.put(field, new ReferenceValue(id));
        }
    }

    @Override
    public Id getReference(String field) {
        ReferenceValue value = getValue(field);
        return value == null ? null : value.get();
    }

    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append('{').append('\n');
        result.append(ModelUtil.getDetailedDescription(this));
        result.append('}');
        return result.toString();
    }
}
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
    protected boolean dirty = false;

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
        dirty = source.isDirty();
    }

    @Override
    public Id getId() {
        return id;
    }

    //@Override
    public void setId(Id id) {
        this.id = id;
        dirty = true;
    }

    @Override
    public void setValue(String field, Value value) {
        if (value != null) {
            fieldValues.put(field, value);
        } else {
            fieldValues.remove(field);
        }
        dirty = true;
    }

    @SuppressWarnings("unchecked")
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
        } else {
            fieldValues.remove(field);
        }
        dirty = true;
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
        } else {
            fieldValues.remove(field);
        }
        dirty = true;
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
        } else {
            fieldValues.remove(field);
        }
        dirty = true;
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
        } else {
            fieldValues.remove(field);
        }
        dirty = true;
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
        } else {
            fieldValues.remove(field);
        }
        dirty = true;
    }

    @Override
    public Date getTimestamp(String field) {
        TimestampValue value = getValue(field);
        return value == null ? null : value.get();
    }

    @Override
    public void setTimelessDate(String field, TimelessDate value) {
        if (value != null) {
            fieldValues.put(field, new TimelessDateValue(value));
        } else {
            fieldValues.remove(field);
        }
        dirty = true;
    }

    @Override
    public TimelessDate getTimelessDate(String field) {
        TimelessDateValue value = getValue(field);
        return value == null ? null : value.get();
    }

    @Override
    public void setDateTimeWithTimeZone(String field, DateTimeWithTimeZone value) {
        if (value != null) {
            fieldValues.put(field, new DateTimeWithTimeZoneValue(value));
        } else {
            fieldValues.remove(field);
        }
        dirty = true;
    }

    @Override
    public DateTimeWithTimeZone getDateTimeWithTimeZone(String field) {
        DateTimeWithTimeZoneValue value = getValue(field);
        return value == null ? null : value.get();
    }

    @Override
    public void setReference(String field, DomainObject domainObject) {
        if (domainObject != null) {
            Id id = domainObject.getId();
            if (id != null) {
                fieldValues.put(field, new ReferenceValue(id));
            } else {
                throw new NullPointerException("Impossible to reference to an unsaved object");
            }
        } else {
            fieldValues.remove(field);
        }
        dirty = true;
    }

    @Override
    public void setReference(String field, Id id) {
        if (id != null) {
            fieldValues.put(field, new ReferenceValue(id));
        } else {
            fieldValues.remove(field);
        }
        dirty = true;
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

    @Override
    public boolean isDirty() {
        return dirty;
    }

    public void resetDirty() {
        dirty = false;
    }
}
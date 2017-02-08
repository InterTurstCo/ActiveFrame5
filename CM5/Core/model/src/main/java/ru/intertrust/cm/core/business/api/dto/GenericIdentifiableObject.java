package ru.intertrust.cm.core.business.api.dto;

import ru.intertrust.cm.core.business.api.util.ModelUtil;
import ru.intertrust.cm.core.business.api.util.ObjectCloner;
import ru.intertrust.cm.core.model.GwtIncompatible;

import java.math.BigDecimal;
import java.util.*;

/**
 * Author: Denis Mitavskiy
 * Date: 23.05.13
 * Time: 17:13
 */
public class GenericIdentifiableObject implements IdentifiableObject, Cloneable {

    private Id id;
    protected LinkedHashMap<String, Value> fieldValues;
    protected LinkedHashSet<String> originalKeys = new LinkedHashSet<>();
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
        originalKeys.add(field);
        if (value != null) {
            fieldValues.put(getLowerCaseKey(field), value);
        } else {
            fieldValues.remove(getLowerCaseKey(field));
        }
        dirty = true;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends Value> T getValue(String field) {
        return (T) fieldValues.get(getLowerCaseKey(field));
    }

    @Override
    public ArrayList<String> getFields() {
        return new ArrayList<>(originalKeys);
    }

    @Override
    public void setString(String field, String value) {
        originalKeys.add(field);
        if (value != null) {
            fieldValues.put(getLowerCaseKey(field), new StringValue(value));
        } else {
            fieldValues.remove(getLowerCaseKey(field));
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
        originalKeys.add(field);
        if (value != null) {
            fieldValues.put(getLowerCaseKey(field), new LongValue(value));
        } else {
            fieldValues.remove(getLowerCaseKey(field));
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
        originalKeys.add(field);
        if (value != null) {
            fieldValues.put(getLowerCaseKey(field), new BooleanValue(value));
        } else {
            fieldValues.remove(getLowerCaseKey(field));
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
        originalKeys.add(field);
        if (value != null) {
            fieldValues.put(getLowerCaseKey(field), new DecimalValue(value));
        } else {
            fieldValues.remove(getLowerCaseKey(field));
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
        originalKeys.add(field);
        if (value != null) {
            fieldValues.put(getLowerCaseKey(field), new DateTimeValue(value));
        } else {
            fieldValues.remove(getLowerCaseKey(field));
        }
        dirty = true;
    }

    @Override
    public Date getTimestamp(String field) {
        DateTimeValue value = getValue(field);
        return value == null ? null : value.get();
    }

    @Override
    public void setTimelessDate(String field, TimelessDate value) {
        originalKeys.add(field);
        if (value != null) {
            fieldValues.put(getLowerCaseKey(field), new TimelessDateValue(value));
        } else {
            fieldValues.remove(getLowerCaseKey(field));
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
        originalKeys.add(field);
        if (value != null) {
            fieldValues.put(getLowerCaseKey(field), new DateTimeWithTimeZoneValue(value));
        } else {
            fieldValues.remove(getLowerCaseKey(field));
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
        originalKeys.add(field);
        if (domainObject != null) {
            Id id = domainObject.getId();
            if (id != null) {
                fieldValues.put(getLowerCaseKey(field), new ReferenceValue(id));
            } else {
                throw new NullPointerException("Impossible to reference to an unsaved object");
            }
        } else {
            fieldValues.remove(getLowerCaseKey(field));
        }
        dirty = true;
    }

    @Override
    public void setReference(String field, Id id) {
        originalKeys.add(field);
        if (id != null) {
            fieldValues.put(getLowerCaseKey(field), new ReferenceValue(id));
        } else {
            fieldValues.remove(getLowerCaseKey(field));
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

    public boolean containsFieldValues(Map<String, Value> fieldValues) {
        return containsFieldValues(this, fieldValues);
    }

    public void retainFields(Set<String> fields) {
        ArrayList<String> toRemove = new ArrayList<>();
        for (String originalKey : originalKeys) {
            if (!fields.contains(originalKey)) {
                toRemove.add(originalKey);
            }
        }
        for (String field : toRemove) {
            originalKeys.remove(field);
            fieldValues.remove(field.toLowerCase());
        }
    }

    @Deprecated
    /**
     * Plaform-based application Developer! Do not use this method, use interface methods!
     */
    public LinkedHashMap<String, Value> getFieldValues() {
        return fieldValues;
    }

    static boolean containsFieldValues(IdentifiableObject object, Map<String, Value> fieldValues) {
        final Set<String> fields = fieldValues.keySet();
        for (String field : fields) {
            final Value thisValue = object.getValue(field);
            final Value valueToCheck = fieldValues.get(field);
            boolean thisValueNull = thisValue == null || thisValue.get() == null;
            boolean valueToCheckNull = valueToCheck == null || valueToCheck.get() == null;
            if (thisValueNull && valueToCheckNull) {
                continue;
            }
            if (thisValueNull && !valueToCheckNull || !thisValueNull && valueToCheckNull || !thisValue.equals(valueToCheck)) {
                return false;
            }
        }
        return true;
    }

    private String getLowerCaseKey(String key) {
        String lowerCaseKey = null;
        if (key != null) {
            lowerCaseKey = key.toLowerCase();
        }
        return lowerCaseKey;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        GenericIdentifiableObject that = (GenericIdentifiableObject) o;

        if (id != null ? !id.equals(that.id) : that.id != null) {
            return false;
        }
        final boolean fieldValuesEmpty = fieldValues == null || fieldValues.isEmpty();
        final boolean thatFieldValuesEmpty = that.fieldValues == null || that.fieldValues.isEmpty();
        if (fieldValuesEmpty && thatFieldValuesEmpty) {
            return true;
        }
        if (!fieldValuesEmpty && thatFieldValuesEmpty || fieldValuesEmpty && !thatFieldValuesEmpty) {
            return false;
        }
        // we don't compare original keys, as presence of NULL-value in one object and its absense of this value
        // in another means the same
        final Set<String> fieldNames = fieldValues.keySet();
        final Set<String> thatFieldNames = that.fieldValues.keySet();
        HashSet<String> allFieldNames = new HashSet<>(2 * (fieldNames.size() + thatFieldNames.size()));
        for (String fieldName : fieldNames) {
            allFieldNames.add(fieldName.toLowerCase());
        }
        for (String thatFieldName : thatFieldNames) {
            allFieldNames.add(thatFieldName.toLowerCase());
        }
        for (String fieldName : allFieldNames) {
            Value value = getValue(fieldName);
            Value thatValue = that.getValue(fieldName);
            Object valueObj = value == null ? null : value.get();
            Object thatValueObj = thatValue == null ? null : thatValue.get();
            if (!Objects.equals(valueObj, thatValueObj)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Override
    @GwtIncompatible
    public Object clone() throws CloneNotSupportedException {
        final GenericIdentifiableObject clone = (GenericIdentifiableObject) super.clone();
        // id is immutable, nothing to do with it
        clone.originalKeys = new LinkedHashSet<>(clone.originalKeys); // everything is a string, so just put them into a different map
        clone.fieldValues = new LinkedHashMap<>(clone.fieldValues);

        final ObjectCloner cloner = ObjectCloner.getInstance();
        for (Map.Entry<String, Value> stringValueEntry : clone.fieldValues.entrySet()) {
            final Value value = stringValueEntry.getValue();
            if (value != null && !value.isImmutable()) {
                stringValueEntry.setValue(cloner.cloneObject(value));
            }
        }
        return clone;
    }
}
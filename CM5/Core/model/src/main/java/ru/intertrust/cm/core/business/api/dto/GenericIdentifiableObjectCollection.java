package ru.intertrust.cm.core.business.api.dto;

import ru.intertrust.cm.core.business.api.util.ModelUtil;
import ru.intertrust.cm.core.config.FieldConfig;

import java.math.BigDecimal;
import java.util.*;

/**
 * Коллекция объектов, наделённых идентификатором
 * <p/>
 * Author: Denis Mitavskiy
 * Date: 23.05.13
 * Time: 10:54
 */
public class GenericIdentifiableObjectCollection implements IdentifiableObjectCollection {

    private ArrayList<IdentifiableObject> list = new ArrayList<IdentifiableObject>();
    private CaseInsensitiveKeyHashMap<Integer> fieldIndexes = new CaseInsensitiveKeyHashMap<Integer>();
    private ArrayList<String> fields;
    private ArrayList<FieldConfig> fieldConfigs;

    public GenericIdentifiableObjectCollection() {
    }
    

    /**
     * Обертывает @{link HashMap} для хранения ключей объектов в нижнем регистре (регистронезависимо).
     * @author atsvetkov
     *
     */
    private static class CaseInsensitiveKeyHashMap<T> {
        private HashMap<String, T> map = new HashMap<String, T>();

        public T put(String key, T value) {
            String lowerCaseKey = null;
            if (key != null) {
                lowerCaseKey = key.toLowerCase();
            }
            return map.put(lowerCaseKey, value);
        }

        public T get(String key) {
            String lowerCaseKey = null;
            if (key != null) {
                lowerCaseKey = key.toLowerCase();
            }
            return map.get(lowerCaseKey);

        }
        
    }

    //TODO Удалить после исправления JdbcDatabaseMetaData.java
    @Override
    public void setFields(List<String> fields) {
        if (this.fields != null) {
            throw new IllegalArgumentException("Collection fields are already set");
        }
        if (fields == null) {
            this.fields = new ArrayList<String>();
        } else {
            this.fields = new ArrayList<String>(fields);
        }
        int fieldIndex = 0;
        for (String field : this.fields) {
            fieldIndexes.put(field, fieldIndex);
            ++fieldIndex;
        }
    }

    @Override
    public void setFieldsConfiguration(List<FieldConfig> fieldConfigs) {
        if (this.fieldConfigs != null) {
            throw new IllegalArgumentException("Collection field configs are already set");
        }
        if (fieldConfigs == null) {
            this.fieldConfigs = new ArrayList<FieldConfig>(0);
        } else {
            this.fieldConfigs = new ArrayList<FieldConfig>(fieldConfigs);
        }
        int fieldIndex = 0;
        for (FieldConfig field : this.fieldConfigs) {
            fieldIndexes.put(field.getName(), fieldIndex);
            ++fieldIndex;
        }
    }

    @Override
    public Id getId(int row) {
        return list.get(row).getId();
    }

    @Override
    public void setId(int row, Id id) {
        addRowsIfNeeded(row);
        list.get(row).setId(id);
    }

    @Override
    public void set(int fieldIndex, int row, Value value) {
        addRowsIfNeeded(row);
        ((FastIdentifiableObjectImpl) list.get(row)).setValue(fieldIndex, value);
    }

    @Override
    public void set(String field, int row, Value value) {
        addRowsIfNeeded(row);
        list.get(row).setValue(field, value);
    }

    @Override
    public IdentifiableObject get(int row) {
        return list.get(row);
    }

    @Override
    public Value get(String field, int row) {
        return list.get(row).getValue(field);
    }

    @Override
    public Value get(int fieldIndex, int row) {
        return ((FastIdentifiableObjectImpl) list.get(row)).getValue(fieldIndex);
    }

    @Override
    public int getFieldIndex(String field) {

        return fieldIndexes.get(field);
    }

    @Override
    public ArrayList<String> getFields() {
        if (this.fields == null) {
            ArrayList<String> fieldNames = new ArrayList<String>();

            for (FieldConfig fieldConfig : fieldConfigs) {
                fieldNames.add(fieldConfig.getName());
            }

            this.fields = fieldNames;
        }
        return this.fields;
    }

    @Override
    public ArrayList<FieldConfig> getFieldsConfiguration() {
        return fieldConfigs == null ? new ArrayList<FieldConfig>(0) : new ArrayList<>(fieldConfigs);
    }

    @Override
    public int size() {
        return list.size();
    }

    @Override
    public ListIterator<IdentifiableObject> iterator() {
        return list.listIterator();
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        for (IdentifiableObject obj : this) {
            result.append(ModelUtil.getTableRowDescription(obj)).append('\n');
        }
        return result.toString();
    }

    private void addRowsIfNeeded(int row) {
        int size = size();
        if (row < size) {
            return;
        }
        int rowsToAdd = row - size + 1;
        for (int i = 0; i < rowsToAdd; ++i) {
            list.add(createObjectByTemplate());
        }
    }

    public void resetDirty(int row) {
        ((FastIdentifiableObjectImpl) list.get(row)).resetDirty();
    }

    private IdentifiableObject createObjectByTemplate() {
        if (fieldConfigs == null) {
            setFieldsConfiguration(new ArrayList<FieldConfig>(0));
        }
        return new FastIdentifiableObjectImpl(this);
    }

    /**
     * Имплементация, позволяющая получить быстрый доступ к значениям полей по индексу
     */
    static class FastIdentifiableObjectImpl implements IdentifiableObject {
        private Id id;
        private ArrayList<Value> fieldValues;
        private IdentifiableObjectCollection collection;
        private boolean dirty = false;

        FastIdentifiableObjectImpl() {
        }

        private FastIdentifiableObjectImpl(IdentifiableObjectCollection collection) {
            this.collection = collection;
            int fieldsSize = collection.getFieldsConfiguration().size();
            fieldValues = new ArrayList<Value>(fieldsSize);
            for (int i = 0; i < fieldsSize; ++i) {
                fieldValues.add(null);
            }
        }

        @Override
        public Id getId() {
            return id;
        }

        @Override
        public void setId(Id id) {
            this.id = id;
            dirty = true;
        }

        @Override
        public void setValue(String field, Value value) {
            fieldValues.set(collection.getFieldIndex(field), value);
            dirty = true;
        }

        @Override
        public <T extends Value> T getValue(String field) {
            return (T) fieldValues.get(collection.getFieldIndex(field));
        }

        public void setValue(int index, Value value) {
            fieldValues.set(index, value);
            dirty = true;
        }

        public <T extends Value> T getValue(int index) {
            return (T) fieldValues.get(index);
        }

        @Override
        public void setString(String field, String value) {
            fieldValues.set(collection.getFieldIndex(field), new StringValue(value));
            dirty = true;
        }

        public void setString(int index, String value) {
            fieldValues.set(index, new StringValue(value));
            dirty = true;
        }

        @Override
        public String getString(String field) {
            return this.<StringValue>getValue(field).get();
        }

        public String getString(int index) {
            return this.<StringValue>getValue(index).get();
        }


        @Override
        public void setLong(String field, Long value) {
            fieldValues.set(collection.getFieldIndex(field), new LongValue(value));
            dirty = true;
        }

        public void setLong(int index, Long value) {
            fieldValues.set(index, new LongValue(value));
            dirty = true;
        }

        @Override
        public Long getLong(String field) {
            return this.<LongValue>getValue(field).get();
        }

        public Long getLong(int index) {
            return this.<LongValue>getValue(index).get();
        }

        @Override
        public void setBoolean(String field, Boolean value) {
            fieldValues.set(collection.getFieldIndex(field), new BooleanValue(value));
            dirty = true;
        }

        public void setBoolean(int index, Boolean value) {
            fieldValues.set(index, new BooleanValue(value));
            dirty = true;
        }

        @Override
        public Boolean getBoolean(String field) {
            return this.<BooleanValue>getValue(field).get();
        }

        public Boolean getBoolean(int index) {
            return this.<BooleanValue>getValue(index).get();
        }

        @Override
        public void setDecimal(String field, BigDecimal value) {
            fieldValues.set(collection.getFieldIndex(field), new DecimalValue(value));
            dirty = true;
        }

        public void setDecimal(int index, BigDecimal value) {
            fieldValues.set(index, new DecimalValue(value));
            dirty = true;
        }

        @Override
        public BigDecimal getDecimal(String field) {
            return this.<DecimalValue>getValue(field).get();
        }

        public BigDecimal getDecimal(int index) {
            return this.<DecimalValue>getValue(index).get();
        }

        @Override
        public void setTimestamp(String field, Date value) {
            fieldValues.set(collection.getFieldIndex(field), new TimestampValue(value));
            dirty = true;
        }

        public void setTimestamp(int index, Date value) {
            fieldValues.set(index, new TimestampValue(value));
            dirty = true;
        }

        @Override
        public Date getTimestamp(String field) {
            return this.<TimestampValue>getValue(field).get();
        }

        @Override
        public void setTimelessDate(String field, TimelessDate value) {
            fieldValues.set(collection.getFieldIndex(field), new TimelessDateValue(value));
            dirty = true;
        }

        public void setTimelessDate(int index, TimelessDate value) {
            fieldValues.set(index, new TimelessDateValue(value));
            dirty = true;
        }

        @Override
        public TimelessDate getTimelessDate(String field) {
            return this.<TimelessDateValue>getValue(field).get();
        }

        public Date getTimestamp(int index) {
            return this.<TimestampValue>getValue(index).get();
        }

        @Override
        public void setDateTimeWithTimeZone(String field, DateTimeWithTimeZone value) {
            fieldValues.set(collection.getFieldIndex(field), new DateTimeWithTimeZoneValue(value));
            dirty = true;
        }

        public void setDateTimeWithTimeZone(int index, DateTimeWithTimeZone value) {
            fieldValues.set(index, new DateTimeWithTimeZoneValue(value));
            dirty = true;
        }

        @Override
        public DateTimeWithTimeZone getDateTimeWithTimeZone(String field) {
            return this.<DateTimeWithTimeZoneValue>getValue(field).get();
        }

        public DateTimeWithTimeZone getDateTimeWithTimeZone(int index) {
            return this.<DateTimeWithTimeZoneValue>getValue(index).get();
        }

        @Override
        public void setReference(String field, DomainObject domainObject) {
            fieldValues.set(collection.getFieldIndex(field), new ReferenceValue(domainObject.getId()));
            dirty = true;
        }

        public void setReference(int index, DomainObject domainObject) {
            fieldValues.set(index, new ReferenceValue(domainObject.getId()));
            dirty = true;
        }

        @Override
        public void setReference(String field, Id id) {
            fieldValues.set(collection.getFieldIndex(field), new ReferenceValue(id));
            dirty = true;
        }

        @Override
        public Id getReference(String field) {
            return this.<ReferenceValue>getValue(field).get();
        }

        public Id getReference(int index) {
            return this.<ReferenceValue>getValue(index).get();
        }

        @Override
        public ArrayList<String> getFields() {
            return collection.getFields();
        }

        @Override
        public String toString() {
            return ModelUtil.getDetailedDescription(this);
        }

        @Override
        public boolean isDirty() {
            return dirty;
        }

        public void resetDirty() {
            dirty = false;
        }
    }
    
}

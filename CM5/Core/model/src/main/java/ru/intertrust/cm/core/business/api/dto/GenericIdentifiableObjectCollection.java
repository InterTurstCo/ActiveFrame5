package ru.intertrust.cm.core.business.api.dto;

import ru.intertrust.cm.core.business.api.util.ModelUtil;

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

    private ArrayList<IdentifiableObject> list = new ArrayList<>();
    private HashMap<String, Integer> fieldIndexes = new HashMap<>();
    private ArrayList<String> fields;

    @Override
    public void setFields(List<String> fields) {
        if (this.fields != null) {
            throw new IllegalArgumentException("Collection fields are already set");
        }
        if (fields == null) {
            this.fields = new ArrayList<>();
        } else {
            this.fields = new ArrayList<>(fields);
        }
        int fieldIndex = 0;
        for (String field : this.fields) {
            fieldIndexes.put(field, fieldIndex);
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
        return fields == null ? new ArrayList<String>() : new ArrayList<>(fields);
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
        for (String field : getFields()) {

        }
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

    private IdentifiableObject createObjectByTemplate() {
        if (fields == null) {
            setFields(Collections.<String>emptyList());
        }
        return new FastIdentifiableObjectImpl(fields.size());
    }

    /**
     * Имплементация, позволяющая получить быстрый доступ к значениям полей по индексу
     */
    private class FastIdentifiableObjectImpl implements IdentifiableObject {
        private Id id;
        private ArrayList<Value> fieldValues;

        private FastIdentifiableObjectImpl(int fieldsSize) {
            fieldValues = new ArrayList<>(fieldsSize);
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
        }

        @Override
        public void setValue(String field, Value value) {
            fieldValues.set(getFieldIndex(field), value);
        }

        @Override
        public <T extends Value> T getValue(String field) {
            return (T) fieldValues.get(getFieldIndex(field));
        }

        public void setValue(int index, Value value) {
            fieldValues.set(index, value);
        }

        public <T extends Value> T getValue(int index) {
            return (T) fieldValues.get(index);
        }

        @Override
        public void setString(String field, String value) {
            fieldValues.set(getFieldIndex(field), new StringValue(value));
        }

        public void setString(int index, String value) {
            fieldValues.set(index, new StringValue(value));
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
            fieldValues.set(getFieldIndex(field), new LongValue(value));
        }

        public void setLong(int index, Long value) {
            fieldValues.set(index, new LongValue(value));
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
            fieldValues.set(getFieldIndex(field), new BooleanValue(value));
        }

        public void setBoolean(int index, Boolean value) {
            fieldValues.set(index, new BooleanValue(value));
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
            fieldValues.set(getFieldIndex(field), new DecimalValue(value));
        }

        public void setDecimal(int index, BigDecimal value) {
            fieldValues.set(index, new DecimalValue(value));
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
            fieldValues.set(getFieldIndex(field), new TimestampValue(value));
        }

        public void setTimestamp(int index, Date value) {
            fieldValues.set(index, new TimestampValue(value));
        }

        @Override
        public Date getTimestamp(String field) {
            return this.<TimestampValue>getValue(field).get();
        }

        public Date getTimestamp(int index) {
            return this.<TimestampValue>getValue(index).get();
        }

        @Override
        public void setReference(String field, DomainObject domainObject) {
            fieldValues.set(getFieldIndex(field), new ReferenceValue(domainObject.getId()));
        }

        public void setReference(int index, DomainObject domainObject) {
            fieldValues.set(index, new ReferenceValue(domainObject.getId()));
        }

        @Override
        public void setReference(String field, Id id) {
            fieldValues.set(getFieldIndex(field), new ReferenceValue(id));
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
            return GenericIdentifiableObjectCollection.this.getFields();
        }

        @Override
        public String toString() {
            return ModelUtil.getDetailedDescription(this);
        }
    }
}

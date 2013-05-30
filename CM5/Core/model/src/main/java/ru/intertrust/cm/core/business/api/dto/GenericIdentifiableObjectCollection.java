package ru.intertrust.cm.core.business.api.dto;

import ru.intertrust.cm.core.business.api.util.ModelUtil;

import java.math.BigDecimal;
import java.util.*;

/**
 * Коллекция объектов, наделённых идентификатором
 *
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
        public Value getValue(String field) {
            return fieldValues.get(getFieldIndex(field));
        }

        public void setValue(int index, Value value) {
            fieldValues.set(index, value);
        }

        public Value getValue(int index) {
            return fieldValues.get(index);
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

    public static void main(String[] args) {
        //todo move to unit tests
        IdentifiableObjectCollection collection = new GenericIdentifiableObjectCollection();
        ArrayList<String> fields = new ArrayList<>();
        fields.add("A");
        fields.add("B");
        fields.add("C");
        collection.setFields(fields);

        collection.setId(0, new RdbmsId("bo", 1923));
        collection.set(0, 5, new IntegerValue(5));
        collection.set(2, 7, new DecimalValue(new BigDecimal(3.14)));

        System.out.println(collection.get(0));
        System.out.println(collection.get(0, 5));
        System.out.println(collection);
    }
}

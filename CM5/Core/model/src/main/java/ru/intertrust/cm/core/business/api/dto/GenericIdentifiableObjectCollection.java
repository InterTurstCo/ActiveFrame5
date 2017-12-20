package ru.intertrust.cm.core.business.api.dto;

import ru.intertrust.cm.core.business.api.util.ModelUtil;
import ru.intertrust.cm.core.business.api.util.ObjectCloner;
import ru.intertrust.cm.core.config.FieldConfig;
import ru.intertrust.cm.core.model.GwtIncompatible;

import java.math.BigDecimal;
import java.util.*;

/**
 * Коллекция объектов, наделённых идентификатором
 * <p/>
 * Author: Denis Mitavskiy
 * Date: 23.05.13
 * Time: 10:54
 */
public class GenericIdentifiableObjectCollection implements IdentifiableObjectCollection, Cloneable {

    private ArrayList<FastIdentifiableObjectImpl> list = new ArrayList<>();
    private CaseInsensitiveMap<Integer> fieldIndexes = new CaseInsensitiveMap<>();
    private ArrayList<FieldConfig> fieldConfigs;

    public GenericIdentifiableObjectCollection() {
    }

    public void setFields(List<String> fields) {

    }

    @Override
    public void setFieldsConfiguration(List<FieldConfig> fieldConfigs) {
        // todo: это было сделано для того, чтобы в коллекцию можно было добавить новые поля.
        // вот только если порядок полей перестанет совпадать с предыдущим, коллекция перестанет "работать"
        // Надо расширить интерфейс
        // IdentifiableObjectCollection для того, чтобы можно было добавлять новые поля (см. CMFIVE-386)

        /*if (this.fieldConfigs != null) {
            throw new IllegalArgumentException("Collection field configs are already set");
        }*/
        if (fieldConfigs == null) {
            this.fieldConfigs = new ArrayList<>(0);
            return;
        }

        this.fieldConfigs = new ArrayList<>(fieldConfigs);
        int fieldIndex = 0;
        for (FieldConfig field : this.fieldConfigs) {
            fieldIndexes.put(Case.toLower(field.getName()), fieldIndex);
            ++fieldIndex;
        }
    }

    public boolean containsField(String fieldName) { // todo: not used and incorrect: fieldConfigs doesn't contain Strings
        if (fieldConfigs.contains(fieldName)) {
            return true;
        }
        throw new IllegalArgumentException("Field: " + fieldName + " does not exist in collection view configuration");
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
        list.get(row).setValue(fieldIndex, value);
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
        return list.get(row).getValue(fieldIndex);
    }

    public void sort(SortOrder sortOrder) {
        if(sortOrder != null){
            Collections.sort(list, new FastIdentifiableObjectComparator(/*this,*/ sortOrder));
        }
    }

    @Override
    public void append(IdentifiableObjectCollection collection) {
        if (collection == null || collection.size() == 0) {
            return;
        }
        ArrayList<FieldConfig> fieldConfigs = this.fieldConfigs == null ? new ArrayList<FieldConfig>() : this.fieldConfigs;
        ArrayList<FieldConfig> newFieldConfiguration = new ArrayList<>(fieldConfigs);
        final ArrayList<FieldConfig> collectionFields = collection.getFieldsConfiguration();
        int[] addedCollectionFieldIndexesInThisCollection = new int[collectionFields.size()];
        for (int i = 0; i < collectionFields.size(); i++) {
            FieldConfig fieldConfig = collectionFields.get(i);
            int thisCollectionIndex = getFieldIndex(fieldConfig.getName());
            if (thisCollectionIndex == -1) {
                newFieldConfiguration.add(fieldConfig);
                thisCollectionIndex = newFieldConfiguration.size() - 1;
            }
            addedCollectionFieldIndexesInThisCollection[i] = thisCollectionIndex;
        }

        setFieldsConfiguration(newFieldConfiguration);

        final int curSize = size();
        final int newSize = curSize + collection.size();
        addRowsIfNeeded(newSize - 1);

        int thisCollectionRow = curSize;
        for (int row = 0; row < collection.size(); ++row) {
            this.setId(thisCollectionRow, collection.getId(row));
            for (int col = 0; col < collectionFields.size(); ++col) {
                this.set(addedCollectionFieldIndexesInThisCollection[col], thisCollectionRow, collection.get(col, row));
            }
            ++thisCollectionRow;
        }
    }

    @Override
    public int getFieldIndex(String field) {
        final Integer index = fieldIndexes.get(field);
        return index == null ? -1 : index;
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
        return (ListIterator<IdentifiableObject>) (ListIterator) list.listIterator();
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
        (/*(FastIdentifiableObjectImpl)*/ list.get(row)).resetDirty();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !obj.getClass().equals(this.getClass())) {
            return false;
        }
        final GenericIdentifiableObjectCollection objCollection = (GenericIdentifiableObjectCollection) obj;
        return new HashSet<>(list).equals(new HashSet<>(objCollection.list));
    }

    @Override
    @GwtIncompatible
    public Object clone() throws CloneNotSupportedException {
        final GenericIdentifiableObjectCollection clone = (GenericIdentifiableObjectCollection) super.clone();
        ArrayList<FastIdentifiableObjectImpl> listClone = new ArrayList<>(list.size());
        for (FastIdentifiableObjectImpl fastIdentifiableObject : clone.list) {
            listClone.add((FastIdentifiableObjectImpl) fastIdentifiableObject.clone());
        }

        clone.list = listClone;
        clone.fieldIndexes = (CaseInsensitiveMap<Integer>) fieldIndexes.clone();
        clone.fieldConfigs = ObjectCloner.getInstance().cloneObject(fieldConfigs);
        return clone;
    }

    private FastIdentifiableObjectImpl createObjectByTemplate() {
        if (fieldConfigs == null) {
            setFieldsConfiguration(new ArrayList<FieldConfig>(0));
        }
        return new FastIdentifiableObjectImpl(/*this*/);
    }

    /**
     * Имплементация, позволяющая получить быстрый доступ к значениям полей по индексу
     */
    /*static*/ class FastIdentifiableObjectImpl implements IdentifiableObject, Cloneable {
        private Id id;
        private ArrayList<Value> fieldValues;
        //private IdentifiableObjectCollection collection;
        private boolean dirty = false;

        /*FastIdentifiableObjectImpl() {
        }*/

        private FastIdentifiableObjectImpl(/*IdentifiableObjectCollection collection*/) {
            //this.collection = collection;
            final List<FieldConfig> fieldsConfiguration = /*collection.*/getFieldsConfiguration();
            int fieldsSize = fieldsConfiguration.size();
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
            dirty = true;
        }

        @Override
        public void setValue(String field, Value value) {
            // todo: это было сделано для того, чтобы в коллекцию можно было добавить новые поля. Надо расширить интерфейс
            // IdentifiableObjectCollection для того, чтобы можно было добавлять новые поля (см. CMFIVE-386)
            // Identifiable Objects, возвращаемые из коллекции должны стать IMMUTABLE!
            final int fieldIndex = /*collection.*/getFieldIndex(field);
            if (fieldIndex < 0) {
                throw new IllegalArgumentException("Field " + field + " is not presented in collection");
            }
            addEmptyValuesIfNeeded(fieldIndex + 1);
            fieldValues.set(fieldIndex, value);
            dirty = true;
        }

        private void addEmptyValuesIfNeeded(int size) {
            if (fieldValues.size() < size){
                fieldValues.ensureCapacity(size);
                for (int i = 0; i < size - fieldValues.size(); ++i) {
                    fieldValues.add(null);
                }
            }
        }

        @Override
        public <T extends Value> T getValue(String field) {
            // Behavior should be such (for now), that non-existing field should give null-result
            final int fieldIndex = /*collection.*/getFieldIndex(field);
            return fieldIndex != -1 && fieldIndex < fieldValues.size() ? (T) fieldValues.get(fieldIndex) : null;
        }

        public void setValue(int index, Value value) {
            if (index >= fieldValues.size() && index < fieldConfigs.size()) {
                addEmptyValuesIfNeeded(index + 1);
            }
            fieldValues.set(index, value);
            dirty = true;
        }

        public <T extends Value> T getValue(int index) {
            if (index < fieldValues.size()) { // this condition also provides throwing exception when index < 0
                return (T) fieldValues.get(index);
            }
            final int fieldsSize = /*collection.*/getFieldsConfiguration().size();
            if (index < fieldsSize) { // field exists in collection, but not in this array
                return null;
            }
            throw new IndexOutOfBoundsException("No field with index: " + index);
        }

        @Override
        public void setString(String field, String value) {
            setValue(field, new StringValue(value));
            //dirty = true;
        }

        public void setString(int index, String value) {
            setValue(index, new StringValue(value));
            //dirty = true;
        }

        @Override
        public String getString(String field) {
            final StringValue value = this.getValue(field);
            return value == null ? null : value.get();
        }

        public String getString(int index) {
            final StringValue value = this.getValue(index);
            return value == null ? null : value.get();
        }


        @Override
        public void setLong(String field, Long value) {
            setValue(field, new LongValue(value));
            //dirty = true;
        }

        public void setLong(int index, Long value) {
            setValue(index, new LongValue(value));
            //dirty = true;
        }

        @Override
        public Long getLong(String field) {
            final LongValue value = this.getValue(field);
            return value == null ? null : value.get();
        }

        public Long getLong(int index) {
            final LongValue value = this.getValue(index);
            return value == null ? null : value.get();
        }

        @Override
        public void setBoolean(String field, Boolean value) {
            setValue(field, new BooleanValue(value));
            //dirty = true;
        }

        public void setBoolean(int index, Boolean value) {
            setValue(index, new BooleanValue(value));
            //dirty = true;
        }

        @Override
        public Boolean getBoolean(String field) {
            final BooleanValue value = this.getValue(field);
            return value == null ? null : value.get();
        }

        public Boolean getBoolean(int index) {
            final BooleanValue value = this.getValue(index);
            return value == null ? null : value.get();
        }

        @Override
        public void setDecimal(String field, BigDecimal value) {
            setValue(field, new DecimalValue(value));
            //dirty = true;
        }

        public void setDecimal(int index, BigDecimal value) {
            setValue(index, new DecimalValue(value));
            //dirty = true;
        }

        @Override
        public BigDecimal getDecimal(String field) {
            final DecimalValue value = this.getValue(field);
            return value == null ? null : value.get();
        }

        public BigDecimal getDecimal(int index) {
            final DecimalValue value = this.getValue(index);
            return value == null ? null : value.get();
        }

        @Override
        public void setTimestamp(String field, Date value) {
            setValue(field, new DateTimeValue(value));
            //dirty = true;
        }

        public void setTimestamp(int index, Date value) {
            setValue(index, new DateTimeValue(value));
            //dirty = true;
        }

        @Override
        public Date getTimestamp(String field) {
            final DateTimeValue value = this.getValue(field);
            return value == null ? null : value.get();
        }

        @Override
        public void setTimelessDate(String field, TimelessDate value) {
            setValue(field, new TimelessDateValue(value));
            //dirty = true;
        }

        public void setTimelessDate(int index, TimelessDate value) {
            setValue(index, new TimelessDateValue(value));
            //dirty = true;
        }

        @Override
        public TimelessDate getTimelessDate(String field) {
            final TimelessDateValue value = this.getValue(field);
            return value == null ? null : value.get();
        }

        public Date getTimestamp(int index) {
            final DateTimeValue value = this.getValue(index);
            return value == null ? null : value.get();
        }

        @Override
        public void setDateTimeWithTimeZone(String field, DateTimeWithTimeZone value) {
            setValue(field, new DateTimeWithTimeZoneValue(value));
            //dirty = true;
        }

        public void setDateTimeWithTimeZone(int index, DateTimeWithTimeZone value) {
            setValue(index, new DateTimeWithTimeZoneValue(value));
            //dirty = true;
        }

        @Override
        public DateTimeWithTimeZone getDateTimeWithTimeZone(String field) {
            final DateTimeWithTimeZoneValue value = this.getValue(field);
            return value == null ? null : value.get();
        }

        public DateTimeWithTimeZone getDateTimeWithTimeZone(int index) {
            final DateTimeWithTimeZoneValue value = this.getValue(index);
            return value == null ? null : value.get();
        }

        @Override
        public void setReference(String field, DomainObject domainObject) {
            setValue(field, new ReferenceValue(domainObject.getId()));
            //dirty = true;
        }

        public void setReference(int index, DomainObject domainObject) {
            setValue(index, new ReferenceValue(domainObject.getId()));
            //dirty = true;
        }

        @Override
        public void setReference(String field, Id id) {
            setValue(field, new ReferenceValue(id));
            //dirty = true;
        }

        @Override
        public Id getReference(String field) {
            final ReferenceValue value = this.getValue(field);
            return value == null ? null : value.get();
        }

        public Id getReference(int index) {
            final ReferenceValue value = this.getValue(index);
            return value == null ? null : value.get();
        }

        @Override
        public ArrayList<String> getFields() {
            final ArrayList<FieldConfig> fieldsConfiguration = /*collection.*/getFieldsConfiguration();
            final ArrayList<String> result = new ArrayList<>(fieldsConfiguration.size());
            for (FieldConfig config : fieldsConfiguration) {
                result.add(config.getName());
            }            
            return result;
        }

        @Override
        @GwtIncompatible
        public Object clone() {
            try {
                final FastIdentifiableObjectImpl clone = (FastIdentifiableObjectImpl) super.clone();
                ArrayList<Value> clonedFieldValues = (ArrayList<Value>) fieldValues.clone();
                final ObjectCloner cloner = ObjectCloner.getInstance();
                for (int i = 0; i < clonedFieldValues.size(); i++) {
                    Value fieldValue = clonedFieldValues.get(i);
                    if (fieldValue != null && !fieldValue.isImmutable()) {
                        clonedFieldValues.set(i, cloner.cloneObject(fieldValue));
                    }
                }
                clone.fieldValues = clonedFieldValues;
            } catch (CloneNotSupportedException e) { // impossible
            }
            return this;
        }

        @Override
        public String toString() {
            return ModelUtil.getDetailedDescription(this);
        }

        @Override
        public boolean isDirty() {
            return dirty;
        }

        @Override
        public boolean containsFieldValues(Map<String, Value> fieldValues) {
            return GenericIdentifiableObject.containsFieldValues(this, fieldValues);
        }

        public void resetDirty() {
            dirty = false;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null || !obj.getClass().equals(this.getClass())) {
                return false;
            }
            final FastIdentifiableObjectImpl fio = (FastIdentifiableObjectImpl) obj;
            final ArrayList<String> fields = getFields();
            if (!Objects.equals(id, fio.id) || !fields.equals(fio.getFields())) {
                 return false;
            }
            for (int i = 0; i < fields.size(); i++) {
                if (!Objects.equals(getValue(i), fio.getValue(i))) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public int hashCode() {
            return id != null ? id.hashCode() : fieldValues.hashCode();
        }
    }

    /*static*/ class FastIdentifiableObjectComparator implements Comparator<FastIdentifiableObjectImpl> {
        private ArrayList<SortCriterionComparator> comparators;

        FastIdentifiableObjectComparator(/*GenericIdentifiableObjectCollection collection,*/ SortOrder sortOrder) {
            comparators = new ArrayList<>(sortOrder.size());
            for (SortCriterion criterion : sortOrder) {
                final boolean asc = criterion.getOrder() == SortCriterion.Order.ASCENDING;
                final Comparator<Value> comparator = Value.getComparator(asc, true);
                final int fieldIndex = /*collection.*/getFieldIndex(criterion.getField());
                comparators.add(new SortCriterionComparator(fieldIndex, comparator));
            }
        }

        @Override
        public int compare(FastIdentifiableObjectImpl o1, FastIdentifiableObjectImpl o2) {
            for (SortCriterionComparator comparator : comparators) {
                final int comparisonResult = comparator.compare(o1, o2);
                if (comparisonResult != 0) {
                    return comparisonResult;
                }
            }
            return 0;
        }
    }

    private static class SortCriterionComparator {
        private final int fieldIndex;
        private final Comparator<Value> comparator;

        private SortCriterionComparator(int fieldIndex, Comparator<Value> comparator) {
            this.fieldIndex = fieldIndex;
            this.comparator = comparator;
        }

        public int compare(FastIdentifiableObjectImpl o1, FastIdentifiableObjectImpl o2) {
            return comparator.compare(o1.getValue(fieldIndex), o2.getValue(fieldIndex));
        }
    }
}

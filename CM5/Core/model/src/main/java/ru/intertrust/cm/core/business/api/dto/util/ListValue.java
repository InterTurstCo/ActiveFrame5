package ru.intertrust.cm.core.business.api.dto.util;

import static java.util.Arrays.asList;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import ru.intertrust.cm.core.business.api.dto.Value;

/**
 * Хранение списка однотипных значений для передачи в SQL in () выражение. Не
 * отображается на поля доменного объекта! Используется для удобства передачи
 * параметров при поиске коллекций (@see
 * {@link ru.intertrust.cm.core.dao.impl.CollectionsDaoImpl#findCollectionByQuery(String, List, int, int, ru.intertrust.cm.core.dao.access.AccessToken)}
 * )
 * @author atsvetkov
 */
public class ListValue extends Value<ListValue> {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public static ListValue createListValue(List<? extends Value<?>> values) {
        ListValue listValue = new ListValue();
        listValue.values = new ArrayList(values);
        return listValue;
    }

    private ArrayList<Value> values;
    private transient List<Serializable> primitives;

    public ListValue() {
        this.values = new ArrayList<>();
    }

    /**
     * Преобразует список {@link Value} в список типов, поддерживаемых in()
     * выражением
     * @param values
     *            целочисленное значение
     * @deprecated Используйте {@link #createListValue(List)}
     */
    @Deprecated
    public ListValue(List<Value> values) {
        ArrayList<Value<?>> t = new ArrayList<Value<?>>();
        for (Value<?> value : values) {
            t.add(value);
        }
        this.values = new ArrayList(values);
    }

    public ListValue(Value<?>... values) {
        this.values = new ArrayList(asList(values));
    }

    @Override
    public List<Serializable> get() {
        if (values == null) {
            return null;
        }
        if (primitives == null) {
            primitives = getPrimitiveValues(new ArrayList(values));
        }
        return primitives;
    }

    private List<Serializable> getPrimitiveValues(List<Value<?>> values) {
        if (values == null || values.size() <= 0) {
            return null;
        }
        List<Serializable> resultValues = new ArrayList<>();
        for (Value<?> value : values) {
            resultValues.add((Serializable) value.get());
        }
        return resultValues;
    }

    /**
     * @deprecated Используйте {@link #getUnmodifiableValuesList()}.
     * @return
     */
    @Deprecated
    public ArrayList<Value> getValues() {
        return values;
    }

    public List<Value<?>> getUnmodifiableValuesList() {
        ArrayList<Value<?>> t = new ArrayList<Value<?>>();
        for (Value<?> v : values) {
            t.add(v);
        }
        return t;
    }

    @Override
    public int hashCode() {
        int result = 1;
        result = 31 * result + ((values == null) ? 0 : values.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        ListValue other = (ListValue) obj;
        if (values == null) {
            if (other.values != null) {
                return false;
            }
        } else if (!values.equals(other.values)) {
            return false;
        }
        return true;
    }

}

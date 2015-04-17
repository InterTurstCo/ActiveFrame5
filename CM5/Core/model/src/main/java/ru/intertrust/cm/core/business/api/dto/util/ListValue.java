package ru.intertrust.cm.core.business.api.dto.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import ru.intertrust.cm.core.business.api.dto.Value;

/**
 * Хранение списка однотипных значений для передачи в SQL in () выражение. Не отображается на поля доменного объекта!
 * Используется для удобства передачи параметров при поиске коллекций (@see
 * {@link ru.intertrust.cm.core.dao.impl.CollectionsDaoImpl#findCollectionByQuery(String, List, int, int, ru.intertrust.cm.core.dao.access.AccessToken)}
 * )
 * @author atsvetkov
 */
public class ListValue extends Value<ListValue> {

    private ArrayList<Value> values;
    private transient List<Serializable> primitives;

    public ListValue() {
        this.values = new ArrayList<>();
    }

    /**
     * Преобразует список @link(Value) в список типов, поддерживаемых in() выражением
     * @param values целочичсленное значение
     */
    public ListValue(List<Value> values) {
        this.values = new ArrayList<>(values);
    }

    @Override
    public List<Serializable> get() {
        if (values == null) {
            return null;
        }
        if (primitives == null) {
            primitives = getPrimitiveValues(values);
        }
        return primitives;
    }

    private List<Serializable> getPrimitiveValues(List<Value> values) {
        if (values == null || values.size() <= 0) {
            return null;
        }
        List<Serializable> resultValues = new ArrayList<>();
        for (Value value : values) {
            resultValues.add((Serializable) value.get());
        }
        return resultValues;
    }

    public ArrayList<Value> getValues() {
        return values;
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

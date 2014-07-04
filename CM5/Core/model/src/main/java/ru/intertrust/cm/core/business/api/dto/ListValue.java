package ru.intertrust.cm.core.business.api.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Хранение списка однотипных значений для передачи в SQL in () выражение.
 * @author atsvetkov
 */
public class ListValue extends Value<ListValue> {

    private List<Serializable> values;

    public ListValue() {
        this.values = new ArrayList<>();
    }

    /**
     * Преобразует список @link(Value) в список типов, поддерживаемых in() выражением
     * @param values целочичсленное значение
     */
    public ListValue(List<Value> values) {
        this.values = getPrimitiveValues(values);
    }

    @Override
    public List<Serializable> get() {
        return values;
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
}

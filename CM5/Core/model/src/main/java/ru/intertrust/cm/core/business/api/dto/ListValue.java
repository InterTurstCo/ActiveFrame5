package ru.intertrust.cm.core.business.api.dto;

import java.util.ArrayList;
import java.util.List;

import ru.intertrust.cm.core.business.api.dto.impl.RdbmsId;

/**
 * Хранение списка однотипных значений для передачи в SQL in () выражение.
 * @author atsvetkov
 */
public class ListValue extends Value<ListValue> {

    private List values;

    public ListValue() {
        this.values = new ArrayList();
    }

    /**
     * Преобразует список @link(Value) в список типов, поддерживаемых in() выражением
     * @param values целочичсленное значение
     */
    public ListValue(List<Value> values) {
        this.values = copyValues(values);
    }

    private List copyValues(List<Value> values) {
        List resultValues = new ArrayList();
        if (values != null && values.size() > 0) {
            for (Value value : values) {
                if (value instanceof DateTimeWithTimeZoneValue || value instanceof TimelessDateValue) {
                    throw new IllegalArgumentException("Value of type " + value.getClass().getName()
                            + " is not supported for ListValue");

                } else if (value instanceof ReferenceValue) {
                    resultValues.add(((RdbmsId) value.get()).getId());
                } else {
                    resultValues.add(value.get());
                }
            }
        }
        return resultValues;
    }

    @Override
    public List get() {
        return values;
    }
}

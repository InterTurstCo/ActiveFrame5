package ru.intertrust.cm.core.business.api.dto;

/**
 * Значение поля доменного объекта, определяющее дату без времени время в виде календаря.
 * @author vmatsukevich
 *         Date: 10/24/13
 *         Time: 3:29 PM
 */
public class TimelessDateValue extends Value<TimelessDateValue> {
    private TimelessDate value;

    /**
     * Создает пустое булево значение
     */
    public TimelessDateValue() {
    }

    /**
     * Создает булево значение
     * @param value булево значение
     */
    public TimelessDateValue(TimelessDate value) {
        this.value = value;
    }

    @Override
    public TimelessDate get() {
        return value;
    }

    @Override
    public int compareTo(TimelessDateValue o) {
        if (o == null || o.isEmpty()) {
            return this.isEmpty() ? 0 : 1;
        } else {
            return this.isEmpty() ? -1 : value.compareTo(o.value);
        }
    }

    public TimelessDate getValue() {
        return value;
    }

    public void setValue(TimelessDate value) {
        this.value = value;
    }
}
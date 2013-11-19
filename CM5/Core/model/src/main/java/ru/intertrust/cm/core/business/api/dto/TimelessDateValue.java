package ru.intertrust.cm.core.business.api.dto;

/**
 * Значение поля доменного объекта, определяющее дату без времени время в виде календаря.
 * @author vmatsukevich
 *         Date: 10/24/13
 *         Time: 3:29 PM
 */
public class TimelessDateValue extends Value {
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

    public TimelessDate getValue() {
        return value;
    }

    public void setValue(TimelessDate value) {
        this.value = value;
    }
}
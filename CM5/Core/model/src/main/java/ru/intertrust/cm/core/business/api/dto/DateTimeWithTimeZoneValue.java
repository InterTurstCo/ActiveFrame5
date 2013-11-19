package ru.intertrust.cm.core.business.api.dto;

/**
 * Значение поля доменного объекта, определяющее дату и время в виде календаря с часовым поясом
 * или временнЫм смещением.
 * @author vmatsukevich
 *         Date: 10/24/13
 *         Time: 3:29 PM
 */
public class DateTimeWithTimeZoneValue extends Value {
    private DateTimeWithTimeZone value;

    /**
     * Создает пустое булево значение
     */
    public DateTimeWithTimeZoneValue() {
    }

    /**
     * Создает булево значение
     * @param value булево значение
     */
    public DateTimeWithTimeZoneValue(DateTimeWithTimeZone value) {
        this.value = value;
    }

    @Override
    public DateTimeWithTimeZone get() {
        return value;
    }

    public DateTimeWithTimeZone getValue() {
        return value;
    }

    public void setValue(DateTimeWithTimeZone value) {
        this.value = value;
    }
}
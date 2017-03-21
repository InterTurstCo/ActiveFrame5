package ru.intertrust.cm.core.business.api.dto;

import java.util.Date;

/**
 * Значение поля доменного объекта, определяющее дату и время. Точность определяется Java-типом {@link java.util.Date}
 * Author: Denis Mitavskiy
 * Date: 19.05.13
 * Time: 16:20
 */
public class DateTimeValue extends Value<DateTimeValue> {
    private Date value;

    /**
     * Создаёт пустое значение поля доменного объекта, определяющее дату и время
     */
    public DateTimeValue() {
    }

    /**
     * Создаёт значение поля доменного объекта, определяющее дату и время
     * @param value значение
     */
    public DateTimeValue(Date value) {
        this.value = value;
    }

    @Override
    public Date get() {
        return value;
    }

    @Override
    public boolean isImmutable() {
        return true;
    }

    @Override
    public int compareTo(DateTimeValue o) {
        if (o == null || o.isEmpty()) {
            return this.isEmpty() ? 0 : 1;
        } else {
            return this.isEmpty() ? -1 : value.compareTo(o.value);
        }
    }

    @Override
    public final DateTimeValue getPlatformClone() {
        final Date value = get();
        if (this.getClass() != DateTimeValue.class) {
            return value == null ? new DateTimeValue() : new DateTimeValue(new Date(value.getTime()));
        } else if (value != null && value.getClass() != Date.class) {
            return new DateTimeValue(new Date(value.getTime()));
        } else {
            return this;
        }
    }
}

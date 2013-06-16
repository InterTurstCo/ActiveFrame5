package ru.intertrust.cm.core.business.api.dto;

import java.util.Date;

/**
 * Значение поля доменного объекта, определяющее дату и время. Точность определяется Java-типом {@link java.util.Date}
 * Author: Denis Mitavskiy
 * Date: 19.05.13
 * Time: 16:20
 */
public class TimestampValue extends Value {
    private Date value;

    /**
     * Создаёт пустое значение поля доменного объекта, определяющее дату и время
     */
    public TimestampValue() {
    }

    /**
     * Создаёт значение поля доменного объекта, определяющее дату и время
     * @param value значение
     */
    public TimestampValue(Date value) {
        this.value = value;
    }

    @Override
    public Date get() {
        return value;
    }

}

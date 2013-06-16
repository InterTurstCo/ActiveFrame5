package ru.intertrust.cm.core.business.api.dto;

/**
 * Строковое значение поля доменного объекта
 * Author: Denis Mitavskiy
 * Date: 19.05.13
 * Time: 16:20
 */
public class StringValue extends Value {
    private String value;

    /**
     * Создаёт пустое строковое значение
     */
    public StringValue() {
    }

    /**
     * Создаёт строковое значение
     * @param value строковое значение
     */
    public StringValue(String value) {
        this.value = value;
    }

    @Override
    public String get() {
        return value;
    }

    @Override
    public boolean isEmpty() {
        return value == null || "".equals(value);
    }
}

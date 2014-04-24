package ru.intertrust.cm.core.business.api.dto;

/**
 * Булево значение поля доменного объекта
 *
 * Author: Denis Mitavskiy
 * Date: 19.05.13
 * Time: 16:25
 */
public class BooleanValue extends Value<BooleanValue> {
    private Boolean value;

    /**
     * Создает пустое булево значение
     */
    public BooleanValue() {
    }

    /**
     * Создает булево значение
     * @param value булево значение
     */
    public BooleanValue(Boolean value) {
        this.value = value;
    }

    @Override
    public Boolean get() {
        return value;
    }

    @Override
    public int compareTo(BooleanValue o) {
        if (o == null || o.isEmpty()) {
            return this.isEmpty() ? 0 : 1;
        } else {
            return this.isEmpty() ? -1 : value.compareTo(o.value);
        }
    }
}

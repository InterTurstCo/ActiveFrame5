package ru.intertrust.cm.core.business.api.dto;

/**
 * Булево значение поля бизнес-объекта
 *
 * Author: Denis Mitavskiy
 * Date: 19.05.13
 * Time: 16:25
 */
public class BooleanValue extends Value {
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

}

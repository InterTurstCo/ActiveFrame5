package ru.intertrust.cm.core.business.api.dto;

/**
 * Целочисленное значение поля бизнес-объекта. Граничные значения определяются Java-типом {@link java.lang.Long}
 * Author: Denis Mitavskiy
 * Date: 19.05.13
 * Time: 16:21
 */
public class IntegerValue extends Value {
    private Long value;

    /**
     * Создаёт пустое целочичсленное значение
     */
    public IntegerValue() {
    }

    /**
     * Создаёт целочичсленное значение
     * @param value целочичсленное значение
     */
    public IntegerValue(Integer value) {
        this.value = value == null ? null : (long) value;
    }

    /**
     * Создаёт целочичсленное значение
     * @param value целочичсленное значение
     */
    public IntegerValue(Long value) {
        this.value = value;
    }

    @Override
    public Long get() {
        return value;
    }

}

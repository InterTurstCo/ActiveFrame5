package ru.intertrust.cm.core.business.api.dto;

/**
 * Целочисленное значение поля доменного объекта. Граничные значения определяются Java-типом {@link java.lang.Long}
 * Author: Denis Mitavskiy
 * Date: 19.05.13
 * Time: 16:21
 */
public class LongValue extends Value {
    private Long value;

    /**
     * Создаёт пустое целочичсленное значение
     */
    public LongValue() {
    }

    /**
     * Создаёт целочичсленное значение
     * @param value целочичсленное значение
     */
    public LongValue(Integer value) {
        this.value = value == null ? null : (long) value;
    }

    /**
     * Создаёт целочичсленное значение
     * @param value целочичсленное значение
     */
    public LongValue(Long value) {
        this.value = value;
    }

    @Override
    public Long get() {
        return value;
    }

}

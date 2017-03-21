package ru.intertrust.cm.core.business.api.dto;

/**
 * Целочисленное значение поля доменного объекта. Граничные значения определяются Java-типом {@link java.lang.Long}
 * Author: Denis Mitavskiy
 * Date: 19.05.13
 * Time: 16:21
 */
public class LongValue extends Value<LongValue> {
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

    @Override
    public boolean isImmutable() {
        return true;
    }

    @Override
    public int compareTo(LongValue o) {
        if (o == null || o.isEmpty()) {
            return this.isEmpty() ? 0 : 1;
        } else {
            return this.isEmpty() ? -1 : value.compareTo(o.value);
        }
    }

    @Override
    public final LongValue getPlatformClone() {
        if (this.getClass() != LongValue.class) {
            return new LongValue(get());
        } else {
            return this;
        }
    }

}

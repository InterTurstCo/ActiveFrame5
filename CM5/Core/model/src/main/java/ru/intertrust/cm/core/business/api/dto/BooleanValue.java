package ru.intertrust.cm.core.business.api.dto;

/**
 * Булево значение поля доменного объекта
 *
 * Author: Denis Mitavskiy
 * Date: 19.05.13
 * Time: 16:25
 */
public class BooleanValue extends Value<BooleanValue> {

    public static BooleanValue TRUE = new BooleanValue(Boolean.TRUE);
    public static BooleanValue FALSE = new BooleanValue(Boolean.FALSE);
    public static BooleanValue EMPTY = new BooleanValue();

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

    /**
     * Создает булево значение
     * @param value булево значение
     */
    public BooleanValue(boolean value) {
        this.value = value ? Boolean.TRUE : Boolean.FALSE;
    }

    @Override
    public Boolean get() {
        return value;
    }

    @Override
    public boolean isImmutable() {
        return true;
    }

    @Override
    public int compareTo(BooleanValue o) {
        if (o == null || o.isEmpty()) {
            return this.isEmpty() ? 0 : 1;
        } else {
            return this.isEmpty() ? -1 : value.compareTo(o.value);
        }
    }

    @Override
    public final BooleanValue getPlatformClone() {
        if (this.getClass() != BooleanValue.class) {
            return new BooleanValue(get());
        } else {
            return this;
        }
    }
}

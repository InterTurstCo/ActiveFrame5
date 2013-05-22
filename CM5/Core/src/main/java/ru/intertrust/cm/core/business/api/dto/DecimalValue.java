package ru.intertrust.cm.core.business.api.dto;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * Десятичное значение (произвольной точности) поля бизнес-объекта
 *
 * Author: Denis Mitavskiy
 * Date: 19.05.13
 * Time: 16:20
 */
public class DecimalValue extends Value {
    private BigDecimal value;

    /**
     * Создаёт пустое десятичное значение
     */
    public DecimalValue() {
    }

    /**
     * Создаёт десятичное значение
     * @param value десятичное значение
     */
    public DecimalValue(Integer value) {
        this.value = value == null ? null : BigDecimal.valueOf((long) value);
    }

    /**
     * Создаёт десятичное значение
     * @param value десятичное значение
     */
    public DecimalValue(Long value) {
        this.value = value == null ? null : BigDecimal.valueOf(value);
    }

    /**
     * Создаёт десятичное значение
     * @param value десятичное значение
     */
    public DecimalValue(BigInteger value) {
        this.value = value == null ? null : new BigDecimal(value);
    }

    /**
     * Создаёт десятичное значение
     * @param value десятичное значение
     */
    public DecimalValue(BigDecimal value) {
        this.value = value;
    }

    @Override
    public BigDecimal get() {
        return value;
    }

}

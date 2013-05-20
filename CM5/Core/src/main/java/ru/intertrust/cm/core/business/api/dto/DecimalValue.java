package ru.intertrust.cm.core.business.api.dto;

import java.math.BigDecimal;

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
    public DecimalValue(BigDecimal value) {
        this.value = value;
    }

    @Override
    public BigDecimal get() {
        return value;
    }

}

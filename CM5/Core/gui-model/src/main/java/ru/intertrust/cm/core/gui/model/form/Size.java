package ru.intertrust.cm.core.gui.model.form;

import ru.intertrust.cm.core.business.api.dto.Dto;

/**
 * Класс, определяющий размер элемента графического элемента пользователя. Помимо значения, определяет единцы измерения
 * этого значения.
 *
 * @author Denis Mitavskiy
 *         Date: 06.09.13
 *         Time: 12:37
 */
public class Size implements Dto {
    private int value;
    private Unit unit;

    /**
     * Конструктор по умолчанию
     */
    public Size() {
    }

    /**
     * Конструктор, определяющий величину и её единицу измерения
     * @param value значение
     * @param unit единица измерения
     */
    public Size(int value, Unit unit) {
        this.value = value;
        this.unit = unit;
    }

    /**
     * Возвращает значение величины размера
     * @return значение величины размера
     */
    public int getValue() {
        return value;
    }

    /**
     * Устанавливает значение величины размера
     * @param value значение величины размера
     */
    public void setValue(int value) {
        this.value = value;
    }

    /**
     * Возвращает значение единицы измерения размера
     * @return значение единицы измерения размера
     */
    public Unit getUnit() {
        return unit;
    }

    /**
     * Возвращает значение единицы измерения размера
     * @param unit значение единицы измерения размера
     */
    public void setUnit(Unit unit) {
        this.unit = unit;
    }
}

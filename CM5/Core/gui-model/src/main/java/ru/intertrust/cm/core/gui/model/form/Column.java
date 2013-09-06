package ru.intertrust.cm.core.gui.model.form;

import ru.intertrust.cm.core.business.api.dto.Dto;

/**
 * Класс определяет колонку в табличной разметке
 *
 * @author Denis Mitavskiy
 *         Date: 06.09.13
 *         Time: 12:33
 */
public class Column implements Dto {
    private Size width;
    private HorizontalAlignment defaultAlignment;

    /**
     * Возвращает ширину колонки
     * @return ширину колонки
     */
    public Size getWidth() {
        return width;
    }

    /**
     * Устанавливает ширину колонки
     * @param width ширину колонки
     */
    public void setWidth(Size width) {
        this.width = width;
    }

    /**
     * Возвращает выравнивание по умолчанию в данной колонке
     * @return выравнивание по умолчанию в данной колонке
     */
    public HorizontalAlignment getDefaultAlignment() {
        return defaultAlignment;
    }

    /**
     * Устанавливает выравнивание по умолчанию в данной колонке
     * @param defaultAlignment выравнивание по умолчанию в данной колонке
     */
    public void setDefaultAlignment(HorizontalAlignment defaultAlignment) {
        this.defaultAlignment = defaultAlignment;
    }
}

package ru.intertrust.cm.core.gui.model.form;

import ru.intertrust.cm.core.business.api.dto.Dto;

/**
 * Класс определяет строку в табличной разметке
 *
 * @author Denis Mitavskiy
 *         Date: 06.09.13
 *         Time: 12:33
 */
public class Row implements Dto {
    private Size height;
    private VerticalAlignment defaultAlignment;

    /**
     * Возвращает высоту строки
     * @return высоту строки
     */
    public Size getHeight() {
        return height;
    }

    /**
     * Устанавливает высоту строки
     * @param height высота строки
     */
    public void setHeight(Size height) {
        this.height = height;
    }

    /**
     * Возвращает выравнивание по умолчанию в данной строке
     * @return выравнивание по умолчанию в данной строке
     */
    public VerticalAlignment getDefaultAlignment() {
        return defaultAlignment;
    }

    /**
     * Устанавливает выравнивание по умолчанию в данной строке
     * @param defaultAlignment выравнивание по умолчанию в данной строке
     */
    public void setDefaultAlignment(VerticalAlignment defaultAlignment) {
        this.defaultAlignment = defaultAlignment;
    }
}

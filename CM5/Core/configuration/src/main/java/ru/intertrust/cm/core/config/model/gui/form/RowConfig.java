package ru.intertrust.cm.core.config.model.gui.form;

import ru.intertrust.cm.core.business.api.dto.Dto;

import java.util.List;

/**
 * Класс определяет строку в табличной разметке
 *
 * @author Denis Mitavskiy
 *         Date: 06.09.13
 *         Time: 12:33
 */
public class RowConfig implements Dto {
    private String height;
    private String defaultVerticalAlignment;
    private List<CellConfig> cells;

    /**
     * Возвращает высоту строки
     * @return высоту строки
     */
    public String getHeight() {
        return height;
    }

    /**
     * Устанавливает высоту строки
     * @param height высота строки
     */
    public void setHeight(String height) {
        this.height = height;
    }

    /**
     * Возвращает выравнивание по умолчанию в данной строке
     * @return выравнивание по умолчанию в данной строке
     */
    public String getDefaultVerticalAlignment() {
        return defaultVerticalAlignment;
    }

    /**
     * Устанавливает выравнивание по умолчанию в данной строке
     * @param defaultVerticalAlignment выравнивание по умолчанию в данной строке
     */
    public void setDefaultVerticalAlignment(String defaultVerticalAlignment) {
        this.defaultVerticalAlignment = defaultVerticalAlignment;
    }

    public List<CellConfig> getCells() {
        return cells;
    }

    public void setCells(List<CellConfig> cells) {
        this.cells = cells;
    }
}

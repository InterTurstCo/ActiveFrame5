package ru.intertrust.cm.core.gui.model.form;

import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.gui.model.form.widget.WidgetDefinition;

/**
 * Класс определяет ячейку в табличной разметке
 *
 * @author Denis Mitavskiy
 *         Date: 06.09.13
 *         Time: 13:09
 */
public class Cell implements Dto {
    private int columnSpan;
    private int rowSpan;
    private HorizontalAlignment horizontalAlignment;
    private VerticalAlignment verticalAlignment;
    private WidgetDefinition widgetDefinition;

    /**
     * Конструктор по умолчанию. Ячейка размахом 1 позицию по горизонтали и вертикали, выравнивание элементов в ячейке
     * по левому краю и верхнему краю.
     */
    public Cell() {
        this(1, 1, HorizontalAlignment.Left, VerticalAlignment.Top);
    }

    /**
     * Конструктор ячейки, размах которой 1 по горизонтали и вертикали
     *
     * @param hAlignment горизонтальное выравнивание
     * @param vAlignment вертикальное выравнивание
     */
    public Cell(HorizontalAlignment hAlignment, VerticalAlignment vAlignment) {
        this(1, 1, hAlignment, vAlignment);
    }

    /**
     * Конструктор ячейки
     * @param colSpan размах ячейки по горизонтали
     * @param rowSpan размах ячейки по вертикали
     * @param hAlignment горизонтальное выравнивание
     * @param vAlignment вертикальное выравнивание
     */
    public Cell(int colSpan, int rowSpan, HorizontalAlignment hAlignment, VerticalAlignment vAlignment) {
        this.columnSpan = colSpan;
        this.rowSpan = rowSpan;
        this.horizontalAlignment = hAlignment;
        this.verticalAlignment = vAlignment;
    }

    /**
     * Возвращает горизонтальное выравнивание ячейки
     * @return горизонтальное выравнивание ячейки
     */
    public HorizontalAlignment getHorizontalAlignment() {
        return horizontalAlignment;
    }

    /**
     * Устанавливает горизонтальное выравнивание ячейки
     * @param horizontalAlignment горизонтальное выравнивание ячейки
     */
    public void setHorizontalAlignment(HorizontalAlignment horizontalAlignment) {
        this.horizontalAlignment = horizontalAlignment;
    }

    /**
     * Устанавливает вертикальное выравнивание ячейки
     * @return вертикальное выравнивание ячейки
     */
    public VerticalAlignment getVerticalAlignment() {
        return verticalAlignment;
    }

    /**
     * Уставнавливает вертикальное выравнивание ячейки
     * @param verticalAlignment вертикальное выравнивание ячейки
     */
    public void setVerticalAlignment(VerticalAlignment verticalAlignment) {
        this.verticalAlignment = verticalAlignment;
    }

    /**
     * Возвращает размах ячейки (количество колонок, занимаемых ячейкой) по горизонтали
     * @return размах ячейки (количество колонок, занимаемых ячейкой) по горизонтали
     */
    public int getColumnSpan() {
        return columnSpan;
    }

    /**
     * Устанавливает размах ячейки (количество колонок, занимаемых ячейкой) по горизонтали
     *
     * @param columnSpan размах ячейки (количество колонок, занимаемых ячейкой) по горизонтали
     */
    public void setColumnSpan(int columnSpan) {
        this.columnSpan = columnSpan;
    }

    /**
     * Возвращает размах ячейки (количество строк, занимаемых ячейкой) по вертикали
     * @return размах ячейки (количество строк, занимаемых ячейкой) по вертикали
     */
    public int getRowSpan() {
        return rowSpan;
    }

    /**
     * Устанавливает размах ячейки (количество строк, занимаемых ячейкой) по вертикали
     * @param rowSpan размах ячейки (количество строк, занимаемых ячейкой) по вертикали
     */
    public void setRowSpan(int rowSpan) {
        this.rowSpan = rowSpan;
    }
}

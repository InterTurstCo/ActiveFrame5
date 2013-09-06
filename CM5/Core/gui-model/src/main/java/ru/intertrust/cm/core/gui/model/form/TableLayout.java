package ru.intertrust.cm.core.gui.model.form;

import java.util.List;

/**
 * Табличная разметка. Позволяет отобразить элементы пользовательского интерфейса в ячейках таблицы, находящихся в
 * строка и столбцах.
 *
 * @author Denis Mitavskiy
 *         Date: 05.09.13
 *         Time: 17:49
 */
public class TableLayout implements Layout {
    private List<Row> rows;
    private List<Column> columns;
    private List<List<Cell>> cells;

    /**
     * Конструктор по умолчанию
     */
    public TableLayout() {
    }

    /**
     * Устанавливает определение разметки
     * @param rows строки в порядке следования сверху вниз
     * @param columns колонки в порядке следования слева направо
     * @param cells ячейки. cells.get(3).get(5) возвращает ячейку с индексом 3 по вертикали (4я строка) и индеком 5 по
     *              горизонтали (6я колонка)
     */
    public void define(List<Row> rows, List<Column> columns, List<List<Cell>> cells) {
        this.rows = rows;
        this.columns = columns;
        this.cells = cells;
    }

    /**
     * Возвращает строки табличной разметки
     * @return строки табличной разметки
     */
    public List<Row> getRows() {
        return rows;
    }

    /**
     * Устанавливает строки табличной разметки
     * @param rows строки табличной разметки
     */
    public void setRows(List<Row> rows) {
        this.rows = rows;
    }

    /**
     * Возвращает колонки табличной разметки
     * @return колонки табличной разметки
     */
    public List<Column> getColumns() {
        return columns;
    }

    /**
     * Устанавливает колонки табличной разметки
     * @param columns колонки табличной разметки
     */
    public void setColumns(List<Column> columns) {
        this.columns = columns;
    }

    /**
     * Возвращает ячейки табличной разметки. list.get(3).get(5) возвращает ячейку с индексом 3 по вертикали (4я строка)
     * и индеком 5 по горизонтали (6я колонка)
     * @return ячейки табличной разметки
     */
    public List<List<Cell>> getCells() {
        return cells;
    }

    /**
     * Устанавливает ячейки табличной разметки. list.get(3).get(5) возвращает ячейку с индексом 3 по вертикали
     * (4я строка) и индеком 5 по горизонтали (6я колонка)
     * @param cells ячейки табличной разметки
     */
    public void setCells(List<List<Cell>> cells) {
        this.cells = cells;
    }

    /**
     * Возвращает ячейки табличной разметки в конкртеной строке
     * @return ячейки табличной разметки в конкртеной строке
     */
    public List<Cell> getRowCells(int index) {
        return cells.get(index);
    }
}

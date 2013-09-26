package ru.intertrust.cm.core.gui.impl.client.plugins.collection.view.panel;

import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;

/**
 * Created with IntelliJ IDEA.
 * User: lvov
 * Date: 13.09.13
 * Time: 15:46
 * To change this template use File | Settings | File Templates.
 */
public class ColumnResizeController<T> extends ResizableHeader<T> {
    private final CellTable<T> tableHeader;
    private final CellTable<T> tableBody;

    public ColumnResizeController(String title, CellTable<T> tableHeader, CellTable<T> tableBody, Column<T, ?> column) {
        super(title, tableHeader, tableBody, column);
        this.tableHeader = tableHeader;
        this.tableBody = tableBody;
    }



    @Override
    protected int getTableBodyHeight() {
        return tableHeader.getBodyHeight();
    }
}
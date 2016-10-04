package ru.intertrust.cm.core.gui.impl.client.themes.taurika.datagrid;

import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.cellview.client.DataGrid;

/**
 * Created by IntelliJ IDEA.
 * Developer: Ravil Abdulkhairov
 * Date: 04.10.2016
 * Time: 15:07
 * To change this template use File | Settings | File and Code Templates.
 */
public interface TaurikaDataGridResources extends DataGrid.Resources {

    String CSS_FILE = "CellTableCommon.css";

    @Source("empty.png")
    ImageResource dataGridSortAscending();

    @Source("empty.png")
    ImageResource dataGridSortDescending();

    @CssResource.ImportedWithPrefix("gwt-CellTable")
    public interface StyleEx extends DataGrid.Style {



        /**
         * Applied to every cell.
         */
        @ClassName("dataGridCell")
        String dataGridCell();

        /**
         * Applied to even rows.
         */
        @ClassName("dataGridEvenRow")
        String dataGridEvenRow();

        /**
         * Applied to cells in even rows.
         */
        @ClassName("dataGridEvenRowCell")
        String dataGridEvenRowCell();

        /**
         * Applied to the first column.
         */
        @ClassName("dataGridFirstColumn")
        String dataGridFirstColumn();

        /**
         * Applied to the first column footers.
         */
        @ClassName("dataGridFirstColumnFooter")
        String dataGridFirstColumnFooter();

        /**
         * Applied to the first column headers.
         */
        @ClassName("dataGridFirstColumnHeader")
        String dataGridFirstColumnHeader();

        /**
         * Applied to footers cells.
         */
        @ClassName("dataGridFooter")
        String dataGridFooter();

        /**
         * Applied to headers cells.
         */
        @ClassName("dataGridHeader")
        String dataGridHeader();

        /**
         * Applied to the hovered row.
         */
        @ClassName("dataGridHoveredRow")
        String dataGridHoveredRow();

        /**
         * Applied to the cells in the hovered row.
         */
        @ClassName("dataGridHoveredRowCell")
        String dataGridHoveredRowCell();

        /**
         * Applied to the keyboard selected cell.
         */
        @ClassName("dataGridKeyboardSelectedCell")
        String dataGridKeyboardSelectedCell();

        /**
         * Applied to the keyboard selected row.
         */
        @ClassName("dataGridKeyboardSelectedRow")
        String dataGridKeyboardSelectedRow();

        /**
         * Applied to the cells in the keyboard selected row.
         */
        @ClassName("dataGridKeyboardSelectedRowCell")
        String dataGridKeyboardSelectedRowCell();

        /**
         * Applied to the last column.
         */
        @ClassName("dataGridLastColumn")
        String dataGridLastColumn();

        /**
         * Applied to the last column footers.
         */
        @ClassName("dataGridLastColumnFooter")
        String dataGridLastColumnFooter();

        /**
         * Applied to the last column headers.
         */
        @ClassName("dataGridLastColumnHeader")
        String dataGridLastColumnHeader();

        /**
         * Applied to odd rows.
         */
        @ClassName("dataGridOddRow")
        String dataGridOddRow();

        /**
         * Applied to cells in odd rows.
         */
        @ClassName("dataGridOddRowCell")
        String dataGridOddRowCell();

        /**
         * Applied to selected rows.
         */
        @ClassName("dataGridSelectedRow")
        String dataGridSelectedRow();

        /**
         * Applied to cells in selected rows.
         */
        @ClassName("dataGridSelectedRowCell")
        String dataGridSelectedRowCell();

        /**
         * Applied to header cells that are sortable.
         */
        @ClassName("dataGridSortableHeader")
        String dataGridSortableHeader();

        /**
         * Applied to header cells that are sorted in ascending order.
         */
        @ClassName("dataGridSortedHeaderAscending")
        String dataGridSortedHeaderAscending();

        /**
         * Applied to header cells that are sorted in descending order.
         */
        @ClassName("dataGridSortedHeaderDescending")
        String dataGridSortedHeaderDescending();

        /**
         * Applied to the table.
         */
        @ClassName("dataGridWidget")
        String dataGridWidget();
    }


    @Override
    @CssResource.NotStrict
    @Source(CSS_FILE)
    DataGrid.Style dataGridStyle();
}

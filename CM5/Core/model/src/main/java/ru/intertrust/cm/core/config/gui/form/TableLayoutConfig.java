package ru.intertrust.cm.core.config.gui.form;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.ArrayList;
import java.util.List;

/**
 * Табличная разметка. Позволяет отобразить элементы пользовательского интерфейса в ячейках таблицы, находящихся в
 * строках и столбцах.
 *
 * @author Denis Mitavskiy
 *         Date: 05.09.13
 *         Time: 17:49
 */
@Root(name = "table")
public class TableLayoutConfig implements LayoutConfig {
    @Attribute(name = "width", required = false)
    private String width;

    @Attribute(name = "height", required = false)
    private String height;

    @Attribute(name = "row-height", required = false)
    private String rowHeight;

    @Attribute(name = "col-width", required = false)
    private String colWidth;

    @Attribute(name = "h-align", required = false)
    private String hAlign;

    @Attribute(name = "v-align", required = false)
    private String vAlign;

    @ElementList(inline = true)
    private List<RowConfig> rows = new ArrayList<RowConfig>();

    public List<RowConfig> getRows() {
        return rows;
    }

    public void setRows(List<RowConfig> rows) {
        this.rows = rows;
    }

    public String getWidth() {
        return width;
    }

    public void setWidth(String width) {
        this.width = width;
    }

    public String getHeight() {
        return height;
    }

    public void setHeight(String height) {
        this.height = height;
    }

    public String getRowHeight() {
        return rowHeight;
    }

    public void setRowHeight(String rowHeight) {
        this.rowHeight = rowHeight;
    }

    public String getColWidth() {
        return colWidth;
    }

    public void setColWidth(String colWidth) {
        this.colWidth = colWidth;
    }

    public String getHAlign() {
        return hAlign;
    }

    public void setHAlign(String hAlign) {
        this.hAlign = hAlign;
    }

    public String getVAlign() {
        return vAlign;
    }

    public void setVAlign(String vAlign) {
        this.vAlign = vAlign;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        TableLayoutConfig that = (TableLayoutConfig) o;

        if (colWidth != null ? !colWidth.equals(that.colWidth) : that.colWidth != null) {
            return false;
        }
        if (hAlign != null ? !hAlign.equals(that.hAlign) : that.hAlign != null) {
            return false;
        }
        if (height != null ? !height.equals(that.height) : that.height != null) {
            return false;
        }
        if (rowHeight != null ? !rowHeight.equals(that.rowHeight) : that.rowHeight != null) {
            return false;
        }
        if (rows != null ? !rows.equals(that.rows) : that.rows != null) {
            return false;
        }
        if (vAlign != null ? !vAlign.equals(that.vAlign) : that.vAlign != null) {
            return false;
        }
        if (width != null ? !width.equals(that.width) : that.width != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = width != null ? width.hashCode() : 0;
        result = 31 * result + (height != null ? height.hashCode() : 0);
        result = 31 * result + (rowHeight != null ? rowHeight.hashCode() : 0);
        result = 31 * result + (colWidth != null ? colWidth.hashCode() : 0);
        result = 31 * result + (hAlign != null ? hAlign.hashCode() : 0);
        result = 31 * result + (vAlign != null ? vAlign.hashCode() : 0);
        result = 31 * result + (rows != null ? rows.hashCode() : 0);
        return result;
    }
}

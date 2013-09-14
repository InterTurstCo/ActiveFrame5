package ru.intertrust.cm.core.config.model.gui.form;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.config.model.gui.form.widget.CellWidgetConfig;

/**
 * Класс определяет ячейку в табличной разметке
 *
 * @author Denis Mitavskiy
 *         Date: 06.09.13
 *         Time: 13:09
 */
@Root(name = "td")
public class CellConfig implements Dto {
    @Attribute(name = "colspan", required = false)
    private String columnSpan;

    @Attribute(name = "rowspan", required = false)
    private String rowSpan;

    @Attribute(name = "h-align", required = false)
    private String horizontalAlignment;

    @Attribute(name = "v-align", required = false)
    private String verticalAlignment;

    @Element(name = "widget", required = false)
    private CellWidgetConfig widgetConfig;

    public String getColumnSpan() {
        return columnSpan;
    }

    public void setColumnSpan(String columnSpan) {
        this.columnSpan = columnSpan;
    }

    public String getRowSpan() {
        return rowSpan;
    }

    public void setRowSpan(String rowSpan) {
        this.rowSpan = rowSpan;
    }

    public String getHorizontalAlignment() {
        return horizontalAlignment;
    }

    public void setHorizontalAlignment(String horizontalAlignment) {
        this.horizontalAlignment = horizontalAlignment;
    }

    public String getVerticalAlignment() {
        return verticalAlignment;
    }

    public void setVerticalAlignment(String verticalAlignment) {
        this.verticalAlignment = verticalAlignment;
    }

    public CellWidgetConfig getWidgetConfig() {
        return widgetConfig;
    }

    public void setWidgetConfig(CellWidgetConfig widgetConfig) {
        this.widgetConfig = widgetConfig;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        CellConfig that = (CellConfig) o;

        if (columnSpan != null ? !columnSpan.equals(that.columnSpan) : that.columnSpan != null) {
            return false;
        }
        if (horizontalAlignment != null ? !horizontalAlignment.equals(that.horizontalAlignment) : that.horizontalAlignment != null) {
            return false;
        }
        if (rowSpan != null ? !rowSpan.equals(that.rowSpan) : that.rowSpan != null) {
            return false;
        }
        if (verticalAlignment != null ? !verticalAlignment.equals(that.verticalAlignment) : that.verticalAlignment != null) {
            return false;
        }
        if (widgetConfig != null ? !widgetConfig.equals(that.widgetConfig) : that.widgetConfig != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = columnSpan != null ? columnSpan.hashCode() : 0;
        result = 31 * result + (rowSpan != null ? rowSpan.hashCode() : 0);
        result = result + (horizontalAlignment != null ? horizontalAlignment.hashCode() : 0);
        result = result + (verticalAlignment != null ? verticalAlignment.hashCode() : 0);
        result = result + (widgetConfig != null ? widgetConfig.hashCode() : 0);
        return result;
    }
}

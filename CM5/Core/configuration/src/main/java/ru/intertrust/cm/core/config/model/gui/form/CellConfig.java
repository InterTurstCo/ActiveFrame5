package ru.intertrust.cm.core.config.model.gui.form;

import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.config.model.gui.form.widget.WidgetConfig;

/**
 * Класс определяет ячейку в табличной разметке
 *
 * @author Denis Mitavskiy
 *         Date: 06.09.13
 *         Time: 13:09
 */
public class CellConfig implements Dto {
    private String columnSpan;
    private String rowSpan;
    private String horizontalAlignment;
    private String verticalAlignment;
    private WidgetConfig widgetConfig;

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

    public WidgetConfig getWidgetConfig() {
        return widgetConfig;
    }

    public void setWidgetConfig(WidgetConfig widgetConfig) {
        this.widgetConfig = widgetConfig;
    }
}

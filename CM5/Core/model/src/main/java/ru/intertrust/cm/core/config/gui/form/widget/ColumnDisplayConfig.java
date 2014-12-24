package ru.intertrust.cm.core.config.gui.form.widget;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import ru.intertrust.cm.core.business.api.dto.Dto;

/**
 * Created by andrey on 08.12.14.
 */
public class ColumnDisplayConfig implements Dto {

    public enum Position {
        before, after
    }
    @Attribute(name = "position", required = false)
    private String position;
    @Attribute(name = "tooltip", required = false)
    private String tooltip;

    @Element(name = "image", required = false)
    private ColumnDisplayImageConfig columnDisplayImageConfig;
    @Element(name = "text", required = false)
    private ColumnDisplayTextConfig columnDisplayTextConfig;

    public ColumnDisplayImageConfig getColumnDisplayImageConfig() {
        return columnDisplayImageConfig;
    }

    public void setColumnDisplayImageConfig(ColumnDisplayImageConfig columnDisplayImageConfig) {
        this.columnDisplayImageConfig = columnDisplayImageConfig;
    }

    public ColumnDisplayTextConfig getColumnDisplayTextConfig() {
        return columnDisplayTextConfig;
    }

    public void setColumnDisplayTextConfig(ColumnDisplayTextConfig columnDisplayTextConfig) {
        this.columnDisplayTextConfig = columnDisplayTextConfig;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getTooltip() {
        return tooltip;
    }

    public void setTooltip(String tooltip) {
        this.tooltip = tooltip;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ColumnDisplayConfig that = (ColumnDisplayConfig) o;

        if (columnDisplayImageConfig != null ? !columnDisplayImageConfig.equals(that.columnDisplayImageConfig) : that.columnDisplayImageConfig != null)
            return false;
        if (columnDisplayTextConfig != null ? !columnDisplayTextConfig.equals(that.columnDisplayTextConfig) : that.columnDisplayTextConfig != null)
            return false;
        if (position != null ? !position.equals(that.position) : that.position != null) return false;
        if (tooltip != null ? !tooltip.equals(that.tooltip) : that.tooltip != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = position != null ? position.hashCode() : 0;
        result = 31 * result + (tooltip != null ? tooltip.hashCode() : 0);
        result = 31 * result + (columnDisplayImageConfig != null ? columnDisplayImageConfig.hashCode() : 0);
        result = 31 * result + (columnDisplayTextConfig != null ? columnDisplayTextConfig.hashCode() : 0);
        return result;
    }
}

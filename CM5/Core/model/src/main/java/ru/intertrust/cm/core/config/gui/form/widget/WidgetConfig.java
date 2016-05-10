package ru.intertrust.cm.core.config.gui.form.widget;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import ru.intertrust.cm.core.config.gui.IdentifiedConfig;
import ru.intertrust.cm.core.config.gui.NotNullLogicalValidation;

/**
 * @author Denis Mitavskiy
 *         Date: 14.09.13
 *         Time: 14:16
 */
public abstract class WidgetConfig implements IdentifiedConfig {
    private static final String MAX_DEFAULT_TOOLTIP_WIDTH = "400px";
    private static final String MAX_DEFAULT_TOOLTIP_HEIGHT = "300px";

    @NotNullLogicalValidation
    @Attribute(name = "id", required = false)
    protected String id;
    @Attribute(name = "read-only", required = false)
    protected Boolean readOnly;
    @Attribute(name = "handler", required = false)
    protected String handler;
    @Attribute(name = "max-tooltip-width", required = false)
    protected String maxTooltipWidth;
    @Attribute(name = "max-tooltip-height", required = false)
    protected String maxTooltipHeight;
    @Attribute(name = "persist", required = false)
    protected Boolean persist;

    //TODO: "questionlist","solutionslist" should be removed after hierarchywidget implementation		
    @NotNullLogicalValidation(skippedComponentNames = {"label", "table-viewer", "coordination", "questionlist", "solutionslist", "hierarchywidget", "cases-widget"})
    @Element(name = "field-path", required = false)
    protected FieldPathConfig fieldPathConfig;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getHandler() {
        return handler;
    }

    public void setHandler(String handler) {
        this.handler = handler;
    }

    public FieldPathConfig getFieldPathConfig() {
        return fieldPathConfig;
    }

    public void setFieldPathConfig(FieldPathConfig fieldPathConfig) {
        this.fieldPathConfig = fieldPathConfig;
    }

    public boolean isReadOnly() {
        return readOnly == null ? false : readOnly;
    }

    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    public String getMaxTooltipWidth() {
        return maxTooltipWidth == null ? MAX_DEFAULT_TOOLTIP_WIDTH : maxTooltipWidth;
    }

    public Boolean isPersist() {
        if (persist == null) {
            persist = true;
            return persist;
        } else {
            return persist;
        }
    }

    public void setPersist(Boolean persist) {
        this.persist = persist;
    }

    public void setMaxTooltipWidth(String maxTooltipWidth) {
        this.maxTooltipWidth = maxTooltipWidth;
    }

    public String getMaxTooltipHeight() {
        return maxTooltipHeight == null ? MAX_DEFAULT_TOOLTIP_HEIGHT : maxTooltipHeight;
    }

    public void setMaxTooltipHeight(String maxTooltipHeight) {
        this.maxTooltipHeight = maxTooltipHeight;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        WidgetConfig that = (WidgetConfig) o;

        if (readOnly != null ? !readOnly.equals(that.readOnly) : that.readOnly != null) {
            return false;
        }
        if (fieldPathConfig != null ? !fieldPathConfig.equals(that.fieldPathConfig) : that.fieldPathConfig != null) {
            return false;
        }
        if (id != null ? !id.equals(that.id) : that.id != null) {
            return false;
        }
        if (handler != null ? !handler.equals(that.handler) : that.handler != null) {
            return false;
        }
        if (maxTooltipWidth != null ? !maxTooltipWidth.equals(that.maxTooltipWidth) : that.maxTooltipWidth != null) {
            return false;
        }
        if (maxTooltipHeight != null ? !maxTooltipHeight.equals(that.maxTooltipHeight) : that.maxTooltipHeight != null) {
            return false;
        }
        if (persist != null ? !persist.equals(that.persist) : that.persist != null) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (readOnly != null ? readOnly.hashCode() : 0);
        result = 31 * result + (fieldPathConfig != null ? fieldPathConfig.hashCode() : 0);
        result = 31 * result + (persist != null ? persist.hashCode() : 0);
        return result;
    }

    public abstract String getComponentName();

    public boolean handlesMultipleObjects() {
        return false;
    }
}

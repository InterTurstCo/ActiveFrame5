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

    @Element(name = "events",required = false)
    EventsTypeConfig eventsTypeConfig;

    @Element(name = "rules",required = false)
    RulesTypeConfig rulesTypeConfig;

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
    @Attribute(name = "translate-id", required = false)
    protected Boolean translateId;

    //TODO: "questionlist","solutionslist" should be removed after hierarchywidget implementation		
    @NotNullLogicalValidation(skippedComponentNames = {"label", "table-viewer", "coordination", "questionlist", "solutionslist", "hierarchywidget", "cases-widget","list-cell","editable-table-browser"})
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

    public Boolean getTranslateId() {
        return  translateId == null ? false : translateId;
    }

    public void setTranslateId(Boolean translateId) {
        this.translateId = translateId;
    }

    public EventsTypeConfig getEventsTypeConfig() {
        return eventsTypeConfig;
    }

    public void setEventsTypeConfig(EventsTypeConfig eventsTypeConfig) {
        this.eventsTypeConfig = eventsTypeConfig;
    }

    public RulesTypeConfig getRulesTypeConfig() {
        return rulesTypeConfig;
    }

    public void setRulesTypeConfig(RulesTypeConfig rulesTypeConfig) {
        this.rulesTypeConfig = rulesTypeConfig;
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
        if (translateId != null ? !translateId.equals(that.translateId) : that.translateId != null) {
            return false;
        }
        if (eventsTypeConfig != null ? !eventsTypeConfig.equals(that.eventsTypeConfig) : that.eventsTypeConfig != null) {
            return false;
        }
        if (rulesTypeConfig != null ? !rulesTypeConfig.equals(that.rulesTypeConfig) : that.rulesTypeConfig != null) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    public abstract String getComponentName();

    public boolean handlesMultipleObjects() {
        return false;
    }

    /**
     * Возвращает название компонента (на данный момент, spring-бина), который отвечает за логическую валидацию виджета
     * @return название компонента (на данный момент, spring-бина), который отвечает за логическую валидацию виджета
     */
    public String getLogicalValidatorComponentName() {
        return null;
    }
}

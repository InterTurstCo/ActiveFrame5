package ru.intertrust.cm.core.config.gui.form;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.business.api.dto.Dto;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 22.02.2015
 *         Time: 9:56
 */
@Root(name = "default-value-setter")
public class DefaultValueSetterConfig implements Dto {
    @Attribute(name = "component", required = false)
    private String component;

    @Element(name = "widget-states")
    private WidgetStatesConfig widgetStatesConfig;

    public String getComponent() {
        return component;
    }

    public void setComponent(String component) {
        this.component = component;
    }

    public WidgetStatesConfig getWidgetStatesConfig() {
        return widgetStatesConfig;
    }

    public void setWidgetStatesConfig(WidgetStatesConfig widgetStatesConfig) {
        this.widgetStatesConfig = widgetStatesConfig;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        DefaultValueSetterConfig that = (DefaultValueSetterConfig) o;

        if (component != null ? !component.equals(that.component) : that.component != null) {
            return false;
        }
        if (widgetStatesConfig != null ? !widgetStatesConfig.equals(that.widgetStatesConfig)
                : that.widgetStatesConfig != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = component != null ? component.hashCode() : 0;
        result = 31 * result + (widgetStatesConfig != null ? widgetStatesConfig.hashCode() : 0);
        return result;
    }
}

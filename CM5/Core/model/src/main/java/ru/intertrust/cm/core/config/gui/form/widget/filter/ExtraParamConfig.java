package ru.intertrust.cm.core.config.gui.form.widget.filter;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 01.11.2014
 *         Time: 17:41
 */
@Root(name = "param")
public class ExtraParamConfig extends ComplexParamConfig {
    @Attribute(name = "widget-id", required = false)
    private String widgetId;

    public String getWidgetId() {
        return widgetId;
    }

    public void setWidgetId(String widgetId) {
        this.widgetId = widgetId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        ExtraParamConfig that = (ExtraParamConfig) o;

        if (widgetId != null ? !widgetId.equals(that.widgetId) : that.widgetId != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (widgetId != null ? widgetId.hashCode() : 0);
        return result;
    }
}

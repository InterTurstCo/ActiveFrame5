package ru.intertrust.cm.core.config.gui.form.widget;

import org.simpleframework.xml.Attribute;
import ru.intertrust.cm.core.business.api.dto.Dto;

/**
 * Created by tbilyi on 27.08.2014.
 */
public class ConfirmationForConfig implements Dto {
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
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ConfirmationForConfig that = (ConfirmationForConfig) o;

        if (widgetId != null ? !widgetId.equals(that.widgetId) : that.widgetId != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return widgetId != null ? widgetId.hashCode() : 0;
    }
}

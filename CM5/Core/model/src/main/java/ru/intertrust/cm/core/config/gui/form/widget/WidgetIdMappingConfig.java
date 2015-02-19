package ru.intertrust.cm.core.config.gui.form.widget;

import org.simpleframework.xml.Attribute;
import ru.intertrust.cm.core.business.api.dto.Dto;

/**
 * Created by andrey on 07.11.14.
 */
@Deprecated
public class WidgetIdMappingConfig implements Dto {
    @Attribute(name = "linked-form-name")
    private String linkedFormName;
    @Attribute(name = "widget-id", required = false)
    private String widgetId;

    public String getLinkedFormName() {
        return linkedFormName;
    }

    public void setLinkedFormName(String linkedFormName) {
        this.linkedFormName = linkedFormName;
    }

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

        WidgetIdMappingConfig that = (WidgetIdMappingConfig) o;

        if (linkedFormName != null ? !linkedFormName.equals(that.linkedFormName) : that.linkedFormName != null)
            return false;
        if (widgetId != null ? !widgetId.equals(that.widgetId) : that.widgetId != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = linkedFormName != null ? linkedFormName.hashCode() : 0;
        result = 31 * result + (widgetId != null ? widgetId.hashCode() : 0);
        return result;
    }
}

package ru.intertrust.cm.core.config.model.gui.form.widget;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.business.api.dto.Dto;

/**
 * @author Yaroslav Bondacrhuk
 *         Date: 13/9/13
 *         Time: 12:05 PM
 */
@Root(name = "template-based-widget")
public class TemplateBasedWidgetConfig implements Dto {
    @Attribute(name = "id")
    private String id;

    @Attribute(name = "template-name", required = false)
    private String widgetId;

    @Element(name = "field-path", required = false)
    private FieldPathConfig fieldPathConfig;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getWidgetId() {
        return widgetId;
    }

    public void setWidgetId(String widgetId) {
        this.widgetId = widgetId;
    }

    public FieldPathConfig getFieldPathConfig() {
        return fieldPathConfig;
    }

    public void setFieldPathConfig(FieldPathConfig fieldPathConfig) {
        this.fieldPathConfig = fieldPathConfig;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        TemplateBasedWidgetConfig that = (TemplateBasedWidgetConfig) o;

        if (id != null ? !id.equals(that.id) : that.id != null) {
            return false;
        }
        if (fieldPathConfig != null ? !fieldPathConfig.equals(that.fieldPathConfig) : that.fieldPathConfig != null) {
            return false;
        }

        if (widgetId != null ? !widgetId.equals(that.widgetId) : that.widgetId != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (widgetId != null ? widgetId.hashCode() : 0);
        result = 31 * result + (fieldPathConfig != null ? fieldPathConfig.hashCode() : 0);
        return result;
    }
}

package ru.intertrust.cm.core.config.gui;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.business.api.dto.Dto;

/**
 * @author Lesia Puhova
 *         Date: 10.03.14
 *         Time: 13:23
 */
@Root(name="validator")
public class ValidatorConfig implements Dto {

    @Attribute(name="class-name", required = true)
    private String className;

    @Attribute(name="widget-id", required = true)
    private String widgetId;


    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

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
        ValidatorConfig that = (ValidatorConfig) o;
        if (className != null ? !className.equals(that.className) : that.className != null) {
            return false;
        }
        if (widgetId != null ? !widgetId.equals(that.widgetId) : that.widgetId != null) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = className != null ? className.hashCode() : 0;
        result = 31 * result + (widgetId != null ? widgetId.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "className: " + className + ", widget-id: " + widgetId;
    }
}

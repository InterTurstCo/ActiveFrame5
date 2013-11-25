package ru.intertrust.cm.core.config.gui.form.widget;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;
/**
 * @author Yaroslav Bondacrhuk
 *         Date: 13/9/13
 *         Time: 12:05 PM
 */
@Root(name = "template-based-widget")
public class TemplateBasedWidgetConfig extends WidgetConfig {

    @Attribute(name = "template-name", required = false)
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

        TemplateBasedWidgetConfig that = (TemplateBasedWidgetConfig) o;

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
    @Override
    public String getComponentName() {
        return "";  //To change body of implemented methods use File | Settings | File Templates.
    }
}

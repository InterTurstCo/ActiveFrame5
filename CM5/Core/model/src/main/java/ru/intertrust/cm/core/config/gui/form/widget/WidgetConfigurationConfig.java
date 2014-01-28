package ru.intertrust.cm.core.config.gui.form.widget;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.ElementListUnion;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.convert.Convert;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.config.converter.WidgetConfigurationConfigConverter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Yaroslav Bondacrhuk
 *         Date: 13/9/13
 *         Time: 12:05 PM
 */
@Root(name = "widget-config")
@Convert(WidgetConfigurationConfigConverter.class)
public class WidgetConfigurationConfig implements Dto {

    @ElementList(type=WidgetConfig.class, inline=true)
    private List<WidgetConfig> widgetConfigList = new ArrayList<WidgetConfig>();

    public List<WidgetConfig> getWidgetConfigList() {
        return widgetConfigList;
    }

    public void setWidgetConfigList(List<WidgetConfig> widgetConfigList) {
        this.widgetConfigList = widgetConfigList;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        WidgetConfigurationConfig that = (WidgetConfigurationConfig) o;

        if (widgetConfigList != null ? !widgetConfigList.equals(that.widgetConfigList) : that.
                widgetConfigList != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = widgetConfigList != null ? widgetConfigList.hashCode() : 0;

        return result;
    }
}
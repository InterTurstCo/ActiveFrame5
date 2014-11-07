package ru.intertrust.cm.core.config.gui.form.widget;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.business.api.dto.Dto;

import java.util.List;

/**
 * Created by andrey on 07.11.14.
 */
@Root(name = "widget-id-mappings")
public class WidgetIdMappingsConfig implements Dto {

    @ElementList(name = "widget-id-mapping", entry = "widget-id-mapping", inline = true)
    private List<WidgetIdMappingConfig> widgetIdMappingConfigs;

    public List<WidgetIdMappingConfig> getWidgetIdMappingConfigs() {
        return widgetIdMappingConfigs;
    }

    public void setWidgetIdMappingConfigs(List<WidgetIdMappingConfig> widgetIdMappingConfigs) {
        this.widgetIdMappingConfigs = widgetIdMappingConfigs;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        WidgetIdMappingsConfig that = (WidgetIdMappingsConfig) o;

        if (widgetIdMappingConfigs != null ? !widgetIdMappingConfigs.equals(that.widgetIdMappingConfigs) : that.widgetIdMappingConfigs != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        return widgetIdMappingConfigs != null ? widgetIdMappingConfigs.hashCode() : 0;
    }
}

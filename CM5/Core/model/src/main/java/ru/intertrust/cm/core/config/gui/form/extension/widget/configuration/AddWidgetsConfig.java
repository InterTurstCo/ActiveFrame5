package ru.intertrust.cm.core.config.gui.form.extension.widget.configuration;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.convert.Convert;
import ru.intertrust.cm.core.config.converter.AddWidgetsConfigConverter;
import ru.intertrust.cm.core.config.gui.form.extension.FormExtensionOperation;
import ru.intertrust.cm.core.config.gui.form.widget.WidgetConfig;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 02.05.2015
 *         Time: 21:52
 */
@Root(name = "add-widgets")
@Convert(AddWidgetsConfigConverter.class)
public class AddWidgetsConfig implements FormExtensionOperation {
    @ElementList(type=WidgetConfig.class, inline=true)
    private List<WidgetConfig> widgetConfigs = new ArrayList<WidgetConfig>();

    public List<WidgetConfig> getWidgetConfigs() {
        return widgetConfigs;
    }

    public void setWidgetConfigs(List<WidgetConfig> widgetConfigs) {
        this.widgetConfigs = widgetConfigs;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        AddWidgetsConfig that = (AddWidgetsConfig) o;

        if (widgetConfigs != null ? !widgetConfigs.equals(that.widgetConfigs)
                : that.widgetConfigs != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return widgetConfigs != null ? widgetConfigs.hashCode() : 0;
    }

}

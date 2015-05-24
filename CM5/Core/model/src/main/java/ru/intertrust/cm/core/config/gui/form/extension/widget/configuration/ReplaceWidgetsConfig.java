package ru.intertrust.cm.core.config.gui.form.extension.widget.configuration;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.convert.Convert;
import ru.intertrust.cm.core.config.converter.ReplaceWidgetsConfigConverter;
import ru.intertrust.cm.core.config.gui.form.extension.FormExtensionOperation;
import ru.intertrust.cm.core.config.gui.form.widget.WidgetConfig;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 03.05.2015
 *         Time: 13:36
 */
@Root(name = "replace-widgets")
@Convert(ReplaceWidgetsConfigConverter.class)
public class ReplaceWidgetsConfig implements FormExtensionOperation {
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

        ReplaceWidgetsConfig that = (ReplaceWidgetsConfig) o;

        if (widgetConfigs != null ? !widgetConfigs.equals(that.widgetConfigs): that.widgetConfigs != null) {

            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return widgetConfigs != null ? widgetConfigs.hashCode() : 0;
    }

}


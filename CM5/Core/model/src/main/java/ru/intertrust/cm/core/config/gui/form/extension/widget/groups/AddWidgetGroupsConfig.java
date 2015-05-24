package ru.intertrust.cm.core.config.gui.form.extension.widget.groups;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.config.gui.form.WidgetGroupConfig;
import ru.intertrust.cm.core.config.gui.form.extension.FormExtensionOperation;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 03.05.2015
 *         Time: 0:22
 */
@Root(name = "add-widget-groups")
public class AddWidgetGroupsConfig implements FormExtensionOperation {
    @ElementList(inline = true, name = "widget-group")
    private List<WidgetGroupConfig> widgetGroupConfigs = new ArrayList<WidgetGroupConfig>();

    public List<WidgetGroupConfig> getWidgetGroupConfigs() {
        return widgetGroupConfigs;
    }

    public void setWidgetGroupConfigs(List<WidgetGroupConfig> widgetGroupConfigs) {
        this.widgetGroupConfigs = widgetGroupConfigs;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        AddWidgetGroupsConfig that = (AddWidgetGroupsConfig) o;

        if (widgetGroupConfigs != null ? !widgetGroupConfigs.equals(that.widgetGroupConfigs) : that.widgetGroupConfigs != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        return widgetGroupConfigs != null ? widgetGroupConfigs.hashCode() : 0;
    }

}

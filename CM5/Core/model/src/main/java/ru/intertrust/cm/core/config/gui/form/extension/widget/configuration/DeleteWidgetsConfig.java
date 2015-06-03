package ru.intertrust.cm.core.config.gui.form.extension.widget.configuration;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.config.gui.form.WidgetRefConfig;
import ru.intertrust.cm.core.config.gui.form.extension.FormExtensionOperation;

import java.util.List;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 02.05.2015
 *         Time: 21:56
 */
@Root(name = "delete-widgets")
public class DeleteWidgetsConfig implements FormExtensionOperation {
    @ElementList(inline = true, name = "widget")
    private List<WidgetRefConfig> widgetRefConfigs;

    public List<WidgetRefConfig> getWidgetRefConfigs() {
        return widgetRefConfigs;
    }

    public void setWidgetRefConfigs(List<WidgetRefConfig> widgetRefConfigs) {
        this.widgetRefConfigs = widgetRefConfigs;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        DeleteWidgetsConfig that = (DeleteWidgetsConfig) o;

        if (widgetRefConfigs != null ? !widgetRefConfigs.equals(that.widgetRefConfigs) : that.widgetRefConfigs != null){
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return widgetRefConfigs != null ? widgetRefConfigs.hashCode() : 0;
    }

}

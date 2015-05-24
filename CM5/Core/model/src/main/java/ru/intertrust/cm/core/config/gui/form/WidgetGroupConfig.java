package ru.intertrust.cm.core.config.gui.form;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.config.gui.IdentifiedConfig;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Lesia Puhova
 *         Date: 02.09.14
 *         Time: 18:55
 */

@Root(name="widget-group")
public class WidgetGroupConfig implements IdentifiedConfig {

    @Attribute(name = "name", required = false)
    private String name;

    @Attribute(name = "id", required = false)
    private String id;

    @ElementList(inline = true, required = false)
    private List<WidgetRefConfig> widgetRefConfigList = new ArrayList<WidgetRefConfig>();


    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public List<WidgetRefConfig> getWidgetRefConfigList() {
        return widgetRefConfigList;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        WidgetGroupConfig that = (WidgetGroupConfig) o;

        if (!name.equals(that.name)) {
            return false;
        }
        if (!id.equals(that.id)) {
            return false;
        }
        if (!widgetRefConfigList.equals(that.widgetRefConfigList)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + id.hashCode();
        result = 31 * result + widgetRefConfigList.hashCode();
        return result;
    }
}

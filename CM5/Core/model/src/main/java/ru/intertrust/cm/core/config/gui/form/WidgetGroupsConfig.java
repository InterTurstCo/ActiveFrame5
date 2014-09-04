package ru.intertrust.cm.core.config.gui.form;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.business.api.dto.Dto;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Lesia Puhova
 *         Date: 03.09.14
 *         Time: 15:47
 */
@Root(name="widget-groups")
public class WidgetGroupsConfig implements Dto {

    @ElementList(inline = true)
    private List<WidgetGroupConfig> widgetGroupConfigList = new ArrayList<>();

    public List<WidgetGroupConfig> getWidgetGroupConfigList() {
        return widgetGroupConfigList;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        WidgetGroupsConfig that = (WidgetGroupsConfig) o;

        if (!widgetGroupConfigList.equals(that.widgetGroupConfigList)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return widgetGroupConfigList.hashCode();
    }
}

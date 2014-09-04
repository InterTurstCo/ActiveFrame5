package ru.intertrust.cm.core.config.gui.form;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.config.gui.GroupsConfig;
import ru.intertrust.cm.core.config.gui.UsersConfig;

/**
 * @author Lesia Puhova
 *         Date: 02.09.14
 *         Time: 16:08
 */
@Root(name="hide-widget")
public class HideWidgetConfig implements Dto {

    @Attribute(name = "widget-id", required = false)
    String widgetId;

    @Attribute(name = "widget-group-id", required = false)
    String widgetGroupId;

    @Element(name = "users", required = false)
    private UsersConfig usersConfig;

    @Element(name = "groups", required = false)
    private GroupsConfig groupsConfig;

    public String getWidgetId() {
        return widgetId;
    }

    public String getWidgetGroupId() {
        return widgetGroupId;
    }

    public UsersConfig getUsersConfig() {
        return usersConfig;
    }

    public GroupsConfig getGroupsConfig() {
        return groupsConfig;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        HideWidgetConfig that = (HideWidgetConfig) o;
        if (groupsConfig != null ? !groupsConfig.equals(that.groupsConfig) : that.groupsConfig != null) {
            return false;
        }
        if (usersConfig != null ? !usersConfig.equals(that.usersConfig) : that.usersConfig != null) {
            return false;
        }
        if (widgetGroupId != null ? !widgetGroupId.equals(that.widgetGroupId) : that.widgetGroupId != null) {
            return false;
        }
        if (widgetId != null ? !widgetId.equals(that.widgetId) : that.widgetId != null) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = widgetId != null ? widgetId.hashCode() : 0;
        result = 31 * result + (widgetGroupId != null ? widgetGroupId.hashCode() : 0);
        result = 31 * result + (usersConfig != null ? usersConfig.hashCode() : 0);
        result = 31 * result + (groupsConfig != null ? groupsConfig.hashCode() : 0);
        return result;
    }
}

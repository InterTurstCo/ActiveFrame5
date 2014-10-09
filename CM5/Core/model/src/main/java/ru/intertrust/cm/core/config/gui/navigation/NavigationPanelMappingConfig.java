package ru.intertrust.cm.core.config.gui.navigation;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.config.gui.UsersConfig;

/**
 * Created by IPetrov on 05.03.14.
 */

@Root(name = "navigation-panel-mapping")
public class NavigationPanelMappingConfig implements Dto {
    @Attribute(name = "name", required = false)
    private String name;

    @Element(name = "users", required = false)
    private UsersConfig usersConfig;

    @Element(name = "groups", required = false)
    private GroupsConfig groupsConfig;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public UsersConfig getUsersConfig() {
        return usersConfig;
    }

    public void setUsersConfig(UsersConfig usersConfig) {
        this.usersConfig = usersConfig;
    }

    public GroupsConfig getGroupsConfig() {
        return groupsConfig;
    }

    public void setGroupsConfig(GroupsConfig groupsConfig) {
        this.groupsConfig = groupsConfig;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        NavigationPanelMappingConfig that = (NavigationPanelMappingConfig) o;

        if (groupsConfig != null ? !groupsConfig.equals(that.groupsConfig) : that.groupsConfig != null) {
            return false;
        }
        if (!name.equals(that.name)) {
            return false;
        }
        if (usersConfig != null ? !usersConfig.equals(that.usersConfig) : that.usersConfig != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
}

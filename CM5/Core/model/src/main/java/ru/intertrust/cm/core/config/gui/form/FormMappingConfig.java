package ru.intertrust.cm.core.config.gui.form;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.config.gui.GroupsConfig;
import ru.intertrust.cm.core.config.gui.UsersConfig;

/**
 * @author Yaroslav Bondacrhuk
 *         Date: 13/9/13
 *         Time: 12:05 PM
 */
@Root(name = "form-mapping")
public class FormMappingConfig implements Dto {
    @Attribute(name = "form", required = false)
    private String form;

    @Attribute(name = "domain-object-type", required = false)
    private String domainObjectType;

    @Element(name = "users", required = false)
    private UsersConfig usersConfig;

    @Element(name = "groups", required = false)
    private GroupsConfig groupsConfig;

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

    public String getForm() {
        return form;
    }

    public void setForm(String form) {
        this.form = form;
    }

    public String getDomainObjectType() {
        return domainObjectType;
    }

    public void setDomainObjectType(String domainObjectType) {
        this.domainObjectType = domainObjectType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        FormMappingConfig that = (FormMappingConfig) o;

        if (groupsConfig != null ? !groupsConfig.equals(that.groupsConfig) : that.groupsConfig != null) {
            return false;
        }
        if (usersConfig != null ? !usersConfig.equals(that.usersConfig) : that.usersConfig != null) {
            return false;
        }
        if (form != null ? !form.equals(that.form) : that.form != null) {
            return false;
        }
        if (domainObjectType != null ? !domainObjectType.equals(that.domainObjectType) : that.domainObjectType != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = usersConfig != null ? usersConfig.hashCode() : 0;
        result = 31 * result + (groupsConfig != null ? groupsConfig.hashCode() : 0);
        result = 31 * result + (groupsConfig != null ? groupsConfig.hashCode() : 0);
        result = 31 * result + (domainObjectType != null ? domainObjectType.hashCode() : 0);
        return result;
    }
}

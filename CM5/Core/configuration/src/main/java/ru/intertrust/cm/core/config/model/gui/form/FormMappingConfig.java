package ru.intertrust.cm.core.config.model.gui.form;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.config.model.gui.RolesConfig;
import ru.intertrust.cm.core.config.model.gui.UsersConfig;

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

    @Element(name = "users")
    private UsersConfig usersConfig;

    @Element(name = "roles")
    private RolesConfig rolesConfig;

    public UsersConfig getUsersConfig() {
        return usersConfig;
    }

    public void setUsersConfig(UsersConfig usersConfig) {
        this.usersConfig = usersConfig;
    }

    public RolesConfig getRolesConfig() {
        return rolesConfig;
    }

    public void setRolesConfig(RolesConfig rolesConfig) {
        this.rolesConfig = rolesConfig;
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

        if (rolesConfig != null ? !rolesConfig.equals(that.rolesConfig) : that.rolesConfig != null) {
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
        result = 31 * result + (rolesConfig != null ? rolesConfig.hashCode() : 0);
        result = 31 * result + (rolesConfig != null ? rolesConfig.hashCode() : 0);
        result = 31 * result + (domainObjectType != null ? domainObjectType.hashCode() : 0);
        return result;
    }
}

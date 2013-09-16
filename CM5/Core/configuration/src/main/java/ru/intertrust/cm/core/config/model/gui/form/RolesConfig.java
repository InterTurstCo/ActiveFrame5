package ru.intertrust.cm.core.config.model.gui.form;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.business.api.dto.Dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: User
 * Date: 13.09.13
 * Time: 15:33
 * To change this template use File | Settings | File Templates.
 */
@Root(name = "roles")
public class RolesConfig implements Dto{
    @ElementList(inline = true)
    private List<RoleConfig> roleConfigList = new ArrayList<RoleConfig>();

    public List<RoleConfig> getRoleConfigList() {
        return roleConfigList;
    }

    public void setRoleConfigList(List<RoleConfig> roleConfigList) {
        this.roleConfigList = roleConfigList;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        RolesConfig that = (RolesConfig) o;

        if (roleConfigList != null ? !roleConfigList.equals(that.roleConfigList) : that.roleConfigList != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return roleConfigList != null ? roleConfigList.hashCode() : 0;
    }
}

package ru.intertrust.cm.core.config.model.gui.form;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.business.api.dto.Dto;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Yaroslav Bondacrhuk
 *         Date: 13/9/13
 *         Time: 12:05 PM
 */
@Root(name = "users")
public class UsersConfig implements Dto{
    @ElementList(inline = true)
    private List<UserConfig> userConfigList = new ArrayList<UserConfig>();

    public List<UserConfig> getUserConfigList() {
        return userConfigList;
    }

    public void setUserConfigList(List<UserConfig> userConfigList) {
        this.userConfigList = userConfigList;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        UsersConfig that = (UsersConfig) o;

        if (userConfigList != null ? !userConfigList.equals(that.userConfigList) : that.
                userConfigList != null) {
                    return false;
        }


        return true;
    }

    @Override
    public int hashCode() {
        return userConfigList != null ? userConfigList.hashCode() : 0;
    }
}

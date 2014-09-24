package ru.intertrust.cm.core.config.gui.collection.view;

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
@Root(name = "collection-mapping")
public class CollectionMappingConfig implements Dto {
    @Attribute(name = "collection", required = false)
    private String collection;

    @Attribute(name = "view", required = false)
    private String view;

    @Element(name = "users", required = false)
    private UsersConfig usersConfig;

    @Element(name = "groups", required = false)
    private GroupsConfig groupsConfig;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        CollectionMappingConfig that = (CollectionMappingConfig) o;

        if (collection != null ? !collection.equals(that.collection) : that.collection != null) {
            return false;
        }
        if (groupsConfig != null ? !groupsConfig.equals(that.groupsConfig) : that.groupsConfig != null) {
            return false;
        }
        if (usersConfig != null ? !usersConfig.equals(that.usersConfig) : that.usersConfig != null) {
            return false;
        }
        if (view != null ? !view.equals(that.view) : that.view != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = collection != null ? collection.hashCode() : 0;
        result = 31 * result + (view != null ? view.hashCode() : 0);
        result = 31 * result + (usersConfig != null ? usersConfig.hashCode() : 0);
        result = 31 * result + (groupsConfig != null ? groupsConfig.hashCode() : 0);
        return result;
    }
}

package ru.intertrust.cm.core.config;

import java.util.ArrayList;
import java.util.List;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.ElementListUnion;

/**
 * Конфигурация состава роли, внутри может быть один или несколько различных тегов (group или collector).
 * @author atsvetkov
 *
 */
public class ContextRoleGroupsConfig {

    @ElementListUnion({
            @ElementList(entry = "track-domain-objects", type = TrackDomainObjectsConfig.class, inline = true),
            @ElementList(entry = "collector", type = CollectorConfig.class, inline = true)
    })
    private List<Object> groups = new ArrayList<>();

    public List<Object> getGroups() {
        return groups;
    }

    public void setGroups(List<Object> groups) {
        this.groups = groups;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ContextRoleGroupsConfig that = (ContextRoleGroupsConfig) o;

        if (groups != null ? !groups.equals(that.groups) : that.groups != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = groups != null ? groups.hashCode() : 0;
        return result;
    }

}

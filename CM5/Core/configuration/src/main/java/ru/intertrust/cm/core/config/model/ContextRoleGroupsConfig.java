package ru.intertrust.cm.core.config.model;

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
            @ElementList(entry = "group", type = GroupConfig.class, inline = true),
            @ElementList(entry = "collector", type = CollectorConfig.class, inline = true)
    })
    private List<Object> groups = new ArrayList<>();

    public List<Object> getGroups() {
        return groups;
    }

    public void setGroups(List<Object> groups) {
        this.groups = groups;
    }
}

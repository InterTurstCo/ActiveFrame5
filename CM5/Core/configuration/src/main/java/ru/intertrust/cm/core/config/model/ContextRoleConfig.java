package ru.intertrust.cm.core.config.model;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.config.model.base.TopLevelConfig;

/**
 *
 * @author atsvetkov
 *
 */
@Root(name = "context-role")
public class ContextRoleConfig implements TopLevelConfig {

    @Attribute(required = true)
    private String name;

    @Element(name = "context", required = true)
    private ContextConfig context;

    @Element(name = "groups")
    private ContextRoleGroupsConfig groups;

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ContextConfig getContext() {
        return context;
    }

    public void setContext(ContextConfig context) {
        this.context = context;
    }

    public ContextRoleGroupsConfig getGroups() {
        return groups;
    }

    public void setGroups(ContextRoleGroupsConfig groups) {
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

        ContextRoleConfig that = (ContextRoleConfig) o;

        if (context != null ? !context.equals(that.context) : that.context != null) {
            return false;
        }
        if (groups != null ? !groups.equals(that.groups) : that.groups != null) {
            return false;
        }
        if (name != null ? !name.equals(that.name) : that.name != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }
}

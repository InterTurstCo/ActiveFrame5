package ru.intertrust.cm.core.config.model;

import java.io.Serializable;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

/**
 * 
 * @author atsvetkov
 *
 */
@Root(name = "context-role")
public class ContextRoleConfig implements Serializable {

    @Attribute(required = true)
    private String name;

    @Element(name = "context")
    private ContextConfig context;
    
    @Element(name = "groups")    
    private ContextRoleGroupsConfig groups;
    
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
    
}

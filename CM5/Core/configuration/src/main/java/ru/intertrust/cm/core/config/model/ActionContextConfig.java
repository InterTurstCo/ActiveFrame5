package ru.intertrust.cm.core.config.model;

import java.util.List;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;

public class ActionContextConfig {

    @Attribute(required = true)
    private String name;

    @ElementList (entry = "domain-object-context", inline = true)
    private List<DomainObjectContextConfig> domainObjectContext;

    @ElementList (entry = "action", inline = true)
    private List<ActionConfig> action;

    public String getName() {
        return name;
    }

    public List<DomainObjectContextConfig> getDomainObjectContext() {
        return domainObjectContext;
    }

    public void setDomainObjectContext(
            List<DomainObjectContextConfig> domainObjectContext) {
        this.domainObjectContext = domainObjectContext;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<ActionConfig> getAction() {
        return action;
    }

    public void setAction(List<ActionConfig> action) {
        this.action = action;
    }


}

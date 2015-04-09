package ru.intertrust.cm.core.config.gui.action;

import java.util.List;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import ru.intertrust.cm.core.config.base.TopLevelConfig;
import ru.intertrust.cm.core.config.gui.DomainObjectContextConfig;

@Root(name="action-context")
public class ActionContextConfig implements TopLevelConfig{
    private static final long serialVersionUID = 7623941745498114136L;

    @Attribute(required = true)
    private String name;

    @ElementList (entry = "domain-object-context", inline = true, required=false)
    private List<DomainObjectContextConfig> domainObjectContext;

    @ElementList (entry = "action", inline = true)
    private List<ActionContextActionConfig> action;

    @Override
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

    public List<ActionContextActionConfig> getAction() {
        return action;
    }

    public void setAction(List<ActionContextActionConfig> action) {
        this.action = action;
    }


}

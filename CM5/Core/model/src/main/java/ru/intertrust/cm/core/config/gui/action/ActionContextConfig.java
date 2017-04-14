package ru.intertrust.cm.core.config.gui.action;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.config.base.TopLevelConfig;
import ru.intertrust.cm.core.config.gui.DomainObjectContextConfig;

import java.util.List;

@Root(name="action-context")
public class ActionContextConfig implements TopLevelConfig{
    private static final long serialVersionUID = 7623941745498114136L;

    @Attribute(required = true)
    private String name;

    @Attribute(name = "replace", required = false)
    private String replacementPolicy;

    @ElementList (entry = "domain-object-context", inline = true, required=false)
    private List<DomainObjectContextConfig> domainObjectContext;

    @ElementList (entry = "action", inline = true)
    private List<ActionContextActionConfig> action;

    @Override
    public String getName() {
        return name;
    }

    @Override
    public ExtensionPolicy getReplacementPolicy() {
        return ExtensionPolicy.fromString(replacementPolicy);
    }

    @Override
    public ExtensionPolicy getCreationPolicy() {
        return ExtensionPolicy.Runtime;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ActionContextConfig that = (ActionContextConfig) o;

        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (replacementPolicy != null ? !replacementPolicy.equals(that.replacementPolicy) : that.replacementPolicy != null) return false;
        if (domainObjectContext != null ? !domainObjectContext.equals(that.domainObjectContext) : that.domainObjectContext != null)
            return false;
        if (action != null ? !action.equals(that.action) : that.action != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }
}

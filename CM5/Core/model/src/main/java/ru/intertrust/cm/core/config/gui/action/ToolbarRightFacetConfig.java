package ru.intertrust.cm.core.config.gui.action;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.ElementListUnion;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * @author Sergey.Okolot
 *         Created on 15.04.2014 12:35.
 */
@Element(name = "facet")
public class ToolbarRightFacetConfig implements Serializable {

    @Attribute(name = "name")
    private String name;

    @ElementListUnion({
            @ElementList(entry = "action", type = ActionConfig.class, inline = true, required = false),
            @ElementList(entry = "simple-action", type = SimpleActionConfig.class, inline = true, required = false),
            @ElementList(entry = "action-ref", type = ActionRefConfig.class, inline = true, required = false),
            @ElementList(entry = "action-separator", type = ActionSeparatorConfig.class, inline = true, required = false)
    })
    private List<AbstractActionConfig> actions;

    public String getName() {
        return name;
    }

    public List<AbstractActionConfig> getActions() {
        return actions == null ? Collections.EMPTY_LIST : actions;
    }

    public void setActions(List<AbstractActionConfig> actions) {
        this.actions = actions;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ToolbarRightFacetConfig that = (ToolbarRightFacetConfig) o;

        if (actions != null ? !actions.equals(that.actions) : that.actions != null) {
            return false;
        }
        if (name != null ? !name.equals(that.name) : that.name != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (actions != null ? actions.hashCode() : 0);
        return result;
    }
}

package ru.intertrust.cm.core.config.gui.action;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.ElementListUnion;

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
}

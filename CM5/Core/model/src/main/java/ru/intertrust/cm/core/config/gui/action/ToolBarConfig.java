package ru.intertrust.cm.core.config.gui.action;

import java.util.Collections;
import java.util.List;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.ElementListUnion;
import org.simpleframework.xml.Root;

import ru.intertrust.cm.core.config.base.TopLevelConfig;

/**
 * @author Sergey.Okolot
 *         Created on 14.04.2014 12:07.
 */
@Root(name = "tool-bar")
public class ToolBarConfig extends BaseAttributeConfig implements TopLevelConfig {

    @Attribute(name = "componentName", required = false)
    private String componentName = "action.tool.bar";

    @Attribute(name = "plugin", required = false)
    private String plugin;

    @Attribute(name = "useDefault", required = false)
    private boolean useDefault = true;

    @ElementListUnion({
            @ElementList(entry = "navigable-action", type = ActionEntryConfig.class, inline = true, required = false),
            @ElementList(entry = "perform-action", type = ActionEntryConfig.class, inline = true, required = false),
            @ElementList(entry = "workflow-action", type = ActionEntryConfig.class, inline = true, required = false),
            @ElementList(entry = "action-ref", type = ActionRefConfig.class, inline = true, required = false),
            @ElementList(entry = "action-separator", type = ActionSeparatorConfig.class, inline = true, required = false)
    })
    private List<AbstractActionEntryConfig> actions;

    @Element(name = "facet", required = false)
    private ToolbarRightFacetConfig rightFacetConfig;

    public String getComponentName() {
        return componentName;
    }

    public String getPlugin() {
        return plugin;
    }

    public boolean isUseDefault() {
        return useDefault;
    }

    public List<AbstractActionEntryConfig> getActions() {
        return actions == null ? Collections.EMPTY_LIST : actions;
    }

    public ToolbarRightFacetConfig getRightFacetConfig() {
        return rightFacetConfig == null ? new ToolbarRightFacetConfig() : rightFacetConfig;
    }

    @Override
    public String getName() {
        return getId();
    }
}

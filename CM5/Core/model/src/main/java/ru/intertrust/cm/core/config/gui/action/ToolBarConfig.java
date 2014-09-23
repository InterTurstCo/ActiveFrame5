package ru.intertrust.cm.core.config.gui.action;

import org.simpleframework.xml.*;
import ru.intertrust.cm.core.config.base.TopLevelConfig;

import java.util.Collections;
import java.util.List;

/**
 * @author Sergey.Okolot
 *         Created on 14.04.2014 12:07.
 */
@Root(name = "tool-bar")
public class ToolBarConfig extends BaseAttributeConfig implements TopLevelConfig {

    @Attribute(name = "name", required = false)
    private String name;

    @Attribute(name = "componentName", required = false)
    private String componentName = "action.tool.bar";

    @Attribute(name = "plugin", required = false)
    private String plugin;

    @Attribute(name = "useDefault", required = false)
    private boolean useDefault = true;

    @ElementListUnion({
            @ElementList(entry = "action", type = ActionConfig.class, inline = true, required = false),
            @ElementList(entry = "simple-action", type = SimpleActionConfig.class, inline = true, required = false),
            @ElementList(entry = "action-ref", type = ActionRefConfig.class, inline = true, required = false),
            @ElementList(entry = "action-separator", type = ActionSeparatorConfig.class, inline = true, required = false)
    })
    private List<AbstractActionConfig> actions;

    @Element(name = "facet", required = false)
    private ToolbarRightFacetConfig rightFacetConfig;

    public String getComponentName() {
        return componentName;
    }

    public void setComponentName(String componentName) {
        this.componentName = componentName;
    }

    public String getPlugin() {
        return plugin;
    }

    public void setPlugin(String plugin) {
        this.plugin = plugin;
    }

    public boolean isUseDefault() {
        return useDefault;
    }

    public List<AbstractActionConfig> getActions() {
        return actions == null ? Collections.EMPTY_LIST : actions;
    }

    public void setActions(List<AbstractActionConfig> actions) {
        this.actions = actions;
    }

    public ToolbarRightFacetConfig getRightFacetConfig() {
        return rightFacetConfig == null ? new ToolbarRightFacetConfig() : rightFacetConfig;
    }

    public void setRightFacetConfig(ToolbarRightFacetConfig rightFacetConfig) {
        this.rightFacetConfig = rightFacetConfig;
    }

    public boolean isRendered() {
        return Boolean.valueOf(getRendered());
    }

    @Override
    public String getName() {
        return name == null ?  getId() : name;
    }
}

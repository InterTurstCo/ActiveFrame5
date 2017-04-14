package ru.intertrust.cm.core.config.gui.action;

import org.simpleframework.xml.*;
import ru.intertrust.cm.core.config.base.LocalizableConfig;

import java.util.Collections;
import java.util.List;

/**
 * @author Sergey.Okolot
 *         Created on 14.04.2014 12:07.
 */
@Root(name = "tool-bar")
public class ToolBarConfig extends BaseAttributeConfig implements LocalizableConfig {

    @Attribute(name = "name", required = false)
    private String name;

    @Attribute(name = "componentName", required = false)
    private String componentName = "action.tool.bar";

    @Attribute(name = "plugin", required = false)
    private String plugin;

    @Attribute(name = "replace", required = false)
    private String replacementPolicy;

    @Attribute(name = "useDefault", required = false)
    private boolean useDefault = true;

    @ElementListUnion({
            @ElementList(entry = "action", type = ActionConfig.class, inline = true, required = false),
            @ElementList(entry = "simple-action", type = SimpleActionConfig.class, inline = true, required = false),
            @ElementList(entry = "action-ref", type = ActionRefConfig.class, inline = true, required = false),
            @ElementList(entry = "action-separator", type = ActionSeparatorConfig.class, inline = true, required = false),
            @ElementList(entry = "action-group", type = ActionGroupConfig.class, inline = true, required = false),
            @ElementList(entry = "action-group-ref", type = ActionGroupRefConfig.class, inline = true, required = false)
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
        return plugin;
    }

    @Override
    public ExtensionPolicy getReplacementPolicy() {
        return ExtensionPolicy.fromString(replacementPolicy);
    }

    @Override
    public ExtensionPolicy getCreationPolicy() {
        return ExtensionPolicy.Runtime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ToolBarConfig that = (ToolBarConfig) o;

        if (useDefault != that.useDefault) {
            return false;
        }
        if (actions != null ? !actions.equals(that.actions) : that.actions != null) {
            return false;
        }
        if (componentName != null ? !componentName.equals(that.componentName) : that.componentName != null) {
            return false;
        }
        if (name != null ? !name.equals(that.name) : that.name != null) {
            return false;
        }
        if (replacementPolicy != null ? !replacementPolicy.equals(that.replacementPolicy) : that.replacementPolicy != null) {
            return false;
        }
        if (plugin != null ? !plugin.equals(that.plugin) : that.plugin != null) {
            return false;
        }
        if (rightFacetConfig != null ? !rightFacetConfig.equals(that.rightFacetConfig) : that.rightFacetConfig !=
                null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (componentName != null ? componentName.hashCode() : 0);
        result = 31 * result + (plugin != null ? plugin.hashCode() : 0);
        result = 31 * result + (useDefault ? 1 : 0);
        result = 31 * result + (actions != null ? actions.hashCode() : 0);
        result = 31 * result + (rightFacetConfig != null ? rightFacetConfig.hashCode() : 0);
        return result;
    }
}

package ru.intertrust.cm.core.config.gui.action;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.ElementListUnion;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.convert.Convert;
import ru.intertrust.cm.core.config.base.Localizable;
import ru.intertrust.cm.core.config.base.LocalizableConfig;
import ru.intertrust.cm.core.config.converter.ActionDisplayTypeConverter;

import java.util.Collections;
import java.util.List;


@Root(name="action-group")
public class ActionGroupConfig extends BaseActionConfig implements LocalizableConfig {

    @Attribute(required = false)
    private int weight;

    @Attribute(required = false)
    private boolean merged = true;

    @Attribute(required = false)
    private boolean disabled = false;

    @Attribute(name = "display-empty-groups",required = false)
    private boolean displayEmptyGroups = false;

    @Attribute(name = "name", required = false)
    private String name;

    @Attribute(name = "replace", required = false)
    private String replacementPolicy;

    @Attribute(required = false)
    @Localizable
    private String text;

    @Attribute(name = "image", required = false)
    private String imageUrl;

    @Attribute(name = "imageClass", required = false)
    private String imageClass;

    @Attribute(required = false)
    @Localizable
    private String tooltip;

    @Attribute(required = false)
    @Convert(ActionDisplayTypeConverter.class)
    private ActionDisplayType display = ActionDisplayType.button;

    @Attribute(required = false)
    private boolean dirtySensitivity = true;

    @ElementListUnion({
            @ElementList(entry = "action", type = ActionConfig.class, inline = true, required = false),
            @ElementList(entry = "simple-action", type = SimpleActionConfig.class, inline = true, required = false),
            @ElementList(entry = "action-ref", type = ActionRefConfig.class, inline = true, required = false),
            @ElementList(entry = "action-separator", type = ActionSeparatorConfig.class, inline = true, required = false),
            @ElementList(entry = "action-group", type = ActionGroupConfig.class, inline = true, required = false),
            @ElementList(entry = "action-group-ref", type = ActionGroupRefConfig.class, inline = true, required = false),
            @ElementList(entry = "workflow-actions", type = WorkflowActionsConfig.class, inline = true, required = false)
    })
    private List<AbstractActionConfig> children;

    /**
     * Default constructor to support GWT serialization
     */
    public ActionGroupConfig() {}

    @Override
    public int getWeight() {
        return weight;
    }

    @Override
    public boolean isMerged() {
        return merged;
    }

    public void setMerged(boolean merged) {
        this.merged = merged;
    }

    public boolean isDisabled() {
        return disabled;
    }

    @Override
    public String getName() {
        return name == null || name.isEmpty() ? null : name;
    }

    @Override
    public ExtensionPolicy getReplacementPolicy() {
        return ExtensionPolicy.fromString(replacementPolicy);
    }

    @Override
    public ExtensionPolicy getCreationPolicy() {
        return ExtensionPolicy.Runtime;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(final String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getImageClass() {
        return imageClass;
    }

    public void setImageClass(String imageClass) {
        this.imageClass = imageClass;
    }

    public String getTooltip() {
        return tooltip == null ? text : tooltip;
    }

    public void setTooltip(String tooltip) {
        this.tooltip = tooltip;
    }

    public ActionDisplayType getDisplay() {
        return display;
    }

    public void setDisplay(ActionDisplayType display) {
        this.display = display;
    }

    public boolean isDirtySensitivity() {
        return dirtySensitivity;
    }

    public void setDirtySensitivity(boolean dirtySensitivity) {
        this.dirtySensitivity = dirtySensitivity;
    }

    public boolean isDisplayEmptyGroups() {
        return displayEmptyGroups;
    }

    public void setDisplayEmptyGroups(boolean displayEmptyGroups) {
        this.displayEmptyGroups = displayEmptyGroups;
    }

    public List<AbstractActionConfig> getChildren() {
        return children == null ? Collections.EMPTY_LIST : children;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        ActionGroupConfig that = (ActionGroupConfig) o;

        if (weight != that.weight) return false;
        if (merged != that.merged) return false;
        if (disabled != that.disabled) return false;
        if (displayEmptyGroups != that.displayEmptyGroups) return false;
        if (dirtySensitivity != that.dirtySensitivity) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (replacementPolicy != null ? !replacementPolicy.equals(that.replacementPolicy) : that.replacementPolicy != null) return false;
        if (text != null ? !text.equals(that.text) : that.text != null) return false;
        if (imageUrl != null ? !imageUrl.equals(that.imageUrl) : that.imageUrl != null) return false;
        if (imageClass != null ? !imageClass.equals(that.imageClass) : that.imageClass != null) return false;
        if (tooltip != null ? !tooltip.equals(that.tooltip) : that.tooltip != null) return false;
        if (display != that.display) return false;
        if (children != null ? !children.equals(that.children) : that.children != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }
}

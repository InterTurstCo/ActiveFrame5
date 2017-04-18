package ru.intertrust.cm.core.config.gui.action;

import org.simpleframework.xml.*;
import org.simpleframework.xml.convert.Convert;
import ru.intertrust.cm.core.config.base.Localizable;
import ru.intertrust.cm.core.config.base.LocalizableConfig;
import ru.intertrust.cm.core.config.converter.ActionDisplayTypeConverter;
import ru.intertrust.cm.core.config.converter.ActionTypeConverter;

import java.util.Collections;
import java.util.List;

/**
 * @author Denis Mitavskiy
 *         Date: 22.08.13
 *         Time: 16:02
 */
@Root(name="action")
public class ActionConfig extends BaseActionConfig implements LocalizableConfig {

    @Element(name = "before-execution", required = false)
    private BeforeActionExecutionConfig beforeConfig;

    @Element(name = "after-execution", required = false)
    private AfterActionExecutionConfig afterConfig;

    @Element(name="action-settings", required = false)
    @Convert(ActionSettingsConverter.class)
    private ActionSettings actionSettings;

    @Attribute(required = false)
    private int weight;

    @Attribute(required = false)
    private boolean merged = true;

    @Attribute(required = false)
    private boolean disabled = false;

    @Attribute(name = "name", required = false)
    private String name;

    @Attribute(name = "replace", required = false)
    private String replacementPolicy;

    @Attribute(name = "componentName")
    private String componentName;

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
    private boolean immediate = false;

    @Attribute(required = false)
    private String action;

    @Attribute(required = false)
    @Convert(ActionDisplayTypeConverter.class)
    private ActionDisplayType display = ActionDisplayType.button;

    @Attribute(required = false)
    @Convert(ActionTypeConverter.class)
    private ActionType type = ActionType.perform;

    @Attribute(required = false)
    private String groupId;

    @Attribute(required = false)
    private boolean dirtySensitivity = true;

    @ElementListUnion({
            @ElementList(entry = "action", type = ActionConfig.class, inline = true, required = false),
            @ElementList(entry = "action-ref", type = ActionRefConfig.class, inline = true, required = false),
            @ElementList(entry = "action-separator", type = ActionSeparatorConfig.class, inline = true, required = false)
    })
    private List<AbstractActionConfig> children;

    /**
     * Default constructor to support GWT serialization
     */
    public ActionConfig() {}

    public ActionConfig(final String componentName) {
        this(componentName, null);
    }

    public ActionConfig(final String componentName, final String name) {
        this.componentName = componentName;
        this.name = name;
    }

    public BeforeActionExecutionConfig getBeforeConfig() {
        return beforeConfig;
    }

    public AfterActionExecutionConfig getAfterConfig() {
        return afterConfig;
    }

    public ActionSettings getActionSettings() {
        return actionSettings;
    }

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

    public String getComponentName() {
        return componentName;
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

    public boolean isImmediate() {
        return immediate;
    }

    public void setImmediate(boolean immediate) {
        this.immediate = immediate;
    }

    public String getAction() {
        return action;
    }

    public ActionDisplayType getDisplay() {
        return display;
    }

    public void setDisplay(ActionDisplayType display) {
        this.display = display;
    }

    public ActionType getType() {
        return type;
    }

    public String getGroupId() {
        return groupId;
    }

    public boolean isDirtySensitivity() {
        return dirtySensitivity;
    }

    public void setDirtySensitivity(boolean dirtySensitivity) {
        this.dirtySensitivity = dirtySensitivity;
    }

    public List<AbstractActionConfig> getChildren() {
        return children == null ? Collections.EMPTY_LIST : children;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        ActionConfig that = (ActionConfig) o;

        if (weight != that.weight) return false;
        if (merged != that.merged) return false;
        if (disabled != that.disabled) return false;
        if (immediate != that.immediate) return false;
        if (dirtySensitivity != that.dirtySensitivity) return false;
        if (replacementPolicy != null ? !replacementPolicy.equals(that.replacementPolicy) : that.replacementPolicy != null) return false;
        if (beforeConfig != null ? !beforeConfig.equals(that.beforeConfig) : that.beforeConfig != null) return false;
        if (afterConfig != null ? !afterConfig.equals(that.afterConfig) : that.afterConfig != null) return false;
        if (actionSettings != null ? !actionSettings.equals(that.actionSettings) : that.actionSettings != null)
            return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (componentName != null ? !componentName.equals(that.componentName) : that.componentName != null)
            return false;
        if (text != null ? !text.equals(that.text) : that.text != null) return false;
        if (imageUrl != null ? !imageUrl.equals(that.imageUrl) : that.imageUrl != null) return false;
        if (imageClass != null ? !imageClass.equals(that.imageClass) : that.imageClass != null) return false;
        if (tooltip != null ? !tooltip.equals(that.tooltip) : that.tooltip != null) return false;
        if (action != null ? !action.equals(that.action) : that.action != null) return false;
        if (display != that.display) return false;
        if (type != that.type) return false;
        if (groupId != null ? !groupId.equals(that.groupId) : that.groupId != null) return false;
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

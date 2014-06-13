package ru.intertrust.cm.core.config.gui.action;

import org.simpleframework.xml.*;
import org.simpleframework.xml.convert.Convert;
import ru.intertrust.cm.core.config.base.TopLevelConfig;
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
public class ActionConfig extends AbstractActionConfig implements TopLevelConfig {

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
    private boolean merged = false;

    @Attribute(required = false)
    private boolean disabled = false;

    @Attribute(name = "name", required = false)
    private String name;

    @Attribute(name = "componentName")
    private String componentName;

    @Attribute(required = false)
    private String text;

    @Attribute(name = "image", required = false)
    private String imageUrl;

    @Attribute(required = false)
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

    public boolean isDisabled() {
        return disabled;
    }

    @Override
    public String getName() {
        return name == null ? getId() : name;
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

    public String getTooltip() {
        return tooltip == null ? text : tooltip;
    }

    public void setTooltip(String tooltip) {
        this.tooltip = tooltip;
    }

    public boolean isImmediate() {
        return immediate;
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

    public List<AbstractActionConfig> getChildren() {
        return children == null ? Collections.EMPTY_LIST : children;
    }
}

package ru.intertrust.cm.core.config.gui.action;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.ElementListUnion;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.convert.Convert;
import org.simpleframework.xml.core.Commit;

import ru.intertrust.cm.core.config.base.TopLevelConfig;
import ru.intertrust.cm.core.config.converter.ActionDisplayTypeConverter;
import ru.intertrust.cm.core.config.converter.ActionTypeConverter;

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
    private boolean merged = true;

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

    @Attribute(name = "imageClass", required = false)
    private String imageClass;

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

    @Attribute(name = "visible-when-new", required = false)
    private boolean visibleWhenNew = true;

    @Attribute(name = "visibility-state-condition", required = false)
    private String visibilityStateCondition;

    @Attribute(name = "visibility-checker", required = false)
    private String visibilityChecker;

    @ElementListUnion({
            @ElementList(entry = "action", type = ActionConfig.class, inline = true, required = false),
            @ElementList(entry = "action-ref", type = ActionRefConfig.class, inline = true, required = false),
            @ElementList(entry = "action-separator", type = ActionSeparatorConfig.class, inline = true, required = false)
    })
    private List<AbstractActionConfig> children;

    @ElementList(name = "action-params", required = false)
    private List<ActionParamConfig> actionParams;

    private Map<String, String> properties = new HashMap<>();

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

    @Commit
    public void commit() {
        if (actionParams != null && !actionParams.isEmpty()) {
            for (ActionParamConfig param : actionParams) {
                properties.put(param.getName(), param.getValue());
            }
            actionParams = null;
        }
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
        return name == null ? getId() : name;
    }

    public String getComponentName() {
        return componentName;
    }

    public String getActionHandler() {
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

    public boolean isVisibleWhenNew() {
        return visibleWhenNew;
    }

    public void setVisibleWhenNew(boolean visibleWhenNew) {
        this.visibleWhenNew = visibleWhenNew;
    }

    public String getVisibilityStateCondition() {
        return visibilityStateCondition;
    }

    public void setVisibilityStateCondition(String visibilityStateCondition) {
        this.visibilityStateCondition = visibilityStateCondition;
    }

    public String getVisibilityChecker() {
        return visibilityChecker;
    }

    public void setVisibilityChecker(String visibilityChecker) {
        this.visibilityChecker = visibilityChecker;
    }

    public String getProperty(final String key) {
        return properties == null ? null : properties.get(key);
    }

    public Map<String, String> getProperties() {
        return properties;
    }
}

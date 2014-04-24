package ru.intertrust.cm.core.config.gui.action;

import java.util.Collections;
import java.util.List;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.ElementListUnion;
import org.simpleframework.xml.ElementUnion;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.convert.Convert;

import ru.intertrust.cm.core.config.base.TopLevelConfig;
import ru.intertrust.cm.core.config.converter.ActionDisplayTypeConverter;
import ru.intertrust.cm.core.config.converter.ActionTypeConverter;

/**
 * @author Sergey.Okolot
 *         Created on 14.04.2014 17:21.
 */
@Root(name = "action-entry")
public class ActionEntryConfig extends AbstractActionEntryConfig implements TopLevelConfig, Cloneable {

    @Element(name = "before-execution", required = false)
    private BeforeActionExecutionConfig beforeConfig;

    @Element(name = "after-execution", required = false)
    private AfterActionExecutionConfig afterConfig;

    @Attribute(required = false)
    private String id;

    @Attribute(required = false)
    private String style;

    @Attribute(required = false)
    private String styleClass;

    @Attribute(required = false)
    private String addStyleClass;

    @Attribute(required = false)
    private boolean rendered = true;

    @Attribute(required = false)
    private Integer order;

    @Attribute(required = false)
    private boolean merged = false;

    @Element(required = false)
    private boolean disabled = false;

    @Attribute(name = "componentName")
    private String componentName;

    @Attribute(required = false)
    private String text;

    @Attribute(required = false)
    private String image;

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
            @ElementList(entry = "action-entry", type = ActionEntryConfig.class, inline = true, required = false),
            @ElementList(entry = "action-ref", type = ActionRefConfig.class, inline = true, required = false),
            @ElementList(entry = "action-separator", type = ActionSeparatorConfig.class, inline = true, required = false)
    })
    private List<AbstractActionEntryConfig> children;

    public BeforeActionExecutionConfig getBeforeConfig() {
        return beforeConfig;
    }

    public AfterActionExecutionConfig getAfterConfig() {
        return afterConfig;
    }

    @Override
    public String getId() {
        return id;
    }

    public String getStyle() {
        return style;
    }

    public String getStyleClass() {
        return styleClass;
    }

    public String getAddStyleClass() {
        return addStyleClass;
    }

    public boolean isRendered() {
        return rendered;
    }

    @Override
    public Integer getOrder() {
        return order;
    }

    public void setOrder(final Integer order) {
        this.order = order;
    }

    public boolean isMerged() {
        return merged;
    }

    public boolean isDisabled() {
        return disabled;
    }

    public String getComponentName() {
        return componentName;
    }

    public String getText() {
        return text;
    }

    public void clearText() {
        text = null;
    }

    public String getImage() {
        return image;
    }

    public void clearImage() {
        image = null;
    }

    public String getTooltip() {
        return tooltip;
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

    public ActionType getType() {
        return type;
    }

    @Override
    public String getGroupId() {
        return groupId;
    }

    public boolean isDirtySensitivity() {
        return dirtySensitivity;
    }

    public List<AbstractActionEntryConfig> getChildren() {
        return children == null ? Collections.EMPTY_LIST : children;
    }

    @Override
    public String getName() {
        return id;
    }

    @Override
    public ActionEntryConfig clone() {
        try {
            final ActionEntryConfig result = (ActionEntryConfig) super.clone();
            return result;
        } catch (CloneNotSupportedException cnse) {
            throw new RuntimeException(cnse);
        }
    }
}

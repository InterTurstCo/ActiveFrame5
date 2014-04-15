package ru.intertrust.cm.core.config.gui.action;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.ElementListUnion;
import org.simpleframework.xml.ElementUnion;

/**
 * @author Sergey.Okolot
 *         Created on 14.04.2014 17:21.
 */
@ElementUnion({
        @Element(name = "navigable-action"),
        @Element(name = "workflow-action"),
        @Element(name = "perform-action")
})
public class ActionEntryConfig extends AbstractActionEntryConfig {

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
    private boolean immediate = false;

    @Attribute(required = false)
    private String action;

    @Attribute(required = false)
    private String display = "button";

    @Attribute(required = false)
    private String groupId;

    @Attribute(required = false)
    private boolean dirtySensitivity = true;

    @ElementListUnion({
            @ElementList(entry = "navigable-action", type = ActionEntryConfig.class, inline = true, required = false),
            @ElementList(entry = "perform-action", type = ActionEntryConfig.class, inline = true, required = false),
            @ElementList(entry = "workflow-action", type = ActionEntryConfig.class, inline = true, required = false),
            @ElementList(entry = "action-ref", type = ActionRefConfig.class, inline = true, required = false),
            @ElementList(entry = "action-separator", type = ActionSeparatorConfig.class, inline = true, required = false)
    })
    private List<ActionEntryConfig> children;

    public BeforeActionExecutionConfig getBeforeConfig() {
        return beforeConfig;
    }

    public AfterActionExecutionConfig getAfterConfig() {
        return afterConfig;
    }

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

    public Integer getOrder() {
        return order;
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

    public String getImage() {
        return image;
    }

    public boolean isImmediate() {
        return immediate;
    }

    public String getAction() {
        return action;
    }

    public String getDisplay() {
        return display;
    }

    public String getGroupId() {
        return groupId;
    }

    public boolean isDirtySensitivity() {
        return dirtySensitivity;
    }

    public List<ActionEntryConfig> getChildren() {
        return children == null ? Collections.EMPTY_LIST : children;
    }
}

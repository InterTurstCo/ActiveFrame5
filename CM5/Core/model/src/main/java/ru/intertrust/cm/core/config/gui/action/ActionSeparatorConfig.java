package ru.intertrust.cm.core.config.gui.action;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;

/**
 * @author Sergey.Okolot
 *         Created on 15.04.2014 12:00.
 */
@Element(name = "action-separator")
public class ActionSeparatorConfig extends AbstractActionConfig {
    private static final String DEFAULT_COMPONENT_NAME = "action.separator";

    @Attribute(required = false)
    private boolean merged = false;

    @Attribute(name = "componentName", required = false)
    private String componentName;

    @Override
    public boolean isMerged() {
        return merged;
    }

    public String getComponentName() {
        return componentName == null ? DEFAULT_COMPONENT_NAME : componentName;
    }
}

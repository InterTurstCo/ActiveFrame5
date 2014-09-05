package ru.intertrust.cm.core.config.gui.form.widget;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

import ru.intertrust.cm.core.config.gui.action.ActionRefConfig;

/**
 * @author Sergey.Okolot
 *         Created on 04.09.2014 16:14.
 */
@Root(name = ActionExecutorConfig.COMPONENT_NAME)
public class ActionExecutorConfig extends LabelConfig {

    public static final String COMPONENT_NAME = "action-executor";

    @Element(name = "action-ref")
    private ActionRefConfig actionRefConfig;

    public ActionRefConfig getActionRefConfig() {
        return actionRefConfig;
    }

    @Override
    public String getComponentName() {
        return COMPONENT_NAME;
    }
}

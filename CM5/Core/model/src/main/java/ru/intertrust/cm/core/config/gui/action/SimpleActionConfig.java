package ru.intertrust.cm.core.config.gui.action;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

/**
 * @author Sergey.Okolot
 *         Created on 22.09.2014 13:19.
 */
@Root(name = "simple-action")
public class SimpleActionConfig extends ActionConfig {

    @Attribute(name = "action-handler")
    private String actionHandler;

    public String getActionHandler() {
        return actionHandler;
    }
}

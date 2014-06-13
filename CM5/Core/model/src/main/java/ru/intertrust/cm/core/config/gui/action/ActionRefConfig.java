package ru.intertrust.cm.core.config.gui.action;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;

/**
 * @author Sergey.Okolot
 *         Created on 15.04.2014 12:28.
 */
@Element(name = "action-ref")
public class ActionRefConfig extends AbstractActionConfig {

    @Attribute(name = "actionId")
    private String actionId;

    @Attribute(name = "showText", required = false)
    private boolean showText = true;

    @Attribute(name = "showImage", required = false)
    private boolean showImage = true;

    @Attribute(name = "merged", required = false)
    private Boolean merged;

    public String getActionId() {
        return actionId;
    }

    public boolean isShowText() {
        return showText;
    }

    public boolean isShowImage() {
        return showImage;
    }

    public String getRendered() {
        return rendered;
    }

    public Boolean getMerged() {
        return merged;
    }

    @Override
    public String toString() {
        return new StringBuilder(ActionRefConfig.class.getSimpleName())
                .append(": actionId=").append(actionId)
                .toString();
    }
}

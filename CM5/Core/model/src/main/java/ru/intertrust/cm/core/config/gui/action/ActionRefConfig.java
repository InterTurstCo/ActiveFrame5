package ru.intertrust.cm.core.config.gui.action;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;

/**
 * @author Sergey.Okolot
 *         Created on 15.04.2014 12:28.
 */
@Element(name = "action-ref")
public class ActionRefConfig extends AbstractActionEntryConfig {

    @Attribute(name = "actionId")
    private String actionId;

    @Attribute(name = "showText", required = false)
    private boolean showText = true;

    @Attribute(name = "showImage", required = false)
    private boolean showImage = true;


    public String getActionId() {
        return actionId;
    }

    public void setActionId(String actionId) {
        this.actionId = actionId;
    }

    public boolean isShowText() {
        return showText;
    }

    public void setShowText(boolean showText) {
        this.showText = showText;
    }

    public boolean isShowImage() {
        return showImage;
    }

    public void setShowImage(boolean showImage) {
        this.showImage = showImage;
    }
}

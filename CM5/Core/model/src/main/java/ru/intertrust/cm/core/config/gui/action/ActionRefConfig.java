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

    @Attribute(name = "visible-when-new", required = false)
    private boolean visibleWhenNew = true;

    @Attribute(name = "visibility-state-condition", required = false)
    private String visibilityStateCondition;

    @Attribute(name = "visibility-checker", required = false)
    private String visibilityChecker;

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

    @Override
    public String toString() {
        return new StringBuilder(ActionRefConfig.class.getSimpleName())
                .append(": actionId=").append(actionId)
                .toString();
    }
}

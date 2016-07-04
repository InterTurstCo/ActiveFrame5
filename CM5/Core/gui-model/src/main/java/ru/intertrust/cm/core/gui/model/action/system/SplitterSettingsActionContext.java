package ru.intertrust.cm.core.gui.model.action.system;

import ru.intertrust.cm.core.gui.model.action.ActionContext;

/**
 * @author Sergey.Okolot
 *         Created on 01.08.2014 16:44.
 */
public class SplitterSettingsActionContext extends ActionContext {
    public static final String COMPONENT_NAME = "splitter.settings.action";

    /**
     * 0 - горизонтальная, 1 - вертикальная
     */
    private Long orientation;

    private Long position;

    private Boolean customDrag = false;

    public Long getOrientation() {
        return orientation;
    }

    public void setOrientation(Long orientation) {
        this.orientation = orientation;
    }

    public Long getPosition() {
        return position;
    }

    public void setPosition(Long position) {
        this.position = position;
    }

    public Boolean isCustomDrag() {
        return customDrag;
    }

    public void setCustomDrag(Boolean customDrag) {
        this.customDrag = customDrag;
    }
}

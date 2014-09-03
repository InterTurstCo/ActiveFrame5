package ru.intertrust.cm.core.gui.impl.server.action;

import ru.intertrust.cm.core.gui.api.server.action.ActionHandler;

import java.util.Map;

/**
 * @author Sergey.Okolot
 *         Created on 12.06.2014 16:09.
 */
public class FormPluginHandlerStatusData implements ActionHandler.HandlerStatusData {
    private boolean preview;
    private boolean toggleEdit;
    private boolean newDomainObject;

    @Override
    public void initialize(Map<String, Object> params) {
        preview = Boolean.TRUE.equals(params.get("preview"));
        toggleEdit = Boolean.TRUE.equals(params.get(ActionHandler.TOGGLE_EDIT_KEY));
        newDomainObject = Boolean.TRUE.equals(params.get("isNewDomainObject"));
    }

    public boolean isPreview() {
        return preview;
    }

    public boolean isToggleEdit() {
        return toggleEdit;
    }

    @Override
    public boolean isNewDomainObject() {
        return newDomainObject;
    }
}

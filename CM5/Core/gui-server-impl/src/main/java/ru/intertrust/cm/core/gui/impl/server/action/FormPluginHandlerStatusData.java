package ru.intertrust.cm.core.gui.impl.server.action;

import java.util.Map;

import ru.intertrust.cm.core.gui.api.server.action.ActionHandler;

/**
 * @author Sergey.Okolot
 *         Created on 12.06.2014 16:09.
 */
public class FormPluginHandlerStatusData implements ActionHandler.HandlerStatusData {
    public static final String PLUGIN_IN_CENTRAL_PANEL_ATTR = "pluginIsCentralPanel";
    public static final String TOGGLE_EDIT_ATTR = "toggleEdit";
    public static final String PREVIEW_ATTR = "preview";

    private Map<String, Object> params;


    @Override
    public void initialize(Map<String, Object> params) {
        this.params = params;
    }

    @Override
    public Object getParameter(String key) {
        return params == null ? null : params.get(key);
    }

    public boolean isPreview() {
        return Boolean.TRUE.equals(params.get(PREVIEW_ATTR));
    }

    public boolean isToggleEdit() {
        return Boolean.TRUE.equals(params.get(TOGGLE_EDIT_ATTR));
    }
}

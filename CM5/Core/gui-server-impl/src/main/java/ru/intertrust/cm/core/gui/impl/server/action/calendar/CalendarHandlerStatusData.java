package ru.intertrust.cm.core.gui.impl.server.action.calendar;

import java.util.Map;

import ru.intertrust.cm.core.gui.api.server.action.ActionHandler;

/**
 * @author Sergey.Okolot
 *         Created on 27.10.2014 17:41.
 */
public class CalendarHandlerStatusData implements ActionHandler.HandlerStatusData {
    public static final String MODE_KEY = "mode";

    private Map<String, Object> params;

    public String getMode() {
        return (String) getParameter(MODE_KEY);
    }

    @Override
    public void initialize(Map<String, Object> params) {
        this.params = params;
    }

    @Override
    public Object getParameter(String key) {
        return params == null ? null : params.get(key);
    }
}

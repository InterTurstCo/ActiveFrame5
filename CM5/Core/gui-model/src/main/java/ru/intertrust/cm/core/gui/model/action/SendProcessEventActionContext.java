package ru.intertrust.cm.core.gui.model.action;

import ru.intertrust.cm.core.config.gui.action.ActionConfig;

/**
 * @author Denis Mitavskiy
 *         Date: 24.10.13
 *         Time: 14:24
 */
public class SendProcessEventActionContext extends SaveActionContext {
    private static final long serialVersionUID = 8399776879911036877L;

    public SendProcessEventActionContext() {
    }

    public SendProcessEventActionContext(ActionConfig actionConfig) {
        super(actionConfig);
    }
}

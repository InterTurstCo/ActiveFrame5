package ru.intertrust.cm.core.gui.impl.server.action;

import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.ProfileService;
import ru.intertrust.cm.core.config.gui.action.ActionConfig;
import ru.intertrust.cm.core.gui.api.server.action.ActionHandler;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.action.SimpleActionContext;
import ru.intertrust.cm.core.gui.model.action.SimpleActionData;

/**
 * @author Sergey.Okolot
 *         Created on 23.09.2014 11:50.
 */
@ComponentName(SimpleActionContext.COMPONENT_NAME)
public class SimpleActionHandler extends ActionHandler<SimpleActionContext, SimpleActionData> {

    @Autowired
    private ProfileService profileService;

    @Override
    public SimpleActionData executeAction(SimpleActionContext context) {
        return guiService.executeSimpleAction(context);
    }

    @Override
    public SimpleActionContext getActionContext(ActionConfig actionConfig) {
        return new SimpleActionContext(actionConfig);
    }

    @Override
    public HandlerStatusData getCheckStatusData() {
        return new FormPluginHandlerStatusData();
    }
}

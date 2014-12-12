package ru.intertrust.cm.core.gui.impl.server.action;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.intertrust.cm.core.config.gui.action.ActionConfig;
import ru.intertrust.cm.core.gui.api.server.action.ActionHandler;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.action.ActionContext;
import ru.intertrust.cm.core.gui.model.action.CreateNewRegionActionData;

@ComponentName("create.new.region.action")
public class CreateNewRegionHandler extends ActionHandler<ActionContext, CreateNewRegionActionData> {

    private static Logger logger = LoggerFactory.getLogger(CreateNewRegionHandler.class);

    @Override
    public CreateNewRegionActionData executeAction(ActionContext context) {
//        CreateNewRegionActionData actionData = new CreateNewRegionActionData();
        /* Какие нибудь полезные действия */
//        logger.info("Call create.new.region.action");
//        return actionData;
        return null;
    }

    @Override
    public ActionContext getActionContext(final ActionConfig actionConfig) {
        return new ActionContext(actionConfig);
    }

    @Override
    public HandlerStatusData getCheckStatusData() {
        return new FormPluginHandlerStatusData();
    }

}

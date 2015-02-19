package ru.intertrust.cm.core.gui.impl.server.action;

import ru.intertrust.cm.core.config.gui.action.ActionConfig;
import ru.intertrust.cm.core.gui.api.server.action.ActionHandler;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.action.ActionContext;
import ru.intertrust.cm.core.gui.model.action.ActionData;
import ru.intertrust.cm.core.gui.model.action.CompleteTaskActionContext;

@ComponentName("digital.signature.action")
public class DigitalSignatureActionHandler extends ActionHandler<CompleteTaskActionContext, ActionData>{

    @Override
    public ActionData executeAction(CompleteTaskActionContext context) {
        // TODO Auto-generated method stub
        return null;
    }
    
    @Override
    public ActionContext getActionContext(final ActionConfig actionConfig) {
        return new ActionContext(actionConfig);
    }    
}

package ru.intertrust.cm.core.gui.impl.server.action;

import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.ProcessService;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.gui.api.server.action.ActionHandler;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.GuiException;
import ru.intertrust.cm.core.gui.model.action.ActionContext;
import ru.intertrust.cm.core.gui.model.action.ActionData;
import ru.intertrust.cm.core.gui.model.action.CompleteTaskActionContext;

/**
 * @author Denis Mitavskiy
 *         Date: 23.10.13
 *         Time: 15:18
 */
@ComponentName("complete.task.action")
public class CompleteTaskActionHandler extends ActionHandler<CompleteTaskActionContext, ActionData> {

    @Autowired
    private ProcessService processservice;

    @Override
    public ActionData executeAction(CompleteTaskActionContext completeTaskActionContext) {
        Id domainObjectId = completeTaskActionContext.getRootObjectId();
        if (domainObjectId == null) {
            throw new GuiException("Объект ещё не сохранён");
        }

        // todo: do some action with this domain object or with new domain
        // object
        processservice.completeTask(completeTaskActionContext.getTaskId(), null, completeTaskActionContext.getTaskAction());
        
        return null;
    }

    @Override
    public CompleteTaskActionContext getActionContext() {
        return new CompleteTaskActionContext();
    }
}

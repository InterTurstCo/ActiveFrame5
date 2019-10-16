package ru.intertrust.cm.nbrbase.gui.actions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.dao.api.EventLogService;
import ru.intertrust.cm.core.gui.api.server.action.ActionHandler;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.GuiException;
import ru.intertrust.cm.core.gui.model.action.SimpleActionContext;
import ru.intertrust.cm.core.gui.model.action.SimpleActionData;

@ComponentName("clean.audit.log.action")
public class CleanAuditLogsActionHandler extends ActionHandler<SimpleActionContext, SimpleActionData> {

    @Autowired
    EventLogService eventLogService;

    protected static Logger log = LoggerFactory.getLogger(CleanAuditLogsActionHandler.class);
    @Override
    public SimpleActionData executeAction(SimpleActionContext context) {

        try {
            eventLogService.clearEventLogs();
        } catch (Exception ex) {
            throw new GuiException(ex.getMessage());
        }

        SimpleActionData actionData = new SimpleActionData();
        actionData.setDeleteAction(true);
        actionData.setDeletedObject(context.getRootObjectId());
        actionData.setOnSuccessMessage("Удаление выполнено");
        return actionData;
    }

}

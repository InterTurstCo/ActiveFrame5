package ru.intertrust.cm.core.gui.impl.server.action;

import org.springframework.beans.factory.annotation.Autowired;

import ru.intertrust.cm.core.business.api.ProcessService;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.impl.RdbmsId;
import ru.intertrust.cm.core.config.gui.action.ActionConfig;
import ru.intertrust.cm.core.gui.api.server.action.ActionHandler;
import ru.intertrust.cm.core.gui.impl.server.util.PluginHandlerHelper;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.GuiException;
import ru.intertrust.cm.core.gui.model.action.SimpleActionContext;
import ru.intertrust.cm.core.gui.model.action.SimpleActionData;

/**
 * @author Sergey.Okolot
 *         Created on 03.11.2014 16:19.
 */
@ComponentName("generic.workflow.action")
public class GenericWorkflowActionHandler
        extends ActionHandler<SimpleActionContext, SimpleActionData> {

    @Autowired private ProcessService processService;

    @Override
    public SimpleActionData executeAction(SimpleActionContext context) {
        final Id domainObjectId = context.getRootObjectId();
        if (domainObjectId == null) {
            throw new GuiException("Объект не сохранён");
        }
        final ActionConfig actionConfig = context.getActionConfig();
        final String processType = actionConfig.getProperty(PluginHandlerHelper.WORKFLOW_PROCESS_TYPE_KEY);
        if (processType == null) {
            throw new GuiException("Не задано тип процесса");
        }
        final String processName = actionConfig.getProperty(PluginHandlerHelper.WORKFLOW_PROCESS_NAME_KEY);
        if (processName == null) {
            throw new GuiException("Не задано имя процесса");
        }
        switch(processType) {
            case "start.process":
                processService.startProcess(processName, domainObjectId, null);
                break;
            case "complete.process":
                processService.completeTask(new RdbmsId(actionConfig.getProperty("complete.task.id")),
                        null,
                        actionConfig.getProperty("complete.task.action"));
                break;
            default:
                new GuiException("Process '" + processType + "' not supported.");
        }
        final SimpleActionData result = new SimpleActionData();
        return result;
    }

    @Override
    public SimpleActionContext getActionContext(ActionConfig actionConfig) {
        return new SimpleActionContext(actionConfig);
    }
}

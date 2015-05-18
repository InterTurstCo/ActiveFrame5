package ru.intertrust.cm.core.gui.impl.server.action;

import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.ProcessService;
import ru.intertrust.cm.core.business.api.ProfileService;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.Pair;
import ru.intertrust.cm.core.business.api.dto.impl.RdbmsId;
import ru.intertrust.cm.core.config.gui.action.ActionConfig;
import ru.intertrust.cm.core.config.localization.LocalizationKeys;
import ru.intertrust.cm.core.config.localization.MessageResourceProvider;
import ru.intertrust.cm.core.gui.api.server.action.ActionHandler;
import ru.intertrust.cm.core.gui.impl.server.util.PluginHandlerHelper;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.GuiException;
import ru.intertrust.cm.core.gui.model.action.SimpleActionContext;
import ru.intertrust.cm.core.gui.model.action.SimpleActionData;
import ru.intertrust.cm.core.gui.model.util.PlaceholderResolver;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Sergey.Okolot
 *         Created on 03.11.2014 16:19.
 */
@ComponentName("generic.workflow.action")
public class GenericWorkflowActionHandler
        extends ActionHandler<SimpleActionContext, SimpleActionData> {

    @Autowired private ProcessService processService;

    @Autowired private ProfileService profileService;

    @Override
    public SimpleActionData executeAction(SimpleActionContext context) {
        final Id domainObjectId = context.getRootObjectId();
        if (domainObjectId == null) {
            throw new GuiException(buildMessage(LocalizationKeys.GUI_EXCEPTION_OBJECT_NOT_SAVED,
                    "Объект ещё не сохранён"));
        }
        final ActionConfig actionConfig = context.getActionConfig();
        final String processType = actionConfig.getProperty(PluginHandlerHelper.WORKFLOW_PROCESS_TYPE_KEY);
        if (processType == null) {
            throw new GuiException(buildMessage(LocalizationKeys.GUI_EXCEPTION_NO_PROCESS_TYPE,
                    "Не задано тип процесса"));
        }
        final String processName = actionConfig.getProperty(PluginHandlerHelper.WORKFLOW_PROCESS_NAME_KEY);
        if (processName == null && (processType.equalsIgnoreCase("start.process") || processType.equalsIgnoreCase("send.event"))) {
            throw new GuiException(buildMessage(LocalizationKeys.GUI_EXCEPTION_NO_PROCESS_NAME,
                    "Не задано имя процесса"));
        }
        switch(processType) {
            case "start.process":
                processService.startProcess(processName, domainObjectId, null);
                break;
            case "complete.task":
                processService.completeTask(new RdbmsId(actionConfig.getProperty("complete.task.id")),
                        null,
                        actionConfig.getProperty("complete.task.action"));
                break;
            case "send.message":
                processService.sendProcessMessage(processName, domainObjectId, actionConfig.getProperty("message.name"), null);
                break;
            default:
                new GuiException(buildMessage(LocalizationKeys.GUI_EXCEPTION_PROCESS_NOT_SUPPORTED,
                        "Process '${processType}' not supported.",
                        new Pair("processType", processType)));
        }
        final SimpleActionData result = new SimpleActionData();
        return result;
    }

    @Override
    public SimpleActionContext getActionContext(ActionConfig actionConfig) {
        return new SimpleActionContext(actionConfig);
    }

    private String buildMessage(String message, String defaultValue) {
        return MessageResourceProvider.getMessage(message, defaultValue, profileService.getPersonLocale());
    }

    private String buildMessage(String message, String defaultValue, Pair<String, String> ... params) {
        Map<String, String> paramsMap = new HashMap<>();
        for (Pair<String, String> pair  : params) {
            paramsMap.put(pair.getFirst(), pair.getSecond());
        }
        return PlaceholderResolver.substitute(buildMessage(message, defaultValue), paramsMap);
    }
}

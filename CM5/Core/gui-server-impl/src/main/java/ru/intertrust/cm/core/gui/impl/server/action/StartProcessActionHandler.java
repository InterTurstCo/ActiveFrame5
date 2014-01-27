package ru.intertrust.cm.core.gui.impl.server.action;

import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.ProcessService;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.gui.api.server.action.ActionHandler;
import ru.intertrust.cm.core.gui.impl.server.plugin.handlers.FormPluginHandler;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.GuiException;
import ru.intertrust.cm.core.gui.model.action.*;
import ru.intertrust.cm.core.gui.model.plugin.FormPluginConfig;
import ru.intertrust.cm.core.gui.model.plugin.FormPluginData;

/**
 * @author Denis Mitavskiy Date: 23.10.13 Time: 15:10
 */
@ComponentName("start.process.action")
public class StartProcessActionHandler extends ActionHandler {

    @Autowired
    private ProcessService processservice;

    @Override
    public <T extends ActionData> T executeAction(ActionContext context) {
        StartProcessActionContext startProcessActionContext = (StartProcessActionContext) context;
        Id domainObjectId = startProcessActionContext.getRootObjectId();
        if (domainObjectId == null) {
            throw new GuiException("Объект ещё не сохранён");
        }

        // todo: do some action with this domain object or with new domain
        // object
        processservice.startProcess(
                ((StartProcessActionSettings) startProcessActionContext.getActionConfig().getActionSettings()).getProcessName(),
                domainObjectId, null);

        // get new form after process start
        FormPluginHandler handler = (FormPluginHandler) applicationContext.getBean("form.plugin");
        FormPluginConfig config = new FormPluginConfig(domainObjectId);
        StartProcessActionData result = new StartProcessActionData();
        result.setFormPluginData((FormPluginData) handler.initialize(config));
        return (T) result;
    }
}

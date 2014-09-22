package ru.intertrust.cm.core.gui.impl.server.action;

import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.ProcessService;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.gui.action.ActionConfig;
import ru.intertrust.cm.core.gui.api.server.action.ActionHandler;
import ru.intertrust.cm.core.gui.impl.server.plugin.handlers.FormPluginHandler;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.GuiException;
import ru.intertrust.cm.core.gui.model.action.StartProcessActionContext;
import ru.intertrust.cm.core.gui.model.action.StartProcessActionData;
import ru.intertrust.cm.core.gui.model.action.StartProcessActionSettings;
import ru.intertrust.cm.core.gui.model.plugin.FormPluginConfig;

/**
 * @author Denis Mitavskiy Date: 23.10.13 Time: 15:10
 */
@ComponentName("start.process.action")
public class StartProcessActionHandler extends ActionHandler<StartProcessActionContext, StartProcessActionData> {

    @Autowired
    private ProcessService processservice;

    @Override
    public StartProcessActionData executeAction(StartProcessActionContext startProcessActionContext) {
        Id domainObjectId = startProcessActionContext.getRootObjectId();
        if (domainObjectId == null) {
            throw new GuiException("Объект ещё не сохранён");
        }

        // todo: do some action with this domain object or with new domain
        // object
        final ActionConfig actionConfig = (ActionConfig) startProcessActionContext.getActionConfig();
        processservice.startProcess(((StartProcessActionSettings) actionConfig.getActionSettings()).getProcessName(),
                domainObjectId, null);

        // get new form after process start
        FormPluginHandler handler = (FormPluginHandler) applicationContext.getBean("form.plugin");
        FormPluginConfig config = new FormPluginConfig(domainObjectId);
        config.setPluginState(startProcessActionContext.getPluginState());
        config.setFormViewerConfig(startProcessActionContext.getFormViewerConfig());
        StartProcessActionData result = new StartProcessActionData();
        result.setFormPluginData(handler.initialize(config));
        return result;
    }

    @Override
    public StartProcessActionContext getActionContext(final ActionConfig actionConfig) {
        return new StartProcessActionContext(actionConfig);
    }
}

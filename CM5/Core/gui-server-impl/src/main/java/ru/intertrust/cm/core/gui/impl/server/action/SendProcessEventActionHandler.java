package ru.intertrust.cm.core.gui.impl.server.action;

import org.springframework.beans.factory.annotation.Autowired;

import ru.intertrust.cm.core.business.api.PermissionService;
import ru.intertrust.cm.core.business.api.ProcessService;
import ru.intertrust.cm.core.business.api.ProfileService;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.gui.action.ActionConfig;
import ru.intertrust.cm.core.config.localization.LocalizationKeys;
import ru.intertrust.cm.core.config.localization.MessageResourceProvider;
import ru.intertrust.cm.core.gui.api.server.action.ActionHandler;
import ru.intertrust.cm.core.gui.impl.server.plugin.handlers.FormPluginHandler;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.GuiException;
import ru.intertrust.cm.core.gui.model.action.ActionContext;
import ru.intertrust.cm.core.gui.model.action.SendProcessEventActionContext;
import ru.intertrust.cm.core.gui.model.action.SendProcessEventActionData;
import ru.intertrust.cm.core.gui.model.action.SendProcessEventSettings;
import ru.intertrust.cm.core.gui.model.plugin.FormPluginConfig;

/**
 * @author Denis Mitavskiy Date: 23.10.13 Time: 15:19
 */
@ComponentName("send.process.event.action")
public class SendProcessEventActionHandler extends ActionHandler<SendProcessEventActionContext, SendProcessEventActionData> {
    @Autowired
    private ProcessService processservice;

    @Autowired
    private ProfileService profileService;

    @Autowired
    private PermissionService permissionService;

    @Override
    public SendProcessEventActionData executeAction(SendProcessEventActionContext context) {
        Id domainObjectId = context.getRootObjectId();
        if (domainObjectId == null) {
            throw new GuiException(MessageResourceProvider.getMessage(LocalizationKeys.GUI_EXCEPTION_OBJECT_NOT_SAVED,
                    "Объект ещё не сохранён",
                    profileService.getPersonLocale()));
        }

        // todo: do some action with this domain object or with new domain
        // object
        final ActionConfig actionConfig = (ActionConfig) context.getActionConfig();
        SendProcessEventSettings sendProcessEventSettings = (SendProcessEventSettings) actionConfig.getActionSettings();

        processservice.sendProcessMessage(sendProcessEventSettings.getProcessName(), domainObjectId, sendProcessEventSettings.getEvent(), null);

        //Пересчитываем права чтобы корректно отобразились панель с кнопками
        permissionService.refreshAcls();

        // get new form after process start
        FormPluginHandler handler = (FormPluginHandler) applicationContext.getBean("form.plugin");
        FormPluginConfig config = new FormPluginConfig(domainObjectId);
        config.setPluginState(context.getPluginState());
        config.setFormViewerConfig(context.getFormViewerConfig());
        SendProcessEventActionData result = new SendProcessEventActionData();
        result.setFormPluginData(handler.initialize(config));
        return result;
    }

    @Override
    public ActionContext getActionContext(final ActionConfig actionConfig) {
        return new SendProcessEventActionContext(actionConfig);
    }
}

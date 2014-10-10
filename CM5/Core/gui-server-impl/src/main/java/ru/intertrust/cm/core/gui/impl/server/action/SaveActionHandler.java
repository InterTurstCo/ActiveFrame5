package ru.intertrust.cm.core.gui.impl.server.action;

import ru.intertrust.cm.core.UserInfo;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.config.gui.action.ActionConfig;
import ru.intertrust.cm.core.gui.api.server.GuiContext;
import ru.intertrust.cm.core.gui.api.server.action.ActionHandler;
import ru.intertrust.cm.core.gui.impl.server.plugin.handlers.FormPluginHandler;
import ru.intertrust.cm.core.gui.impl.server.util.PluginHandlerHelper;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.action.SaveActionContext;
import ru.intertrust.cm.core.gui.model.action.SaveActionData;
import ru.intertrust.cm.core.gui.model.plugin.FormPluginConfig;
import ru.intertrust.cm.core.gui.model.validation.ValidationException;

import java.util.List;

/**
 * @author Denis Mitavskiy
 *         Date: 19.09.13
 *         Time: 13:18
 */
@ComponentName("save.action")
@Deprecated
public class SaveActionHandler extends ActionHandler<SaveActionContext, SaveActionData> {

    @Override
    public SaveActionData executeAction(SaveActionContext context) {
        final List<String> errorMessages =
                PluginHandlerHelper.doServerSideValidation(context.getFormState(), applicationContext);
        if (context.getConfirmFormState() != null) {
            errorMessages.addAll(PluginHandlerHelper.doServerSideValidation(
                    context.getConfirmFormState(), applicationContext));
        }
        if (!errorMessages.isEmpty()) {
            throw new ValidationException("Server-side validation failed", errorMessages);
        }
        final UserInfo userInfo = GuiContext.get().getUserInfo();
        boolean isImmediate = ((ActionConfig)context.getActionConfig()).isImmediate();
        DomainObject rootDomainObject = guiService.saveForm(context.getFormState(), userInfo,
                isImmediate ? null : context.getActionConfig().getCustomValidators());
        FormPluginHandler handler = (FormPluginHandler) applicationContext.getBean("form.plugin");
        FormPluginConfig config = new FormPluginConfig(rootDomainObject.getId());
        config.setPluginState(context.getPluginState());
        config.setFormViewerConfig(context.getFormViewerConfig());
        SaveActionData result = new SaveActionData();
        result.setFormPluginData(handler.initialize(config));
        return result;
    }

    @Override
    public SaveActionContext getActionContext(final ActionConfig actionConfig) {
        return new SaveActionContext(actionConfig);
    }

    @Override
    public HandlerStatusData getCheckStatusData() {
        return new FormPluginHandlerStatusData();
    }
}

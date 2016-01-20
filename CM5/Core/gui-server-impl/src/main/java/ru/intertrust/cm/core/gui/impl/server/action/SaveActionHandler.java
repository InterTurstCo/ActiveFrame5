package ru.intertrust.cm.core.gui.impl.server.action;

import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.UserInfo;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.config.BusinessUniverseConfig;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.gui.action.ActionConfig;
import ru.intertrust.cm.core.gui.api.server.GuiContext;
import ru.intertrust.cm.core.gui.api.server.action.ActionHandler;
import ru.intertrust.cm.core.gui.impl.server.plugin.handlers.FormPluginHandler;
import ru.intertrust.cm.core.gui.impl.server.util.PluginHandlerHelper;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.action.SaveActionContext;
import ru.intertrust.cm.core.gui.model.action.SaveActionData;
import ru.intertrust.cm.core.gui.model.form.FormState;
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

    @Autowired
    private ConfigurationExplorer configurationExplorer;

    @Autowired
    private ConfigurationExplorer configurationService;

    @Override
    public SaveActionData executeAction(SaveActionContext context) {
        String locale = GuiContext.getUserLocale();
        final FormState formState = context.getFormState();
        final List<String> errorMessages =
                PluginHandlerHelper.doServerSideValidation(formState, applicationContext,
                        locale);
        if (context.getConfirmFormState() != null) {
            errorMessages.addAll(PluginHandlerHelper.doServerSideValidation(
                    context.getConfirmFormState(), applicationContext, locale));
        }
        if (!errorMessages.isEmpty()) {
            throw new ValidationException("Server-side validation failed", errorMessages);
        }
        final UserInfo userInfo = GuiContext.get().getUserInfo();
        boolean isImmediate = ((ActionConfig)context.getActionConfig()).isImmediate();
        final boolean reReadInSameTransaction = configurationExplorer.getPlainFormConfig(formState.getName()).reReadInSameTransaction();
        DomainObject rootDomainObject;
        if (reReadInSameTransaction) {
            rootDomainObject = guiService.saveForm(formState, userInfo, isImmediate ? null : context.getActionConfig().getCustomValidators());
        } else {
            rootDomainObject = guiService.saveFormInNewTransaction(formState, userInfo, context.getActionConfig().getCustomValidators());
        }
        FormPluginHandler handler = (FormPluginHandler) applicationContext.getBean("form.plugin");
        FormPluginConfig config = new FormPluginConfig(rootDomainObject.getId());
        config.setPluginState(context.getPluginState());
        config.setFormViewerConfig(context.getFormViewerConfig());
        SaveActionData result = new SaveActionData();
        result.setFormPluginData(handler.initialize(config));

        BusinessUniverseConfig businessUniverseConfig = configurationService.getConfig(BusinessUniverseConfig.class,
                BusinessUniverseConfig.NAME);
        result.setDefaultFormEditingStyleConfig(businessUniverseConfig.getDefaultFormEditingStyleConfig());
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

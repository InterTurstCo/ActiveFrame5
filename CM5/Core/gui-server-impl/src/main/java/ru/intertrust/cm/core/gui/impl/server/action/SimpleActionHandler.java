package ru.intertrust.cm.core.gui.impl.server.action;

import java.util.List;

import ru.intertrust.cm.core.UserInfo;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.config.gui.action.ActionConfig;
import ru.intertrust.cm.core.config.gui.action.SimpleActionConfig;
import ru.intertrust.cm.core.gui.api.server.GuiContext;
import ru.intertrust.cm.core.gui.api.server.action.ActionHandler;
import ru.intertrust.cm.core.gui.impl.server.plugin.handlers.FormPluginHandler;
import ru.intertrust.cm.core.gui.impl.server.util.PluginHandlerHelper;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.action.SimpleActionContext;
import ru.intertrust.cm.core.gui.model.action.SimpleActionData;
import ru.intertrust.cm.core.gui.model.form.FormState;
import ru.intertrust.cm.core.gui.model.plugin.FormPluginConfig;
import ru.intertrust.cm.core.gui.model.plugin.FormPluginData;
import ru.intertrust.cm.core.gui.model.validation.ValidationException;

/**
 * @author Sergey.Okolot
 *         Created on 23.09.2014 11:50.
 */
@ComponentName("simple.action")
public class SimpleActionHandler extends ActionHandler<SimpleActionContext, SimpleActionData> {

    @Override
    public SimpleActionData executeAction(SimpleActionContext context) {
        final List<String> errorMessages =
                PluginHandlerHelper.doServerSideValidation(context.getMainFormState(), applicationContext);
        if (context.getConfirmFormState() != null) {
            errorMessages.addAll(PluginHandlerHelper.doServerSideValidation(
                    context.getConfirmFormState(), applicationContext));
        }
        if (!errorMessages.isEmpty()) {
            throw new ValidationException("Server-side validation failed", errorMessages);
        }
        final SimpleActionConfig config = context.getActionConfig();
        final boolean isSaveContext = config.getBeforeConfig() == null
                ? true
                : config.getBeforeConfig().isSaveContext();
        if (isSaveContext) {
            final UserInfo userInfo = GuiContext.get().getUserInfo();
            final FormState maiFormState = context.getMainFormState();
            final FormState confirmFormState = context.getConfirmFormState();
            final DomainObject mainDomainObject;
            if (confirmFormState != null) {
                // fixme resolve references
                mainDomainObject = guiService.saveForm(maiFormState, userInfo);
//                final DomainObject confirmDomainObject = guiService.saveForm(confirmFormState, userInfo);
//                context.setConfirmDomainObjectId(confirmDomainObject.getId());
            } else {
                mainDomainObject = guiService.saveForm(maiFormState, userInfo);
            }
            context.setContextSaved();
            context.setRootObjectId(mainDomainObject.getId());
        }
        final ActionHandler delegate = (ActionHandler) applicationContext.getBean(config.getActionHandler());
        final SimpleActionData result = (SimpleActionData) delegate.executeAction(context);
        FormPluginHandler handler = (FormPluginHandler) applicationContext.getBean("form.plugin");
        FormPluginConfig formPluginConfig = new FormPluginConfig(context.getRootObjectId());
        formPluginConfig.setPluginState(context.getPluginState());
        formPluginConfig.setFormViewerConfig(context.getViewerConfig());
        final FormPluginData formPluginData = handler.initialize(formPluginConfig);
        result.setPluginData(formPluginData);
        if (config.getAfterConfig() != null) {
            result.setOnSuccessMessage(config.getAfterConfig().getMessageConfig() == null
                    ? null
                    : config.getAfterConfig().getMessageConfig().getText());
        }
        return result;
    }

    @Override
    public SimpleActionContext getActionContext(ActionConfig actionConfig) {
        return new SimpleActionContext(actionConfig);
    }
}

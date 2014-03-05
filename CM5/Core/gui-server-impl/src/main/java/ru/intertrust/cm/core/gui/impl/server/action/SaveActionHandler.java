package ru.intertrust.cm.core.gui.impl.server.action;

import ru.intertrust.cm.core.UserInfo;
import ru.intertrust.cm.core.business.api.dto.Constraint;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.config.gui.ActionConfig;
import ru.intertrust.cm.core.gui.api.server.action.ActionHandler;
import ru.intertrust.cm.core.gui.api.server.widget.WidgetHandler;
import ru.intertrust.cm.core.gui.impl.server.GuiContext;
import ru.intertrust.cm.core.gui.impl.server.plugin.handlers.FormPluginHandler;
import ru.intertrust.cm.core.gui.impl.server.validation.validators.NotEmptyValidator;
import ru.intertrust.cm.core.gui.impl.server.validation.validators.ServerValidator;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.action.ActionContext;
import ru.intertrust.cm.core.gui.model.action.ActionData;
import ru.intertrust.cm.core.gui.model.action.SaveActionContext;
import ru.intertrust.cm.core.gui.model.action.SaveActionData;
import ru.intertrust.cm.core.gui.model.form.FormState;
import ru.intertrust.cm.core.gui.model.form.widget.WidgetState;
import ru.intertrust.cm.core.gui.model.plugin.FormPluginConfig;
import ru.intertrust.cm.core.gui.model.validation.ValidationException;
import ru.intertrust.cm.core.gui.model.validation.ValidationResult;

import java.util.List;

/**
 * @author Denis Mitavskiy
 *         Date: 19.09.13
 *         Time: 13:18
 */
@ComponentName("save.action")
public class SaveActionHandler extends ActionHandler {

    @Override
    public <T extends ActionData> T executeAction(ActionContext context) {
        ValidationResult validationResult = validate(context);
        if (validationResult.hasErrors()) {
            throw new ValidationException("Server validation failed", validationResult);
        }

        final UserInfo userInfo = GuiContext.get().getUserInfo();
        DomainObject rootDomainObject = guiService.saveForm(((SaveActionContext) context).getFormState(), userInfo);
        FormPluginHandler handler = (FormPluginHandler) applicationContext.getBean("form.plugin");
        FormPluginConfig config = new FormPluginConfig(rootDomainObject.getId());
        config.setPluginState(((SaveActionContext) context).getPluginState());
        SaveActionData result = new SaveActionData();
        result.setFormPluginData(handler.initialize(config));
        return (T) result;
    }

    private ValidationResult validate(ActionContext context) {
        ValidationResult validationResult = new ValidationResult();
        ActionConfig actionConfig = context.getActionConfig();
        List<Constraint> constraints = actionConfig.getConstratints();
        FormState formState = ((SaveActionContext) context).getFormState();
        for (Constraint constraint : constraints) {
            ServerValidator validator = createValidator(constraint);
            if (validator != null) {
                String widgetId = constraint.param(Constraint.PARAM_WIDGET_ID);
                String domainObjectType = constraint.param(Constraint.PARAM_DOMAIN_OBJECT_TYPE);
                String componentName = getComponentNameByWidgetId(widgetId, domainObjectType);

                WidgetState state = formState.getWidgetState(widgetId);
                if (state != null) {
                    WidgetHandler handler = getWidgetHandler(componentName);
                    validationResult.append(validator.validate(handler.getValue(state)));
                }

            }
        }

        return validationResult;
    }

    private String getComponentNameByWidgetId(String widgetId, String domainObjectType) {
        return guiService.getForm(domainObjectType, GuiContext.get().getUserInfo()).getWidgetComponent(widgetId);
    }

    private ServerValidator createValidator(Constraint constraint) {
        return new NotEmptyValidator(); // TODO: [validation] instantiate server validator based on the constraints type.
    }

    private WidgetHandler getWidgetHandler(String componentName) {
        return (WidgetHandler) applicationContext.getBean(componentName);
    }
}

package ru.intertrust.cm.core.gui.impl.server.action;

import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.gui.form.FormConfig;
import ru.intertrust.cm.core.config.gui.form.FormObjectsRemoverConfig;
import ru.intertrust.cm.core.gui.api.server.action.ActionHandler;
import ru.intertrust.cm.core.gui.api.server.form.FormObjectsRemover;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.action.ActionContext;
import ru.intertrust.cm.core.gui.model.action.DeleteActionData;
import ru.intertrust.cm.core.gui.model.action.SaveActionContext;
import ru.intertrust.cm.core.gui.model.form.FormState;

/**
 * User: IPetrov
 * Date: 09.12.13
 * Time: 15:00
 * обработчик удаления элемента коллекции, удаляется в GUI из таблицы и соответственно на сервере
 */
@ComponentName("delete.action")
public class DeleteActionHandler extends ActionHandler<ActionContext, DeleteActionData> {
    @Autowired
    protected ConfigurationExplorer configurationExplorer;

    @Override
    public DeleteActionData executeAction(ActionContext context) {
        FormState currentFormState = ((SaveActionContext) context).getFormState();
        final FormConfig formConfig = configurationExplorer.getConfig(FormConfig.class, currentFormState.getName());
        final FormObjectsRemoverConfig formObjectsRemoverConfig = formConfig.getFormObjectsRemoverConfig();
        String removerComponent = "defaultFormObjectsRemover";
        if (formObjectsRemoverConfig != null) {
            final String handler = formObjectsRemoverConfig.getHandler();
            if (handler != null && !handler.isEmpty()) {
                removerComponent = handler;
            }
        }
        ((FormObjectsRemover) applicationContext.getBean(removerComponent)).deleteForm(currentFormState);

        DeleteActionData result = new DeleteActionData();
        result.setId(context.getRootObjectId());

        return result;
    }

    @Override
    public ActionContext getActionContext() {
        return new SaveActionContext();
    }

    @Override
    public HandlerStatusData getCheckStatusData() {
        return new FormPluginHandlerStatusData();
    }

    @Override
    public Status getHandlerStatus(String conditionExpression, HandlerStatusData condition) {
        conditionExpression = conditionExpression.replaceAll(TOGGLE_EDIT_ATTR, TOGGLE_EDIT_KEY);
        final boolean result = evaluateExpression(conditionExpression, condition);
        return result ? Status.APPLY : Status.SKIP;
    }
}


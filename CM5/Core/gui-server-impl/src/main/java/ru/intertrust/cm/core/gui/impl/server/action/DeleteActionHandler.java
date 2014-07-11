package ru.intertrust.cm.core.gui.impl.server.action;

import ru.intertrust.cm.core.gui.api.server.action.ActionHandler;
import ru.intertrust.cm.core.gui.impl.server.form.FormObjectsRemover;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.action.ActionContext;
import ru.intertrust.cm.core.gui.model.action.DeleteActionData;
import ru.intertrust.cm.core.gui.model.action.SaveActionContext;

/**
 * User: IPetrov
 * Date: 09.12.13
 * Time: 15:00
 * обработчик удаления элемента коллекции, удаляется в GUI из таблицы и соответственно на сервере
 */
@ComponentName("delete.action")
public class DeleteActionHandler extends ActionHandler<ActionContext, DeleteActionData> {

    @Override
    public DeleteActionData executeAction(ActionContext context) {
        final FormObjectsRemover remover = (FormObjectsRemover) applicationContext.getBean("formObjectsRemover");
        remover.deleteForm(context.getRootObjectId());

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


package ru.intertrust.cm.core.gui.impl.server.action;

import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.gui.api.server.action.ActionHandler;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.action.ActionContext;
import ru.intertrust.cm.core.gui.model.action.ActionData;
import ru.intertrust.cm.core.gui.model.action.DeleteActionData;

/**
 * User: IPetrov
 * Date: 09.12.13
 * Time: 15:00
 * обработчик удаления элемента коллекции, удаляется в GUI из таблицы и соответственно на сервере
 */
@ComponentName("delete.action")
public class DeleteActionHandler extends ActionHandler {

    @Autowired
    private CrudService crudService;

    @Override
    public <T extends ActionData> T executeAction(ActionContext context) {

        crudService.delete(context.getRootObjectId());

        DeleteActionData result = new DeleteActionData();
        result.setId(context.getRootObjectId());

        return (T) result;
    }
}


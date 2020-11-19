package ru.intertrust.cm.nbrbase.gui.actions;

import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.ProcessService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.gui.impl.server.action.SimpleActionHandler;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.GuiException;
import ru.intertrust.cm.core.gui.model.action.SimpleActionContext;
import ru.intertrust.cm.core.gui.model.action.SimpleActionData;

@ComponentName("restore.process.model")
public class RestoreProcessModel extends SimpleActionHandler {
    @Autowired
    private CrudService crudService;

    @Override
    public SimpleActionData executeAction(SimpleActionContext context) {

        DomainObject processInfo = context.getMainFormState().getObjects().getRootDomainObject();

        crudService.setStatus(processInfo.getId(), "Active");

        SimpleActionData actionData = new SimpleActionData();
        actionData.setDeleteAction(true);
        actionData.setDeletedObject(context.getRootObjectId());
        actionData.setOnSuccessMessage("Шаблон процесса восстановлен из корзины");
        return actionData;
    }
}

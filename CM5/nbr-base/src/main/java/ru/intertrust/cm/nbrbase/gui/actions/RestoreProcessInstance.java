package ru.intertrust.cm.nbrbase.gui.actions;

import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.ProcessService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.gui.impl.server.action.SimpleActionHandler;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.DomainObjectMappingId;
import ru.intertrust.cm.core.gui.model.GuiException;
import ru.intertrust.cm.core.gui.model.action.SimpleActionContext;
import ru.intertrust.cm.core.gui.model.action.SimpleActionData;

@ComponentName("restore.process.instance")
public class RestoreProcessInstance extends SimpleActionHandler {
    @Autowired
    private ProcessService processService;

    @Override
    public SimpleActionData executeAction(SimpleActionContext context) {
        DomainObject processInstance = context.getMainFormState().getObjects().getRootDomainObject();
        DomainObjectMappingId id = (DomainObjectMappingId)processInstance.getId();
        processService.activateProcessInstance(id.getId());

        SimpleActionData actionData = new SimpleActionData();
        actionData.setDeleteAction(true);
        actionData.setDeletedObject(context.getRootObjectId());
        actionData.setOnSuccessMessage("Процесс возабновлен");
        return actionData;
    }
}

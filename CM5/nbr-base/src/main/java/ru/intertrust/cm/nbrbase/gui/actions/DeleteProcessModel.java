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

@ComponentName("delete.process.model")
public class DeleteProcessModel extends SimpleActionHandler {
    @Autowired
    private CrudService crudService;

    @Autowired
    private ProcessService processService;

    @Override
    public SimpleActionData executeAction(SimpleActionContext context) {

        DomainObject processInfo = context.getMainFormState().getObjects().getRootDomainObject();

        if (processService.getLastProcessDefinitionId(processInfo.getString("process_id")).equals(processInfo.getId())){
            throw new GuiException("Нельзя удалять активную версию шаблона процесса");
        }

        crudService.setStatus(processInfo.getId(), "Trash");

        SimpleActionData actionData = new SimpleActionData();
        actionData.setDeleteAction(true);
        actionData.setDeletedObject(context.getRootObjectId());
        actionData.setOnSuccessMessage("Шаблон процесса удален в корзину");
        return actionData;
    }
}

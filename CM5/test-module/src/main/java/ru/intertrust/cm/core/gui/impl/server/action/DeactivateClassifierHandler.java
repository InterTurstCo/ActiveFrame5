package ru.intertrust.cm.core.gui.impl.server.action;

import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.action.SimpleActionContext;
import ru.intertrust.cm.core.gui.model.action.SimpleActionData;

/**
 * Created by Ravil Abdulkhairov on 15.10.2014.
 */
@ComponentName("cancel.classifier.action")
public class DeactivateClassifierHandler extends SimpleActionHandler {
    private static final String STATUS_DEACTIVATED = "Аннулирован";

    @Autowired
    private CrudService crudService;

    @Autowired
    private ContactsManager contactsManager;

    @Override
    public SimpleActionData executeAction(SimpleActionContext context) {
        Id rootObjectId = context.getRootObjectId();
        SimpleActionData actionData = new SimpleActionData();
        System.out.println("DEACTIVATION!!!");
        if (rootObjectId != null) {
            DomainObject rootDomainObject = crudService.find(rootObjectId);

            if (!contactsManager.getStatusById(rootDomainObject.getStatus()).equals(STATUS_DEACTIVATED)) {
                contactsManager.changeStatusForDo(rootDomainObject.getId(), STATUS_DEACTIVATED);
            }
        }
        return actionData;
    }
}

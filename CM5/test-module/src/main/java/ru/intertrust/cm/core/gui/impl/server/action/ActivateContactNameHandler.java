package ru.intertrust.cm.core.gui.impl.server.action;

import org.springframework.beans.factory.annotation.Autowired;

import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.gui.api.server.action.ActionHandler;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.action.SimpleActionContext;
import ru.intertrust.cm.core.gui.model.action.SimpleActionData;

/**
 * Created by Konstantin Gordeev on 14.10.2014.
 */
@ComponentName("activate.contact.name.action")
public class ActivateContactNameHandler extends ActionHandler<SimpleActionContext, SimpleActionData> {

    private static final String STATUS_ACTIVATED = "Актуальный";

    @Autowired
    private CrudService crudService;

    @Autowired
    private ContactsManager contactsManager;

    @Override
    public SimpleActionData executeAction(SimpleActionContext context) {
        Id rootObjectId = context.getRootObjectId();
        SimpleActionData actionData = new SimpleActionData();
        if (rootObjectId != null) {
            DomainObject rootDomainObject = crudService.find(rootObjectId);

            if (!contactsManager.getStatusById(rootDomainObject.getStatus()).equals(STATUS_ACTIVATED)) {
                contactsManager.changeStatusForDo(rootDomainObject.getId(), STATUS_ACTIVATED);
            }
        }
        return actionData;
    }
}
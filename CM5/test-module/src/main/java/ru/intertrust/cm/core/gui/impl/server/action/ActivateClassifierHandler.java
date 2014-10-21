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
 * Created by Ravil Abdulkhairov  on 15.10.2014.
 */
@ComponentName("activate.classifier.action")
public class ActivateClassifierHandler extends ActionHandler<SimpleActionContext, SimpleActionData> {
    private static final String STATUS_ACTIVATED = "Active";

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
                contactsManager.changeStatusForDo(rootDomainObject.getId(), STATUS_ACTIVATED);
        }
        return actionData;
    }
}

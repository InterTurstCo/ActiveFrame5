package ru.intertrust.cm.core.gui.impl.server.action;

import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.gui.api.server.action.ActionVisibilityChecker;
import ru.intertrust.cm.core.gui.api.server.action.ActionVisibilityContext;
import ru.intertrust.cm.core.gui.model.ComponentName;

@ComponentName("deactivate.contact.name.visibility.checker")
public class DeActivateContactNameVisibilityChecker implements ActionVisibilityChecker {

    private static final String STATUS_DEACTIVATED = "Аннулирован";

    @Autowired
    private ContactsManager contactsManager;

    @Override
    public boolean isVisible(ActionVisibilityContext context) {
        DomainObject currentDomainObject = context.getDomainObject();

        if (currentDomainObject != null && currentDomainObject.getId() != null) {
            if(currentDomainObject.getStatus() != null) {
                if (!contactsManager.getStatusById(currentDomainObject.getStatus()).equals(STATUS_DEACTIVATED)) {
                    return true;
                }
            }
        }

        return false;
    }
}

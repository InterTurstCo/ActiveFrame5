package ru.intertrust.cm.core.gui.impl.server.action;

import ru.intertrust.cm.core.gui.api.server.action.ActionVisibilityChecker;
import ru.intertrust.cm.core.gui.api.server.action.ActionVisibilityContext;
import ru.intertrust.cm.core.gui.model.ComponentName;




/**
 * Возвращает TRUE если доменный объект новый.
 *
 * @author Ravil Abdulkhairov Created on 24.09.2014
 */
@ComponentName("classifier.deactivate.visibility.checker")
public class ClassifierDeactivateVisibilityChecker implements ActionVisibilityChecker {
    private static String ACTIVE = "Active";
    private static String FIELD_STATUS = "status";
    private ContactsManager contactsManager;


    @Override
    public boolean isVisible(ActionVisibilityContext context) {
        String statusName;
        contactsManager = new ContactsManager();
        if (context.getDomainObject() != null) {
            if(context.getDomainObject().getReference(FIELD_STATUS)!=null){
                statusName = contactsManager.getStatusById(context.getDomainObject().getReference(FIELD_STATUS));
                if(statusName!=null && statusName.trim().equals(ACTIVE))
                {
                   return true;
                }
                else {
                    return false;
                }
            }
            else {
                return false;
            }
        } else {
            return false;
        }

    }


}

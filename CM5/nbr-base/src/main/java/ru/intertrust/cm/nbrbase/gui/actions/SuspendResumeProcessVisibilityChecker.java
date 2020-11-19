package ru.intertrust.cm.nbrbase.gui.actions;

import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.CollectionsService;
import ru.intertrust.cm.core.business.api.PersonService;
import ru.intertrust.cm.core.business.api.dto.IdentifiableObjectCollection;
import ru.intertrust.cm.core.gui.api.server.action.ActionVisibilityChecker;
import ru.intertrust.cm.core.gui.api.server.action.ActionVisibilityContext;
import ru.intertrust.cm.core.gui.model.ComponentName;

/**
 * Видимость кнопки
 */
@ComponentName("suspend.resume.process.visibility.checker")
public class SuspendResumeProcessVisibilityChecker implements ActionVisibilityChecker {

    @Override
    public boolean isVisible(ActionVisibilityContext context) {
        /*
        // что то не работает, всегда приходит context.getDomainObject() == null
        if (context.getDomainObject() != null) {
            boolean suspended = context.getDomainObject().getBoolean("suspended");
            return context.getActionConfig().getName().equals("suspend") ? !suspended : suspended;
        }else{
            return false;
        }*/

        return true;
    }
}

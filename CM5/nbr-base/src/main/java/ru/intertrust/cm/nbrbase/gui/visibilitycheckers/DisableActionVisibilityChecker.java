package ru.intertrust.cm.nbrbase.gui.visibilitycheckers;

import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.dao.api.CurrentUserAccessor;
import ru.intertrust.cm.core.dao.api.PersonManagementServiceDao;
import ru.intertrust.cm.core.gui.api.server.action.ActionVisibilityChecker;
import ru.intertrust.cm.core.gui.api.server.action.ActionVisibilityContext;
import ru.intertrust.cm.core.gui.model.ComponentName;

import java.util.List;

@ComponentName("disable.action.visibility.checker")
public class DisableActionVisibilityChecker implements ActionVisibilityChecker {
    
    @Autowired
    CurrentUserAccessor currentUserAccessor;
    
    @Autowired
    PersonManagementServiceDao personManagementServiceDao;

    @Override
    public boolean isVisible(ActionVisibilityContext context) {

        final Id currentUserId = this.currentUserAccessor.getCurrentUserId();
        final List<DomainObject> groupList = this.personManagementServiceDao.getPersonGroups(currentUserId);
        for (final DomainObject group : groupList)
        {
            if ("superusers".equalsIgnoreCase(group.getString("group_name")) || "administrators".equalsIgnoreCase(group.getString("group_name")))
            {
                if (context.getDomainObject() != null)
                {
                    if (context.getDomainObject().getBoolean("Enabled") )
                        return true;
                }
            }
        }
        return false;
    }
}
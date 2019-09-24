package ru.intertrust.cm.nbrbase.gui.visibilitycheckers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import ru.intertrust.cm.core.dao.api.CurrentUserAccessor;
import ru.intertrust.cm.core.dao.api.PersonManagementServiceDao;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.api.server.action.ActionVisibilityChecker;
import ru.intertrust.cm.core.gui.api.server.action.ActionVisibilityContext;
/**
 * Проверяет является ли текущий пользователь администратором
 * 
 * @author Ivan Fedosov
 *
 */
@ComponentName("admin.visibility.checker")
public class AdminVisibilityChecker implements ActionVisibilityChecker {
    
    @Autowired
    CurrentUserAccessor currentUserAccessor;
    
    @Autowired
    PersonManagementServiceDao personManagementServiceDao;

    @Override
    public boolean isVisible(ActionVisibilityContext context) {
        final Id currentUserId = this.currentUserAccessor.getCurrentUserId();
        final List<DomainObject> groupList = this.personManagementServiceDao.getPersonGroups(currentUserId);
        if(groupList!=null){
            for (final DomainObject group : groupList) {

                if ("superusers".equalsIgnoreCase(group.getString("group_name"))){
                    return true;
                }
            }
        }
        return false;
    }
}

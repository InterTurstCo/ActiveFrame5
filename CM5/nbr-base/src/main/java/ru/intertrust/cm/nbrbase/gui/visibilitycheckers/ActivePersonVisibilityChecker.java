package ru.intertrust.cm.nbrbase.gui.visibilitycheckers;

import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.StringValue;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.dao.api.CurrentUserAccessor;
import ru.intertrust.cm.core.dao.api.PersonManagementServiceDao;
import ru.intertrust.cm.core.gui.api.server.action.ActionVisibilityChecker;
import ru.intertrust.cm.core.gui.api.server.action.ActionVisibilityContext;
import ru.intertrust.cm.core.gui.model.ComponentName;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ComponentName("active.person.visibility.checker")
public class ActivePersonVisibilityChecker implements ActionVisibilityChecker {

    @Autowired
    CurrentUserAccessor currentUserAccessor;

    @Autowired
    PersonManagementServiceDao personManagementServiceDao;

    @Autowired
    CrudService crudService;
    
    @Override
    public boolean isVisible(ActionVisibilityContext context)
    {
        final Id currentUserId = this.currentUserAccessor.getCurrentUserId();
        final List<DomainObject> groupList = this.personManagementServiceDao.getPersonGroups(currentUserId);
        DomainObject person = context.getDomainObject();
        Id status = person.getReference("status");
        if(status != null) {
            Map<String, Value> map = new HashMap<>();
            map.put("name", new StringValue("Inactive"));
            DomainObject statusSleep = crudService.findByUniqueKey("status", map);
            if (status.equals(statusSleep.getId())) {
                return false;
            }

            for (final DomainObject group : groupList) {
                if ("superusers".equalsIgnoreCase(group.getString("group_name")) || "administrators".equalsIgnoreCase(group.getString("group_name"))) {
                    return true;
                }
            }
        }
        return false;
    }
}
package ru.intertrust.cm.globalcacheclient;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ejb.interceptor.SpringBeanAutowiringInterceptor;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.dao.api.PersonManagementServiceDao;
import ru.intertrust.cm.core.model.FatalException;
import ru.intertrust.cm.globalcache.api.GroupAccessChanges;
import ru.intertrust.cm.globalcache.api.PersonAccessChanges;

import javax.ejb.*;
import javax.interceptor.Interceptors;
import java.util.*;

/**
 * @author Denis Mitavskiy
 *         Date: 05.08.2015
 *         Time: 19:12
 */
@Stateless(name = "PersonAccessHelper")
@TransactionManagement(TransactionManagementType.CONTAINER)
@Local(PersonAccessHelper.class)
@Interceptors(SpringBeanAutowiringInterceptor.class)
public class PersonAccessHelperImpl implements PersonAccessHelper {
    @Autowired
    private PersonManagementServiceDao personManagementDao;

    @Override
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public PersonAccessChanges getPersonAccessChanges(GroupAccessChanges groupAccessChanges) {
        final HashSet<String> objectTypesAccessChanged = groupAccessChanges.getObjectTypesAccessChanged();
        if (groupAccessChanges.clearFullAccessLog()) {
            return new PersonAccessChanges(true, objectTypesAccessChanged);
        }
        if (!groupAccessChanges.accessChangesExist()) {
            return new PersonAccessChanges(0, objectTypesAccessChanged);
        }
        return getPersonAccessChanges(groupAccessChanges, objectTypesAccessChanged);
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public PersonAccessChanges getPersonAccessChanges(GroupAccessChanges groupAccessChanges, HashSet<String> objectTypesAccessChanged) {
        HashMap<Id, HashMap<Id, Boolean>> groupAccessByObject = groupAccessChanges.getGroupAccessByObject();
        PersonAccessChanges personAccessChanges = new PersonAccessChanges(groupAccessChanges.getObjectsQty(), objectTypesAccessChanged);
        try {
            for (Id objectId : groupAccessByObject.keySet()) {
                personAccessChanges.addObjectPersonAccess(objectId, toPersonAccess(groupAccessByObject.get(objectId)));
                if (personAccessChanges.clearFullAccessLog()) {
                    return personAccessChanges;
                }
            }
            setPersonsWhosGroupsChanged(personAccessChanges, groupAccessChanges);
            return personAccessChanges;
        } catch (Throwable e) {
            e.printStackTrace();
            throw new FatalException(e);
        }
    }

    private void setPersonsWhosGroupsChanged(PersonAccessChanges personAccessChanges, GroupAccessChanges groupAccessChanges) {
        final HashSet<Id> personsWhosGroupsChanged = groupAccessChanges.getPersonsWhosGroupsChanged();
        for (Id groupId : groupAccessChanges.getGroupsWithChangedBranching()) {
            personsWhosGroupsChanged.addAll(getIds(personManagementDao.getPersonsInGroup(groupId)));
        }
        personAccessChanges.setPersonsWhosAccessRightsChanged(personsWhosGroupsChanged);
    }

    private HashMap<Id, Boolean> toPersonAccess(HashMap<Id, Boolean> groupAccess) {
        HashMap<Id, Boolean> personAccess = new HashMap<>(groupAccess.size() * 10);
        for (Map.Entry<Id, Boolean> groupEntry : groupAccess.entrySet()) {
            Set<Id> personIds = getIds(personManagementDao.getAllPersonsInGroup(groupEntry.getKey()));
            for (Id personId : personIds) {
                Boolean accessGranted = personAccess.get(personId);
                if (accessGranted == Boolean.TRUE) { // if person got access in at least one group - it has access
                    continue;
                } else { // set access to unknown (null) if it's not given to a group. User has no access ONLY IF all groups he's included do not have access
                    personAccess.put(personId, groupEntry.getValue() == Boolean.TRUE ? Boolean.TRUE : null);
                }
            }
        }
        return personAccess;
    }

    private Set<Id> getIds(List<DomainObject> objects) {
        if (objects == null) {
            return Collections.EMPTY_SET;
        }
        final HashSet<Id> result = new HashSet<>((int) (objects.size() / 0.75f));
        for (DomainObject object : objects) {
            result.add(object.getId());
        }
        return result;
    }
}

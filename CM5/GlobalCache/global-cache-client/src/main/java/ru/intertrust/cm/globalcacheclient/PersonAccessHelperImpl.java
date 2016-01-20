package ru.intertrust.cm.globalcacheclient;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ejb.interceptor.SpringBeanAutowiringInterceptor;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.dao.api.PersonManagementServiceDao;
import ru.intertrust.cm.core.model.FatalException;
import ru.intertrust.cm.globalcache.api.GroupAccessChanges;
import ru.intertrust.cm.globalcache.api.PersonAccessChanges;

import javax.annotation.Resource;
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
    @Resource
    private EJBContext ejbContext;

    @Autowired
    private PersonManagementServiceDao personManagementDao;

    @EJB
    private PersonAccessHelper newTransantionService;

    @Override
    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public PersonAccessChanges getPersonAccessChanges(GroupAccessChanges groupAccessChanges) {
        final HashSet<String> objectTypesAccessChanged = groupAccessChanges.getObjectTypesAccessChanged();
        if (groupAccessChanges.clearFullAccessLog()) {
            return new PersonAccessChanges(true, objectTypesAccessChanged);
        }
        if (!groupAccessChanges.accessChangesExist()) {
            return new PersonAccessChanges(0, objectTypesAccessChanged);
        }
        return newTransantionService.getPersonAccessChanges(groupAccessChanges, objectTypesAccessChanged);
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
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
            return personAccessChanges;
        } catch (Throwable e) {
            e.printStackTrace();
            throw new FatalException(e);
        } finally {
            ejbContext.setRollbackOnly();
        }
    }

    private HashMap<Id, Boolean> toPersonAccess(HashMap<Id, Boolean> groupAccess) {
        HashMap<Id, Boolean> personAccess = new HashMap<>(groupAccess.size() * 10);
        for (Map.Entry<Id, Boolean> groupEntry : groupAccess.entrySet()) {
            Set<Id> personIds = getIds(personManagementDao.getAllPersonsInGroup(groupEntry.getKey()));
            for (Id personId : personIds) {
                Boolean accessGranted = personAccess.get(personId);
                if (accessGranted == Boolean.TRUE) { // if person got access in at least one group - it has access
                    continue;
                } else { // either absent or no access, which can change if group access is given
                    personAccess.put(personId, groupEntry.getValue());
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
